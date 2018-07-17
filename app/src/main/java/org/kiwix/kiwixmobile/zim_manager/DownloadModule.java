package org.kiwix.kiwixmobile.zim_manager;

import org.kiwix.kiwixmobile.di.PerFragment;
import org.kiwix.kiwixmobile.downloader.DownloadFragment;
import org.kiwix.kiwixmobile.zim_manager.fileselect_view.ZimFileSelectFragment;
import org.kiwix.kiwixmobile.zim_manager.library_view.LibraryFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DownloadModule {
  @PerFragment
  @ContributesAndroidInjector
  abstract LibraryFragment provideLibraryFragment();

  @PerFragment
  @ContributesAndroidInjector
  abstract DownloadFragment provideDownloadFragment();

  @PerFragment
  @ContributesAndroidInjector
  abstract ZimFileSelectFragment provideZimFileSelectFragment();
}
