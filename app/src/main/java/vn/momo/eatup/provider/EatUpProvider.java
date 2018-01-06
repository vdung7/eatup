package vn.momo.eatup.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

/**
 *
 */
public class EatUpProvider extends ContentProvider {
    private static final String t = "EatDb";
    private static final String DATABASE_NAME = "eat.db";
    private static final int DATABASE_VERSION = 1;
    private static final String EATWHAT_TABLE_NAME = "eatup";

    private static HashMap<String, String> eatupProjectionMap;

    private static final int EATWHAT_ITEMS = 1;
    private static final int EATWHAT_ITEM_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context, String dbPath) {
            super(context, dbPath, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onCreateNamed(db, EATWHAT_TABLE_NAME);
        }

        private void onCreateNamed(SQLiteDatabase db, String tableName) {
            db.execSQL("CREATE TABLE " + tableName + " ("
                    + EatUpProviderAPI.EatWhatColumn._ID + " integer primary key, "
                    + EatUpProviderAPI.EatWhatColumn.NAME + " text not null, "
                    + EatUpProviderAPI.EatWhatColumn.LAST_EAT_DATE + " integer not null, "
                    + EatUpProviderAPI.EatWhatColumn.EAT_FOR + " integer, "
                    + EatUpProviderAPI.EatWhatColumn.EAT_TIMES + " integer default 1, "
                    + EatUpProviderAPI.EatWhatColumn.LOCATION + " text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(t, "Successfully upgraded database from version "
                    + oldVersion + " to " + newVersion
                    + ", without destroying all the old data");
        }
    }

    private DatabaseHelper mDbHelper;

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            File dbFile = new File(getContext().getFilesDir(), DATABASE_NAME);
            mDbHelper = new DatabaseHelper(getContext(), dbFile.getAbsolutePath());
        }
        return mDbHelper;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(EATWHAT_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case EATWHAT_ITEMS:
                qb.setProjectionMap(eatupProjectionMap);
                break;

            case EATWHAT_ITEM_ID:
                qb.setProjectionMap(eatupProjectionMap);
                qb.appendWhere(EatUpProviderAPI.EatWhatColumn._ID + "="
                        + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = null;
        try {
            // Get the database and run the query
            SQLiteDatabase db = getDbHelper().getReadableDatabase();
            c = qb.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder);
            // Tell the cursor what uri to watch, so it knows when its source data
            // changes
            c.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case EATWHAT_ITEMS:
                return EatUpProviderAPI.EatWhatColumn.CONTENT_TYPE;

            case EATWHAT_ITEM_ID:
                return EatUpProviderAPI.EatWhatColumn.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = getDbHelper().getWritableDatabase();

        if (sUriMatcher.match(uri) == EATWHAT_ITEMS) {
            long rowId = db.insert(EATWHAT_TABLE_NAME, null, values);
            if (rowId > 0)
                uri = ContentUris.withAppendedId(EatUpProviderAPI.EatWhatColumn.CONTENT_URI, rowId);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    /**
     * This method removes the entry from the content provider, and also removes
     * any associated files. files: form.xml, [formmd5].formdef, formname-media
     * {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case EATWHAT_ITEMS:
                count = db.delete(EATWHAT_TABLE_NAME, where, whereArgs);
                break;
            case EATWHAT_ITEM_ID:
                String itemId = uri.getPathSegments().get(1);
                count = db.delete(
                        EATWHAT_TABLE_NAME,
                        EatUpProviderAPI.EatWhatColumn._ID
                                + "="
                                + itemId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where
                                + ')' : ""), whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case EATWHAT_ITEMS:
                count = db.update(EATWHAT_TABLE_NAME, values, where, whereArgs);
                break;

            case EATWHAT_ITEM_ID:
                String itemId = uri.getPathSegments().get(1);
                Cursor update = this.query(uri, null, where, whereArgs, null);

                // This should only ever return 1 record.
                if (update != null) {
                    if (update.moveToFirst()) {
                        count = db.update(
                                EATWHAT_TABLE_NAME,
                                values,
                                EatUpProviderAPI.EatWhatColumn._ID
                                        + "="
                                        + itemId
                                        + (!TextUtils.isEmpty(where) ? " AND ("
                                        + where + ')' : ""), whereArgs);
                    } else {
                        Log.e(t, "Attempting to update row that does not exist");
                    }
                    update.close();
                } else {
                    Log.e(t, "Attempting to update row that does not exist");
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(EatUpProviderAPI.AUTHORITY, "eatwhat", EATWHAT_ITEMS);
        sUriMatcher.addURI(EatUpProviderAPI.AUTHORITY, "eatwhat/#", EATWHAT_ITEM_ID);

        eatupProjectionMap = new HashMap<>();
        eatupProjectionMap.put(EatUpProviderAPI.EatWhatColumn._ID, EatUpProviderAPI.EatWhatColumn._ID);
        eatupProjectionMap.put(EatUpProviderAPI.EatWhatColumn.NAME, EatUpProviderAPI.EatWhatColumn.NAME);
        eatupProjectionMap.put(EatUpProviderAPI.EatWhatColumn.LOCATION, EatUpProviderAPI.EatWhatColumn.LOCATION);
        eatupProjectionMap.put(EatUpProviderAPI.EatWhatColumn.LAST_EAT_DATE, EatUpProviderAPI.EatWhatColumn.LAST_EAT_DATE);
        eatupProjectionMap.put(EatUpProviderAPI.EatWhatColumn.EAT_TIMES, EatUpProviderAPI.EatWhatColumn.EAT_TIMES);
        eatupProjectionMap.put(EatUpProviderAPI.EatWhatColumn.EAT_FOR, EatUpProviderAPI.EatWhatColumn.EAT_FOR);
    }

}
