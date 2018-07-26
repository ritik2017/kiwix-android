package org.kiwix.kiwixmobile.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * All fragments should inherit from this fragment.
 */

public abstract class BaseFragment extends Fragment {

  private Unbinder unbinder;

  @Override
  public void onAttach(Context context) {
    AndroidSupportInjection.inject(this);
    super.onAttach(context);
  }

  protected void bindViews(View view) {
    unbinder = ButterKnife.bind(this, view);
  }

  @Override
  public void onDestroyView() {
    if (unbinder != null) {
      unbinder.unbind();
    }
    super.onDestroyView();
  }
}
