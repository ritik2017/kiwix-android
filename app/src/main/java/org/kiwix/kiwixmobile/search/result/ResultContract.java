package org.kiwix.kiwixmobile.search.result;

import org.kiwix.kiwixmobile.base.BaseContract;

interface ResultContract {
  interface View extends BaseContract.View<Presenter> {

  }

  interface Presenter extends BaseContract.Presenter<View> {

  }
}
