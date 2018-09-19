package org.kiwix.kiwixmobile;

import static org.kiwix.kiwixmobile.downloader.KiwixDownloadService.PAUSE;
import static org.kiwix.kiwixmobile.downloader.KiwixDownloadService.STOP;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_LIBRARY;
import static org.kiwix.kiwixmobile.utils.Constants.ONGOING_DOWNLOAD_CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import io.reactivex.Observable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;
import org.kiwix.kiwixmobile.downloader.KiwixDownloadService;
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity.Book;
import org.kiwix.kiwixmobile.network.KiwixLibraryService;
import org.kiwix.kiwixmobile.utils.files.FileUtils;

public class Zim implements Serializable {

  private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
  public static final long CHUNK_SIZE = 1024L * 1024L * 1024L * 2L;
  private static final String ACTION_TOGGLE_DOWNLOAD = "toggle_download";
  private static final String ACTION_STOP = "stop_download";
  private static final String DOWNLOAD_ID_EXTRA = "download_id";
  private static int DOWNLOAD_ID = 1000;

  private final String id;
  private final String title;
  private final String description;
  private final String language;
  private final String creator;
  private final String publisher;
  private final String favicon;
  private final String faviconMimeType;
  private final String date;
  private final String url;
  private final long articleCount;
  private final long mediaCount;
  private final String name;
  private final String tags;
  private final long size;

  private final File localFile;
  private final List<Chunk> componentFiles = new ArrayList<>();

  private long trueSize = -1;
  private String downloadUrl;
  private int downloadId = DOWNLOAD_ID++;

  public Zim(Book book, File localFile) {
    KiwixApplication.getApplicationComponent().inject(this);

    this.id = book.getId();
    this.title = book.getTitle();
    this.description = book.getDescription();
    this.language = book.getLanguage();
    this.creator = book.getCreator();
    this.publisher = book.getPublisher();
    this.favicon = book.getFavicon();
    this.faviconMimeType = book.getFaviconMimeType();
    this.date = book.getDate();
    this.url = book.getUrl();
    this.articleCount = tryParseLong(book.getArticleCount());
    this.mediaCount = tryParseLong(book.getMediaCount());
    this.tags = book.getTags();
    this.name = book.getName();
    this.size = tryParseLong(book.getSize());

    this.localFile = localFile;
  }

  public String getId() {
    return this.id;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }

  public String getLanguage() {
    return this.language;
  }

  public String getCreator() {
    return this.creator;
  }

  public String getPublisher() {
    return this.publisher;
  }

  public String getFavicon() {
    return this.favicon;
  }

  public String getFaviconMimeType() {
    return this.faviconMimeType;
  }

  public String getDate() {
    return this.date;
  }

  public String getUrl() {
    return this.url;
  }

  public long getArticleCount() {
    return this.articleCount;
  }

  public long getMediaCount() {
    return this.mediaCount;
  }

  public String getName() {
    return this.name;
  }

  public Long getSize() {
    return this.size;
  }

  public int getDownloadId() {
    return this.downloadId;
  }

  public String getFilePath() {
    return localFile.getPath();
  }

  public long tryParseLong(String string) {
    try {
      return Long.parseLong(string);
    } catch (NumberFormatException e) {
      return 0;
    }
  }


  private long getTrueSizeCache() {
    if (trueSize == -1) {
      throw new NullPointerException();
    }
    return trueSize;
  }

  private String getDownloadUrlCache() {
    if (downloadUrl == null) {
      throw new NullPointerException();
    }
    return downloadUrl;
  }

  public Observable<Long> getTrueSize(OkHttpClient okHttpClient) {
    return Observable.create(subscriber -> {
      if (trueSize == -1) {
        Request request = new Request.Builder().url(downloadUrl).head().build();
        Response response = okHttpClient.newCall(request).execute();
        String LengthHeader = response.headers().get("Content-Length");
        trueSize = LengthHeader == null ? 0 : Long.parseLong(LengthHeader);
      }
      subscriber.onNext(trueSize);
      subscriber.onComplete();
    });
  }

  public Observable<String> getDownloadUrl(KiwixLibraryService kiwixLibraryService) {
    return Observable.create(subscriber -> {
      if (downloadUrl == null) {
        downloadUrl = kiwixLibraryService.getMetaLinks(getUrl()).firstElement().blockingGet().getRelevantUrl().getValue();
        downloadUrl = UseHttpOnAndroidVersion4(downloadUrl);
      }
      subscriber.onNext(downloadUrl);
      subscriber.onComplete();
    });
  }

  private String UseHttpOnAndroidVersion4(String sourceUrl) {

    // Simply return the current URL on newer builds of Android
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return sourceUrl;
    }

    // Otherwise replace https with http to bypass Android 4.x devices having older certificates
    // See https://github.com/kiwix/kiwix-android/issues/510 for details
    try {
      URL tempURL = new URL(sourceUrl);
      String androidV4URL = "http" + sourceUrl.substring(tempURL.getProtocol().length());
      Log.d("KiwixDownloadSSL", "replacement_url=" + androidV4URL);
      return androidV4URL;
    } catch (MalformedURLException e) {
      return sourceUrl;
    }
  }

  public List<Chunk> getChunks() {
    if (componentFiles.isEmpty()) {
      if (getSize() <= CHUNK_SIZE) {
        componentFiles.add(new Chunk());
      } else {
        int currentIndex = 0;
        while (getSize() > currentIndex * CHUNK_SIZE) {
          componentFiles.add(new Chunk(currentIndex));
        }
      }
    }
    return componentFiles;
  }

  public NotificationCompat.Builder getNotification(Context context) {
    final Intent target = new Intent(context, KiwixMobileActivity.class);
    target.putExtra(EXTRA_LIBRARY, true);

    PendingIntent openNotificationIntent = PendingIntent.getActivity
        (context, downloadId,
            target, PendingIntent.FLAG_CANCEL_CURRENT);

    Intent pauseIntent = new Intent(context, KiwixDownloadService.class).setAction(PAUSE).putExtra(DOWNLOAD_ID_EXTRA, downloadId);
    Intent stopIntent = new Intent(context, KiwixDownloadService.class).setAction(STOP).putExtra(DOWNLOAD_ID_EXTRA, downloadId);
    PendingIntent pausePending = PendingIntent.getService(context, downloadId, pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    PendingIntent stopPending = PendingIntent.getService(context, downloadId, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    NotificationCompat.Action pauseNotificationAction = new NotificationCompat.Action(R.drawable.ic_pause_black_24dp, context.getString(R.string.download_pause), pausePending);
    NotificationCompat.Action stopNotificationAction = new NotificationCompat.Action(R.drawable.ic_stop_black_24dp, context.getString(R.string.download_stop), stopPending);


    return new NotificationCompat.Builder(context, ONGOING_DOWNLOAD_CHANNEL_ID)
        .setContentTitle(context.getResources().getString(R.string.zim_file_downloading) + " " + getTitle())
        .setProgress(100, 0, false)
        .setSmallIcon(R.drawable.kiwix_notification)
        .setColor(Color.BLACK)
        .setContentIntent(openNotificationIntent)
        .addAction(pauseNotificationAction)
        .addAction(stopNotificationAction)
        .setOngoing(true);
  }

  public class Chunk {

    private final long startByte;
    private final long endByte;
    private final int index;
    private final File chunkFile;
    private final long chunkSize;

    public Chunk() {
      startByte = 0;
      endByte = getTrueSizeCache();
      index = 0;
      chunkFile = localFile;
      chunkSize = endByte - startByte;
    }

    public Chunk(int index) {
      this.index = index;
      this.startByte = index * CHUNK_SIZE;
      this.endByte = Math.min(startByte + CHUNK_SIZE, getTrueSizeCache());
      this.chunkSize = endByte - startByte;
      this.chunkFile = new File(localFile.getPath() + ALPHABET[index % 26] + ALPHABET[index / 26]);
    }

    public Observable<Integer> download(OkHttpClient okHttpClient) throws IOException {
      if (!chunkFile.exists()) {
        chunkFile.createNewFile();
      }
      if (chunkFile.length() != chunkSize) {
        long currentPosition = chunkFile.length();

        RandomAccessFile output = new RandomAccessFile(chunkFile, "rw");
        output.seek(currentPosition);

        String rangeHeader = String.format("bytes=%d-%d", currentPosition, endByte);
        Request request = new Request.Builder().url(getDownloadUrlCache())
            .header("Range", rangeHeader).build();
        Response response = okHttpClient.newCall(request).execute();

        // Check that the server is sending us the right file
        if (Math.abs(endByte - currentPosition - response.body().contentLength()) > 10) {
          throw new IOException("Server broadcasting wrong size");
        }

        BufferedSource bufferedSource = response.body().source();
        return readFromSource(bufferedSource, output)
            .doOnComplete(() -> closeBuffer(bufferedSource))
            .doOnError(error -> closeBuffer(bufferedSource));
      }
      return Observable.create(subscriber -> subscriber.onComplete());
    }

    private void closeBuffer(BufferedSource bufferedSource) {
      if (bufferedSource != null) {
        try {
          bufferedSource.close();
        } catch (IOException e) {

        }
      }
    }

    private Observable<Integer> readFromSource(BufferedSource source, RandomAccessFile output) {
      return Observable.create(subscriber -> {
        byte[] buffer = new byte[32768];
        int readBytes;
        int timeout = 0;
        long downloaded = output.getFilePointer();

        while (output.length() != chunkSize) {
          try {
            while ((readBytes = source.read(buffer)) > 0) {
//              while (KiwixDownloadService.paused) {
//                if (KiwixDownloadService.stopped) {
//                  return;
//                }
//              }
              output.write(buffer, 0, readBytes);
              downloaded += readBytes;
              subscriber.onNext((int) ((100 * downloaded / chunkSize)));
            }
          } catch (Exception e) {
            timeout++;
            try {
              Thread.sleep(1000 * timeout);
            } catch (InterruptedException e1) {
              e1.printStackTrace();
            }
          }
        }
        subscriber.onComplete();
      });
    }
  }

  // Two zims are equal if their ids matchtimeout
  @Override
  public boolean equals (Object obj) {
    if (obj instanceof Zim) {
      if (((Zim) obj).getId() != null && ((Zim) obj).getId().equals(getId())) {
        return true;
      }
    }
    return false;
  }

  // Only use the zim's id to generate a hash code
  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  public void delete() {
    FileUtils.deleteZimFile(localFile);
  }
}
