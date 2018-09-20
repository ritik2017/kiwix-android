/*
 * Kiwix Android
 * Copyright (C) 2018  Kiwix <android.kiwix.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kiwix.kiwixmobile.zim_manager.library_view;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import org.kiwix.kiwixmobile.Zim;
import org.kiwix.kiwixmobile.base.BaseFragmentActivityPresenter;
import org.kiwix.kiwixmobile.base.BaseFragmentPresenter;
import org.kiwix.kiwixmobile.database.LocalZimDao;
import org.kiwix.kiwixmobile.downloader.KiwixDownloadService;
import org.kiwix.kiwixmobile.zim_manager.library_view.entity.LibraryNetworkEntity.Book;
import org.kiwix.kiwixmobile.network.KiwixLibraryService;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import org.kiwix.kiwixmobile.utils.NetworkUtils;
import org.kiwix.kiwixmobile.utils.SharedPreferenceUtil;
import org.kiwix.kiwixmobile.utils.StorageUtils;
import org.kiwix.kiwixmobile.zim_manager.ZimManagePresenter;

/**
 * Created by EladKeyshawn on 06/04/2017.
 */

public class LibraryPresenter extends BaseFragmentPresenter<LibraryViewCallback> {

  @Inject
  KiwixLibraryService kiwixLibraryService;

  @Inject
  LocalZimDao localZimDao;

  @Inject
  SharedPreferenceUtil sharedPreferences;

  @Inject
  ConnectivityManager connectivityManager;

  @Inject
  ZimManagePresenter zimManagePresenter;

  @Inject
  public LibraryPresenter() {

  }

  void loadBooks() {
    getMvpView().displayScanningContent();
    kiwixLibraryService.getLibrary()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(library -> getMvpView().showBooks(library.getBooks()), error -> {
          String msg = error.getLocalizedMessage();
          Log.w("kiwixLibrary", "Error loading books:" + (msg != null ? msg : "(null)"));
          getMvpView().displayNoItemsFound();
        });
  }

//  void loadRunningDownloadsFromDb() {
//    for (Zim : bookDao.getDownloadingBooks()) {
//        // TODO: Fix this stuff
//        book.url = book.remoteUrl;
//        downloadFile(book);
//    }
//  }

  public long getSpaceAvailable() {
    return new File(sharedPreferences.getPrefStorage()).getFreeSpace();
  }

  public void zimClick(Book book) {
    if (alreadyDownloaded(book)) {
      getMvpView().displayAlreadyDownloadedToast();
    } else if (!spaceToDownloadZim(book)) {
      getMvpView().displayNoSpaceToast(getSpaceAvailable());
      getMvpView().displayStorageSelectSnackbar();
    } else if (!isConnectedToNetwork()) {
      getMvpView().displayNoNetworkConnection();
    } else if (!networkUsageAuthorised()) {
      getMvpView().displayNetworkConfirmationDialog(() -> downloadFile(book));
    } else {
      downloadFile(book);
    }
  }

  // TODO: Implement
  private void downloadFile(Book book) {
    book.size = "0";
    Intent service = new Intent(getContext(), KiwixDownloadService.class);
    Zim zimToDownload = new Zim(book, new File(sharedPreferences.getPrefStorage() + "/Kiwix/" + StorageUtils.getFileNameFromUrl(book.getUrl())), false);
    service.putExtra(KiwixDownloadService.SELECTED_ZIM, zimToDownload);
    service.setAction(KiwixDownloadService.DOWNLOAD);
    getContext().startService(service);

    getMvpView().displayDownloadStartedToast();
    getMvpView().refreshLibrary();
    zimManagePresenter.addDownloadingZim(zimToDownload);
  }

  private boolean networkUsageAuthorised() {
    return !sharedPreferences.getPrefWifiOnly() || NetworkUtils.isWiFi(getContext());
  }

  private boolean isConnectedToNetwork() {
    NetworkInfo network = connectivityManager.getActiveNetworkInfo();
    return network != null && network.isConnected();
  }

  // TODO: Implement
  private boolean alreadyDownloaded(Book book) {
    return false;
  }

  private boolean spaceToDownloadZim(Book book) {
    return getSpaceAvailable() >= Long.valueOf(book.getSize()) * 1024f;
  }

  public void refreshLibrary() {
    getMvpView().refreshLibrary();
  }

  @Override
  public BaseFragmentActivityPresenter getBaseFragmentActivity() {
    return zimManagePresenter;
  }
}
