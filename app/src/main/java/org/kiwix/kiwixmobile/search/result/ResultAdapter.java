package org.kiwix.kiwixmobile.search.result;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.data.ZimContentProvider;
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static org.kiwix.kiwixmobile.library.LibraryAdapter.createBitmapFromEncodedString;
import static org.kiwix.kiwixmobile.utils.StyleUtils.fromHtml;

class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.Item> {

  private static final String TAG = "ResultAdapter";
  private static final String HTML_IMAGE_TAG = "img src=\"";
  private final OnItemClickListener onItemClickListener;
  private final List<LibraryNetworkEntity.Book> books;
  private List<JNIKiwixSearcher.Result> results = new ArrayList<>();

  ResultAdapter(OnItemClickListener onItemClickListener, List<LibraryNetworkEntity.Book> books) {
    this.onItemClickListener = onItemClickListener;
    this.books = books;
  }

  void showResults(List<JNIKiwixSearcher.Result> results) {
    this.results = results;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Item onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new Item(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_search_result, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull Item holder, int position) {
    JNIKiwixSearcher.Result result = results.get(position);
    Log.d(TAG, "reader index: " + result.getReaderIndex());
    Log.d(TAG, "title: " + result.getTitle());
    Log.d(TAG, "url: " + result.getUrl());
    holder.title.setText(fromHtml(result.getTitle()));
    holder.description.setText(fromHtml(result.getSnippet()));
    holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(result));

    /*
    TODO
    This is a work around to show the first image of the page from the currently opened zim file.
    Images from other zim files are not shown, therefore, the corresponding favicon is shown.
    Find a way to show the first image from other zim files.
     */
    String content = result.getContent();
    int start = content.indexOf(HTML_IMAGE_TAG);
    if (ZimContentProvider.getZimFilePath(result.getReaderIndex()).equals(ZimContentProvider.getZimFile()) &&
        start >= 0) {
      start = content.indexOf("/", start) + 1;
      holder.favicon.setImageURI(Uri.parse((ZimContentProvider.CONTENT_URI +
          content.substring(start, content.indexOf("\"", start)))));
    } else {
      Observable.fromIterable(books)
          .filter(book -> ZimContentProvider.getZimFilePath(result.getReaderIndex())
              .equals(book.file.getPath()))
          .subscribeOn(Schedulers.computation())
          .firstOrError()
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new SingleObserver<LibraryNetworkEntity.Book>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(LibraryNetworkEntity.Book book) {
              holder.favicon.setImageBitmap(createBitmapFromEncodedString(book.getFavicon(),
                  holder.favicon.getContext()));
            }

            @Override
            public void onError(Throwable e) {
              Log.d(TAG, "Unable to load favicon", e);
              holder.favicon.setImageResource(R.mipmap.ic_kiwix_foreground);
            }
          });
    }
  }

  @Override
  public int getItemCount() {
    return results.size();
  }

  interface OnItemClickListener {
    void onItemClick(JNIKiwixSearcher.Result result);
  }

  class Item extends RecyclerView.ViewHolder {
    @BindView(R.id.item_search_result_favicon)
    ImageView favicon;
    @BindView(R.id.item_search_result_title)
    TextView title;
    @BindView(R.id.item_search_result_description)
    TextView description;

    Item(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
