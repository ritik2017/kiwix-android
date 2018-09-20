/*
 * Copyright 2013  Rashiq Ahmad <rashiq.z@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package org.kiwix.kiwixmobile.zim_manager.fileselect_view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.kiwix.kiwixmobile.KiwixApplication;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.Zim;
import org.kiwix.kiwixmobile.ZimContentProvider;
import org.kiwix.kiwixmobile.base.BaseFragment;
import org.kiwix.kiwixmobile.database.LocalZimDao;
import org.kiwix.kiwixmobile.utils.BookUtils;
import org.kiwix.kiwixmobile.utils.LanguageUtils;
import org.kiwix.kiwixmobile.utils.SharedPreferenceUtil;
import org.kiwix.kiwixmobile.zim_manager.ZimManageActivity;

import java.io.File;

import javax.inject.Inject;
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.LocalZimAdapter.ViewHolder;

import static org.kiwix.kiwixmobile.utils.StyleUtils.dialogStyle;

public class ZimFileSelectFragment extends BaseFragment
    implements OnItemClickListener, AdapterView.OnItemLongClickListener, ZimFileSelectViewCallback{

  public RelativeLayout llLayout;
  public SwipeRefreshLayout swipeRefreshLayout;

  private ZimManageActivity zimManageActivity;
  private ListView zimFileListView;
  private TextView fileMessage;

  @Inject ZimFileSelectPresenter presenter;
  @Inject BookUtils bookUtils;
  @Inject SharedPreferenceUtil sharedPreferenceUtil;
  @Inject
  LocalZimDao bookDao;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    KiwixApplication.getApplicationComponent().inject(this);
    zimManageActivity = (ZimManageActivity) super.getActivity();
    presenter.attachView(this, getContext());
    // Replace LinearLayout by the type of the root element of the layout you're trying to load
    llLayout = (RelativeLayout) inflater.inflate(R.layout.zim_list, container, false);
    new LanguageUtils(super.getActivity()).changeFont(super.getActivity().getLayoutInflater(), sharedPreferenceUtil);

    fileMessage = llLayout.findViewById(R.id.file_management_no_files);
    zimFileListView = llLayout.findViewById(R.id.zimfilelist);

    // SwipeRefreshLayout for the list view
    swipeRefreshLayout = llLayout.findViewById(R.id.zim_swiperefresh);
    swipeRefreshLayout.setOnRefreshListener(this::refreshFragment);

    zimFileListView.setOnItemClickListener(this);
    zimFileListView.setOnItemLongClickListener(this);
    // Allow temporary use of ZimContentProvider to query books
    ZimContentProvider.canIterate = true;
    return llLayout; // We must return the loaded Layout
  }

  private void refreshFragment() {
    presenter.showZims();
  }

  @Override
  public void onResume() {
    presenter.showZims();
    super.onResume();
  }

  @Override
  public void setListViewAdapter(LocalZimAdapter localZimAdapter) {
    zimFileListView.setAdapter(localZimAdapter);
  }

  @Override
  public void showNoFilesMessage() {
    fileMessage.setVisibility(View.VISIBLE);
  }

  @Override
  public void hideNoFilesMessage() {
    fileMessage.setVisibility(View.GONE);
  }

  @Override
  public void setRefreshing(boolean b) {
    swipeRefreshLayout.setRefreshing(b);
  }

  @Override
  public void finishActivity() {
    zimManageActivity.finish();
  }

  @Override
  public void runOnUiThread(Runnable runnable) {
    zimManageActivity.runOnUiThread(runnable);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    // Stop file search from accessing content provider potentially opening wrong file
    ZimContentProvider.canIterate = false;

    String file;
    Zim zim = (Zim) zimFileListView.getItemAtPosition(position);
    file = zim.getFilePath();

    if (!new File(file).canRead()) {
      Toast.makeText(zimManageActivity, getString(R.string.error_filenotfound), Toast.LENGTH_LONG).show();
      return;
    }

    zimManageActivity.finishResult(file);
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
    deleteSpecificZimDialog(position);
    return true;
  }

  public void deleteSpecificZimDialog(int position) {
    new AlertDialog.Builder(zimManageActivity, dialogStyle())
        .setMessage(getString(R.string.delete_specific_zim))
        .setPositiveButton(getResources().getString(R.string.delete), (dialog, which) -> {
          if (presenter.deleteSpecificZimFile(position)) {
            Toast.makeText(zimManageActivity, getResources().getString(R.string.delete_specific_zim_toast), Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(zimManageActivity, getResources().getString(R.string.delete_zim_failed), Toast.LENGTH_SHORT).show();
          }
        })
        .setNegativeButton(android.R.string.no, (dialog, which) -> {
          // do nothing
        })
        .show();
  }

  @Override
  public void updateProgressBar(Zim zim, int progress) {
    ViewGroup viewGroup = (ViewGroup) zimFileListView.findViewWithTag(new ViewHolder(zim.getId()));
    if (viewGroup == null) {
      return;
    }
    ProgressBar downloadProgress = viewGroup.findViewById(R.id.downloadProgress);
    downloadProgress.setProgress(progress);
//    TextView timeRemaining = viewGroup.findViewById(R.id.time_remaining);
//    int secLeft = LibraryFragment.mService.timeRemaining.get(mKeys[position], -1);
//    if (secLeft != -1)
//      timeRemaining.setText(toHumanReadableTime(secLeft));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      String permissions[], int[] grantResults) {
      presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }
}
