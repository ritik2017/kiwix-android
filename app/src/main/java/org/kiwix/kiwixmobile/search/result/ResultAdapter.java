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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.kiwix.kiwixmobile.utils.StyleUtils.fromHtml;

class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.Item> {

  private static final String TAG = "ResultAdapter";
  private static final String HTML_IMAGE_TAG = "img src=\"";
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
    holder.title.setText(fromHtml(result.getTitle()));
    holder.description.setText(fromHtml(result.getSnippet()));
    String content = result.getContent();
    int start = content.indexOf(HTML_IMAGE_TAG);
    if (start >= 0) {
      start = content.indexOf("/", start) + 1;
      holder.favicon.setImageURI(Uri.parse((ZimContentProvider.CONTENT_URI +
          content.substring(start, content.indexOf("\"", start)))));
    }
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

    Item(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
