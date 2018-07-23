package org.kiwix.kiwixmobile.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.base.BaseActivity;
import org.kiwix.kiwixmobile.search.filter.FilterFragment;
import org.kiwix.kiwixmobile.search.result.ResultFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class SearchActivity extends BaseActivity implements SearchContract.View {

  public static final String EXTRA_SEARCH_IN_TEXT = "boolean_search_intent";
  private final ResultFragment resultFragment = new ResultFragment();
  private final FilterFragment filterFragment = new FilterFragment();

  @BindView(R.id.activity_search_toolbar)
  Toolbar toolbar;
  @BindView(R.id.activity_search_view_pager)
  ViewPager viewPager;
  @BindView(R.id.activity_search_tab_layout)
  TabLayout tabLayout;
  @Inject
  SearchContract.Presenter presenter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    presenter.attachView(this);
    setContentView(R.layout.activity_search);

    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    tabLayout.setupWithViewPager(viewPager);
    SearchPagerAdapter searchPagerAdapter = new SearchPagerAdapter(this, filterFragment, resultFragment);
    viewPager.setAdapter(searchPagerAdapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_search, menu);
    MenuItem search = menu.findItem(R.id.menu_search);
    search.expandActionView();
    search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
      }

      @Override
      public boolean onMenuItemActionCollapse(MenuItem item) {
        finish();
        return false;
      }
    });
    ((SearchView) search.getActionView()).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (!"".equals(newText) && newText.length() > 0) {
          presenter.searchArticles(newText, 10);
        }
        return true;
      }
    });
    return true;
  }

  @Override
  public void showResults(List<JNIKiwixSearcher.Result> results) {
    Log.d("SearchActivity", results.size() + "");
    resultFragment.showResults(results);
  }

  @Override
  protected void onDestroy() {
    presenter.detachView();
    super.onDestroy();
  }
}
