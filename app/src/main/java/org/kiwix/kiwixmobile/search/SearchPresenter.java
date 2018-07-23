package org.kiwix.kiwixmobile.search;

import android.util.Log;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.base.BasePresenter;
import org.kiwix.kiwixmobile.data.ZimContentProvider;
import org.kiwix.kiwixmobile.di.PerActivity;
import org.kiwix.kiwixmobile.di.qualifiers.IO;
import org.kiwix.kiwixmobile.di.qualifiers.MainThread;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

@PerActivity
class SearchPresenter extends BasePresenter<SearchContract.View> implements SearchContract.Presenter {

  private static final String TAG = "SearchPresenter";
  private final Scheduler io;
  private final Scheduler mainThread;
  private Disposable disposable;

  @Inject
  SearchPresenter(@IO Scheduler io, @MainThread Scheduler mainThread) {
    this.io = io;
    this.mainThread = mainThread;
  }

  @Override
  public void searchArticles(String query, int numberOfArticles) {
    Single.fromCallable(() -> {
      List<JNIKiwixSearcher.Result> results = new ArrayList<>();
      ZimContentProvider.jniSearcher.search(query, numberOfArticles);
      JNIKiwixSearcher.Result result = ZimContentProvider.jniSearcher.getNextResult();
      while (result != null) {
        results.add(result);
        result = ZimContentProvider.jniSearcher.getNextResult();
      }
      return results;
    })
        .subscribeOn(io)
        .observeOn(mainThread)
        .subscribe(new SingleObserver<List<JNIKiwixSearcher.Result>>() {
          @Override
          public void onSubscribe(Disposable d) {
            if (disposable != null && !disposable.isDisposed()) {
              compositeDisposable.remove(disposable);
            }
            compositeDisposable.add(d);
            disposable = d;
          }

          @Override
          public void onSuccess(List<JNIKiwixSearcher.Result> results) {
            view.showResults(results);
          }

          @Override
          public void onError(Throwable e) {
            Log.e(TAG, "Error finding results:", e);
          }
        });
  }
}
