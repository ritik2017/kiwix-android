package org.kiwix.kiwixmobile.search.result;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.Item> {

  private static final String TAG = "ResultAdapter";
  private List<JNIKiwixSearcher.Result> results = new ArrayList<>();

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
    Log.d(TAG, "content: " + result.getContent());
    Log.d(TAG, "snipped: " + result.getSnippet());
    Log.d(TAG, "title: " + result.getTitle());
    Log.d(TAG, "url: " + result.getUrl());
    holder.title.setText(result.getTitle());
    holder.description.setText(result.getSnippet());
    holder.content.setText(result.getContent());
  }

  @Override
  public int getItemCount() {
    return results.size();
  }

  class Item extends RecyclerView.ViewHolder {
    @BindView(R.id.item_search_result_favicon)
    ImageView favicon;
    @BindView(R.id.item_search_result_title)
    TextView title;
    @BindView(R.id.item_search_result_description)
    TextView description;
    @BindView(R.id.item_search_result_content)
    TextView content;

    Item(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
