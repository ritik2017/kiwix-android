package org.kiwix.kiwixmobile.search;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.search.filter.FilterFragment;
import org.kiwix.kiwixmobile.search.result.ResultFragment;

class SearchPagerAdapter extends FragmentPagerAdapter {

  private final SearchActivity activity;
  private final FilterFragment filterFragment;
  private final ResultFragment resultFragment;

  SearchPagerAdapter(SearchActivity activity,
                     FilterFragment filterFragment,
                     ResultFragment resultFragment) {
    super(activity.getSupportFragmentManager());
    this.activity = activity;
    this.filterFragment = filterFragment;
    this.resultFragment = resultFragment;
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return resultFragment;
      case 1:
        return filterFragment;
    }
    return null;
  }

  @Override
  public int getCount() {
    return 2;
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    switch (position) {
      case 0:
        activity.getString(R.string.results);
      case 1:
        activity.getString(R.string.filter);
    }
    return null;
  }
}
