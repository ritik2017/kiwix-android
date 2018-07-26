package org.kiwix.kiwixmobile.search.result;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.base.BaseFragment;
import org.kiwix.kiwixmobile.data.ZimContentProvider;
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity;
import org.kiwix.kiwixmobile.main.MainActivity;
import org.kiwix.kiwixmobile.search.SearchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;
import static org.kiwix.kiwixmobile.utils.Constants.EXTRA_CHOSE_X_URL;

public class ResultFragment extends BaseFragment implements ResultContract.View,
    ResultAdapter.OnItemClickListener {

  private final List<LibraryNetworkEntity.Book> books = new ArrayList<>();
  @BindView(R.id.recycler_view)
  RecyclerView recyclerView;
  private ResultAdapter resultAdapter;
  private SearchActivity searchActivity;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_result, container, false);
    bindViews(root);
    resultAdapter = new ResultAdapter(this, books);
    searchActivity = (SearchActivity) getActivity();
    recyclerView.setAdapter(resultAdapter);
    return root;
  }

  public void showResults(List<JNIKiwixSearcher.Result> results) {
    resultAdapter.showResults(results);
  }

  @Override
  public void onItemClick(JNIKiwixSearcher.Result result) {
    Intent intent = new Intent(searchActivity, MainActivity.class);
    intent.putExtra(EXTRA_CHOSE_X_URL, ZimContentProvider.CONTENT_URI + result.getUrl());

    String path = ZimContentProvider.getZimFilePath(result.getReaderIndex());
    if (!path.equals(ZimContentProvider.getZimFile())) {
      intent.setData(Uri.fromFile(new File(path)));
    }
    if (searchActivity.shouldStartNewActivity()) {
      startActivity(intent);
    } else {
      searchActivity.setResult(RESULT_OK, intent);
    }
    searchActivity.finish();
  }

  public void setBooks(List<LibraryNetworkEntity.Book> books) {
    this.books.clear();
    this.books.addAll(books);
  }
}
