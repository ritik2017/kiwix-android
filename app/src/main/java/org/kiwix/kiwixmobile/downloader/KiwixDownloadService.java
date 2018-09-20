package org.kiwix.kiwixmobile.downloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import org.kiwix.kiwixmobile.KiwixApplication;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.Zim;
import org.kiwix.kiwixmobile.database.LocalZimDao;
import org.kiwix.kiwixmobile.network.KiwixLibraryService;
import org.kiwix.kiwixmobile.utils.Constants;
import org.kiwix.kiwixmobile.utils.SharedPreferenceUtil;
import org.kiwix.kiwixmobile.zim_manager.ZimManagePresenter;

public class KiwixDownloadService extends Service {

  public static final String SELECTED_ZIM = "zim_to_download";
  public static final String DOWNLOAD = "download";
  public static final String PAUSE = "pause";
  public static final String PLAY = "play";
  public static final String STOP = "stop";
  private static KiwixDownloadService kiwixDownloadService;
  private final DownloadExecutor downloadExecutor = new DownloadExecutor(5);

  private final Map<Integer, Zim> downloadingZims = new HashMap<>();
  private final Map<Zim, NotificationCompat.Builder> notifications = new HashMap<>();
  public static boolean paused;
  public static boolean stopped;

  @Inject
  SharedPreferenceUtil sharedPreferenceUtil;

  @Inject
  OkHttpClient okHttpClient;

  @Inject
  KiwixLibraryService kiwixLibraryService;

  @Inject
  NotificationManager notificationManager;

  @Inject
  LocalZimDao localZimDao;

  @Inject
  ZimManagePresenter zimManagePresenter;

  private static KiwixDownloadService getRunningService() {
    return kiwixDownloadService;
  }


  @Override
  public void onCreate() {
    KiwixApplication.getApplicationComponent().inject(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent.getAction().equals(DOWNLOAD)) {
      Zim zim = (Zim) intent.getSerializableExtra(SELECTED_ZIM);
      if (downloadingZims.keySet().contains(zim)) {
        return START_NOT_STICKY;
      }
      startDownload(zim);
      return START_STICKY;
    } else if (intent.getAction().equals(STOP)) {
      paused = true;
      stopped = true;
    } else if (intent.getAction().equals(PAUSE)) {
      paused = !paused;
    }


    return START_NOT_STICKY;
  }

  private void startDownload(Zim zim) {
    if (getRunningService() == null) {
      kiwixDownloadService = this;
      createOngoingDownloadChannel();
    } else if (this == kiwixDownloadService) {

    } else {
      kiwixDownloadService.startDownload(zim);
      return;
    }

    notifications.put(zim, zim.getNotification(this));
    notificationManager.notify(zim.getDownloadId(), notifications.get(zim).build());

    zim.getDownloadUrl(kiwixLibraryService)
        .subscribeOn(Schedulers.from(downloadExecutor))
        .flatMap(u -> zim.calculateSize(okHttpClient))
        .flatMap(u -> io.reactivex.Observable.fromIterable(zim.getChunks()))
        .concatMap(c -> c.download(okHttpClient))
        .distinctUntilChanged().doOnComplete(() -> {
          notificationManager.cancel(zim.getDownloadId());
          notificationManager.notify(zim.getDownloadId(), notifications.get(zim).setOngoing(false).setProgress(0, 0, false).build());
          zimManagePresenter.completeDownload(zim);
        })
        .subscribe(progress -> {
          notificationManager.notify(zim.getDownloadId(), notifications.get(zim).setProgress(100, progress, false).build());
          zimManagePresenter.setProgress(zim, progress);
        });
  }

  /**
   * Creates and registers notification channel with system for notifications of
   * type: download in progress.
   */
  private void createOngoingDownloadChannel () {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = getString(R.string.ongoing_download_channel_name);
      String description = getString(R.string.ongoing_download_channel_desc);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel ongoingDownloadsChannel = new NotificationChannel(
          Constants.ONGOING_DOWNLOAD_CHANNEL_ID, name, importance);
      ongoingDownloadsChannel.setDescription(description);
      ongoingDownloadsChannel.setSound(null, null);
      NotificationManager notificationManager = (NotificationManager) getSystemService(
          NOTIFICATION_SERVICE);
      notificationManager.createNotificationChannel(ongoingDownloadsChannel);
    }
  }


  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}