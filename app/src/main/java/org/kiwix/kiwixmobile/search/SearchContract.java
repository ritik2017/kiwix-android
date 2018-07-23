package org.kiwix.kiwixmobile.search;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.base.BaseContract;

import java.util.List;

interface SearchContract {
  interface View extends BaseContract.View<Presenter> {
    void showResults(List<JNIKiwixSearcher.Result> results);
  }

  interface Presenter extends BaseContract.Presenter<View> {
    void searchArticles(String query, int numberOfArticles);
  }
}
