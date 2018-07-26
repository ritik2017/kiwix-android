package org.kiwix.kiwixmobile.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.kiwix.kiwixlib.JNIKiwixSearcher;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.base.BaseActivity;
import org.kiwix.kiwixmobile.library.entity.LibraryNetworkEntity;
import org.kiwix.kiwixmobile.main.MainActivity;
import org.kiwix.kiwixmobile.search.filter.FilterFragment;
import org.kiwix.kiwixmobile.search.result.ResultFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class SearchActivity extends BaseActivity implements SearchContract.View {

  public static final String EXTRA_FIND_IN_PAGE = "findInPage";
  private final ResultFragment resultFragment = new ResultFragment();
  private final FilterFragment filterFragment = new FilterFragment();
  private final List<LibraryNetworkEntity.Book> books = new ArrayList<>();

  @BindView(R.id.activity_search_toolbar)
  Toolbar toolbar;
  @BindView(R.id.activity_search_view_pager)
  ViewPager viewPager;
  @BindView(R.id.activity_search_tab_layout)
  TabLayout tabLayout;
  @Inject
  SearchContract.Presenter presenter;

  private SearchView searchView;

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

    presenter.loadBooks();
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
    searchView = (SearchView) search.getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (!"".equals(newText) && newText.length() > 0) {
          presenter.searchArticles(newText, 200);
        }
        return true;
      }
    });
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_find_in_page:
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_FIND_IN_PAGE, searchView.getQuery() + "");
        if (shouldStartNewActivity()) {
          startActivity(intent);
        } else {
          setResult(RESULT_OK, intent);
        }
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void showResults(List<JNIKiwixSearcher.Result> results) {
    resultFragment.showResults(results);
  }

  @Override
  public void setBooks(List<LibraryNetworkEntity.Book> books) {
    this.books.clear();
    this.books.addAll(books);
    resultFragment.setBooks(books);
  }

  @Override
  protected void onDestroy() {
    presenter.detachView();
    super.onDestroy();
  }
}
