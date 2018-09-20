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
package org.kiwix.kiwixmobile.database;


import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Query;

import java.util.List;
import org.kiwix.kiwixmobile.Zim;
import org.kiwix.kiwixmobile.database.entity.LocalZimDatabaseEntity;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Dao class for books
 */

public class LocalZimDao {
  private KiwixDatabase mDb;

  @Inject
  public LocalZimDao(KiwixDatabase kiwixDatabase) {
    this.mDb = kiwixDatabase;
  }
  public ArrayList<Zim> getZims() {
    SquidCursor<LocalZimDatabaseEntity> zimCursor = mDb.query(
        LocalZimDatabaseEntity.class,
        Query.select());
    ArrayList<Zim> zims = new ArrayList<>();
    while (zimCursor.moveToNext()) {
      zims.add(new Zim(zimCursor));
    }
    zimCursor.close();
    return zims;
  }

  public void saveZims(List<Zim> zims) {
    for (Zim zim : zims) {
      if (zim != null) {
        mDb.deleteWhere(LocalZimDatabaseEntity.class, LocalZimDatabaseEntity.ZIM_ID.eq(zim.getId()));
        mDb.persist(zim.getDatabaseEntry());
      }
    }
  }

  public void deleteZim(String id) {
    mDb.deleteWhere(LocalZimDatabaseEntity.class, LocalZimDatabaseEntity.ZIM_ID.eq(id));
  }

  public boolean isDownloaded(Zim zim) {
    SquidCursor<LocalZimDatabaseEntity> zimCursor = mDb.query(
        LocalZimDatabaseEntity.class,
        Query.select().where(LocalZimDatabaseEntity.ZIM_ID.eq(zim.getId())));
    if (zimCursor.moveToNext()) {
      return zimCursor.get(LocalZimDatabaseEntity.DOWNLOADED);
    }
    return false;
  }
}
