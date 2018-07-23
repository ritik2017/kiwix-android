package org.kiwix.kiwixmobile.search;

import org.kiwix.kiwixmobile.di.PerActivity;
import org.kiwix.kiwixmobile.di.PerFragment;
import org.kiwix.kiwixmobile.search.filter.FilterFragment;
import org.kiwix.kiwixmobile.search.result.ResultFragment;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SearchModule {
  @PerActivity
  @Provides
  static SearchContract.Presenter provideSearchPresenter(SearchPresenter presenter) {
    return presenter;
  }

  @PerFragment
  @ContributesAndroidInjector
  abstract FilterFragment provideFilterFragment();

  @PerFragment
  @ContributesAndroidInjector
  abstract ResultFragment provideResultFragment();
}
