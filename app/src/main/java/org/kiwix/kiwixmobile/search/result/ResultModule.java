package org.kiwix.kiwixmobile.search.result;

import org.kiwix.kiwixmobile.di.PerFragment;

import dagger.Module;
import dagger.Provides;

@Module
public class ResultModule {
  @PerFragment
  @Provides
  ResultContract.Presenter provideSearchPresenter(ResultPresenter presenter) {
    return presenter;
  }
}
