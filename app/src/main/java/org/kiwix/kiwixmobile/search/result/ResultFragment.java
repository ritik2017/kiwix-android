package org.kiwix.kiwixmobile.search.result;

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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResultFragment extends BaseFragment implements ResultContract.View {

  private final ResultAdapter resultAdapter = new ResultAdapter();

  @BindView(R.id.recycler_view)
  RecyclerView recyclerView;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_result, container, false);
    ButterKnife.bind(this, root);
    recyclerView.setAdapter(resultAdapter);
    return root;
  }

  public void showResults(List<JNIKiwixSearcher.Result> results) {
    resultAdapter.showResults(results);
  }
}
