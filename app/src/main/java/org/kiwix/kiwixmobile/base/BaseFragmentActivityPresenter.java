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
package org.kiwix.kiwixmobile.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import dagger.android.AndroidInjection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseFragmentActivityPresenter<T extends ViewCallback> extends BasePresenter<T> {

  private Map<Class, BaseFragmentPresenter> presenterMap = new HashMap<>();

  public <T extends ViewCallback> void attachPresenter(BaseFragmentPresenter<T> tBaseFragmentPresenter) {
    presenterMap.put(tBaseFragmentPresenter.getClass(), tBaseFragmentPresenter);
  }

  public BaseFragmentPresenter getFragmentPresenter(Class className) {
    return presenterMap.get(className);
  }
}
