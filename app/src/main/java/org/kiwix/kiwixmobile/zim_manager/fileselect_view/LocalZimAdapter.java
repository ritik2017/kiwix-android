package org.kiwix.kiwixmobile.zim_manager.fileselect_view;

import static org.kiwix.kiwixmobile.utils.NetworkUtils.parseURL;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.KiwixApplication;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.Zim;
import org.kiwix.kiwixmobile.zim_manager.library_view.LibraryAdapter;
import org.kiwix.kiwixmobile.utils.BookUtils;

public class LocalZimAdapter extends ArrayAdapter<Zim> {


  @Inject
  BookUtils bookUtils;

  // The Adapter for the ListView for when the ListView is populated with the rescanned files
  public LocalZimAdapter(Context context, int textViewResourceId, List<Zim> zims) {
    super(context, textViewResourceId, zims);
    KiwixApplication.getApplicationComponent().inject(this);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    ViewHolder holder;
    Zim zim = getItem(position);
    if (convertView == null) {
      convertView = View.inflate(getContext(), R.layout.library_item, null);
      holder = new ViewHolder();
      holder.title = convertView.findViewById(R.id.title);
      holder.description = convertView.findViewById(R.id.description);
      holder.language = convertView.findViewById(R.id.language);
      holder.creator = convertView.findViewById(R.id.creator);
      holder.publisher = convertView.findViewById(R.id.publisher);
      holder.date = convertView.findViewById(R.id.date);
      holder.size = convertView.findViewById(R.id.size);
      holder.fileName = convertView.findViewById(R.id.fileName);
      holder.favicon = convertView.findViewById(R.id.favicon);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (zim == null) {
      return convertView;
    }

    holder.id = zim.getId();
    holder.title.setText(zim.getTitle());
    holder.description.setText(zim.getDescription());
    holder.language.setText(bookUtils.getLanguage(zim.getLanguage()));
    holder.creator.setText(zim.getCreator());
    holder.publisher.setText(zim.getPublisher());
    holder.date.setText(zim.getDate());
    holder.size.setText(LibraryAdapter.createGbString((zim.getSize())));
    holder.fileName.setText(parseURL(getContext(), zim.getFilePath()));
    holder.favicon.setImageBitmap(
        LibraryAdapter.createBitmapFromEncodedString(zim.getFavicon(), getContext()));

    //// Check if no value is empty. Set the view to View.GONE, if it is. To View.VISIBLE, if not.
    if (zim.getTitle() == null || zim.getTitle().isEmpty()) {
      holder.title.setVisibility(View.GONE);
    } else {
      holder.title.setVisibility(View.VISIBLE);
    }

    if (zim.getDescription() == null || zim.getDescription().isEmpty()) {
      holder.description.setVisibility(View.GONE);
    } else {
      holder.description.setVisibility(View.VISIBLE);
    }

    if (zim.getCreator() == null || zim.getCreator().isEmpty()) {
      holder.creator.setVisibility(View.GONE);
    } else {
      holder.creator.setVisibility(View.VISIBLE);
    }

    if (zim.getPublisher() == null || zim.getPublisher().isEmpty()) {
      holder.publisher.setVisibility(View.GONE);
    } else {
      holder.publisher.setVisibility(View.VISIBLE);
    }

    if (zim.getDate() == null || zim.getDate().isEmpty()) {
      holder.date.setVisibility(View.GONE);
    } else {
      holder.date.setVisibility(View.VISIBLE);
    }

    if (zim.getSize() == null || zim.getSize() == 0) {
      holder.size.setVisibility(View.GONE);
    } else {
      holder.size.setVisibility(View.VISIBLE);
    }

    if (!zim.isDownloaded()) {
      holder.description.setText("Downloading");
      convertView.findViewById(R.id.downloadProgress).setVisibility(View.VISIBLE);
      convertView.findViewById(R.id.pause).setVisibility(View.VISIBLE);
      convertView.findViewById(R.id.stop).setVisibility(View.VISIBLE);
    } else {
      convertView.findViewById(R.id.downloadProgress).setVisibility(View.GONE);
      convertView.findViewById(R.id.pause).setVisibility(View.GONE);
      convertView.findViewById(R.id.stop).setVisibility(View.GONE);
    }

    return convertView;

  }

  public static class ViewHolder {

    public ViewHolder() {

    }

    public ViewHolder(String id) {
      this.id = id;
    }

    public String id;
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ViewHolder && ((ViewHolder) obj).id != null) {
        return ((ViewHolder) obj).id.equals(id);
      }
      return false;
    }

    TextView title;

    TextView description;

    TextView language;

    TextView creator;

    TextView publisher;

    TextView date;

    TextView size;

    TextView fileName;

    ImageView favicon;
  }
}
