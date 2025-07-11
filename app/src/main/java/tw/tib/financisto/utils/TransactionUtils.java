/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package tw.tib.financisto.utils;

import android.content.Context;
import android.database.Cursor;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.List;

import tw.tib.financisto.R;
import tw.tib.financisto.adapter.AccountSelectorBalanceAdapter;
import tw.tib.financisto.adapter.CategoryListAdapter;
import tw.tib.financisto.adapter.MyEntityAdapter;
import tw.tib.financisto.db.DatabaseAdapter;
import tw.tib.financisto.db.DatabaseHelper.AccountColumns;
import tw.tib.financisto.db.MyEntityManager;
import tw.tib.financisto.model.Currency;
import tw.tib.financisto.model.MyEntity;
import tw.tib.financisto.model.MyLocation;
import tw.tib.financisto.model.Payee;
import tw.tib.financisto.model.Project;

public class TransactionUtils {

    public static ListAdapter createAccountAdapter(Context context, Cursor accountCursor) {
        return new SimpleCursorAdapter(context, android.R.layout.simple_list_item_activated_1, accountCursor,
                new String[]{"e_"+AccountColumns.TITLE}, new int[]{android.R.id.text1});
    }

    public static ListAdapter createAccountBalanceAdapter(Context context, Cursor accountCursor) {
        return new AccountSelectorBalanceAdapter(context, R.layout.simple_list_item_account, accountCursor);
    }

    public static ListAdapter createAccountMultiChoiceAdapter(Context context, Cursor accountCursor) {
        return new SimpleCursorAdapter(context, android.R.layout.simple_list_item_multiple_choice, accountCursor,
                new String[]{"e_"+AccountColumns.TITLE}, new int[]{android.R.id.text1});
    }

    public static SimpleCursorAdapter createCurrencyAdapter(Context context, Cursor currencyCursor) {
        return new SimpleCursorAdapter(context, android.R.layout.simple_list_item_activated_1, currencyCursor,
                new String[]{"e_name"}, new int[]{android.R.id.text1});
    }

    public static ListAdapter createCategoryAdapter(DatabaseAdapter db, Context context, Cursor categoryCursor) {
        return new CategoryListAdapter(db, context, android.R.layout.simple_list_item_activated_1, categoryCursor);
    }

    public static ListAdapter createCategoryMultiChoiceAdapter(DatabaseAdapter db, Context context, Cursor categoryCursor) {
        return new CategoryListAdapter(db, context, android.R.layout.simple_list_item_multiple_choice, categoryCursor);
    }

    public static ListAdapter createCurrencyAdapter(Context context, List<Currency> currencies) {
        return new MyEntityAdapter<>(context, android.R.layout.simple_list_item_activated_1, android.R.id.text1, currencies);
    }

    public static ListAdapter createLocationAdapter(Context context, Cursor cursor) {
        return new SimpleCursorAdapter(context, android.R.layout.simple_list_item_activated_1, cursor,
                new String[]{"e_name"}, new int[]{android.R.id.text1});
    }

    public static SimpleCursorAdapter createCategoryFilterAdapter(Context context, final DatabaseAdapter db) {
        return new FilterSimpleCursorAdapter<DatabaseAdapter, MyLocation>(context, db, MyLocation.class, "title"){
            @Override
            Cursor getAllRows() {
                return db.getCategories(false);
            }

            @Override
            Cursor filterRows(CharSequence constraint) {
                return db.filterCategories(constraint);
            }
        };
    }

    public static class FilterSimpleCursorAdapter<T extends MyEntityManager, E extends MyEntity> extends SimpleCursorAdapter {
        private final T db;
        private final String filterColumn;
        private final Class<E> entityClass;

        private boolean includeAllRecords = false;

        private long[] includeEntityIds;

        public FilterSimpleCursorAdapter(Context context, final T db, Class<E> entityClass, long... includeEntityIds) {
            this(context, db, entityClass, "e_title", includeEntityIds);
        }

        FilterSimpleCursorAdapter(Context context, final T db, Class<E> entityClass, String filterColumn, long... includeEntityIds) {
            super(context, android.R.layout.simple_dropdown_item_1line, null, new String[]{filterColumn}, new int[]{android.R.id.text1});
            this.db = db;
            this.filterColumn = filterColumn;
            this.entityClass = entityClass;
            this.includeEntityIds = includeEntityIds;
        }

        public void setIncludeAllRecords(boolean includeAllRecords) {
            this.includeAllRecords = includeAllRecords;
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(filterColumn));
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (constraint == null || StringUtil.isEmpty(constraint.toString())) {
                return getAllRows();
            } else {
                return filterRows(constraint);
            }
        }

        Cursor filterRows(CharSequence constraint) {
            if (this.includeAllRecords) {
                return db.filterAllEntities(entityClass, constraint.toString());
            }
            else {
                return db.filterActiveEntities(entityClass, constraint.toString(), includeEntityIds);
            }
        }

        Cursor getAllRows() {
            if (this.includeAllRecords) {
                return db.filterAllEntities(entityClass, null);
            }
            else {
                return db.filterActiveEntities(entityClass, null, includeEntityIds);
            }
        }
    }
}
