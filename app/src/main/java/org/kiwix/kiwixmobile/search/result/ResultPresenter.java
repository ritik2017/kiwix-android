package org.kiwix.kiwixmobile.search.result;

import org.kiwix.kiwixmobile.base.BasePresenter;
import org.kiwix.kiwixmobile.data.DataSource;
import org.kiwix.kiwixmobile.di.PerFragment;

import javax.inject.Inject;

@PerFragment
class ResultPresenter extends BasePresenter<ResultContract.View> implements ResultContract.Presenter {

  private final DataSource dataSource;

  @Inject
  ResultPresenter(DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
