package org.kiwix.kiwixmobile.search;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.base.BaseContract;
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity;

import java.util.List;

interface SearchContract {
  interface View extends BaseContract.View<Presenter> {
    void showResults(List<JNIKiwixSearcher.Result> results);

    void setBooks(List<LibraryNetworkEntity.Book> books);
  }

  interface Presenter extends BaseContract.Presenter<View> {
    void searchArticles(String query, int numberOfArticles);

    void loadBooks();
  }
}
