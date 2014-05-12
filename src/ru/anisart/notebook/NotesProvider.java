package ru.anisart.notebook;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NotesProvider extends ContentProvider {

    private static final String TAG = NotesProvider.class.getSimpleName();
    public static final String PROVIDER_NAME = "ru.anisart.notebook.notes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/notes" );
    private static final int URI_NOTES = 1;
    private static final int URI_NOTES_ID = 2;

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "notes";

    public static final String TABLE_NAME = "notes";
    public static final String ROW_ID = "_id";
    public static final String TEXT_CONTENT = "text_content";
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( "
            + ROW_ID + " integer primary key autoincrement, "
            + TEXT_CONTENT + " TEXT)";

    static final String NOTES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + PROVIDER_NAME;
    static final String NOTES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + PROVIDER_NAME;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "notes", URI_NOTES);
        uriMatcher.addURI(PROVIDER_NAME, "notes/#", URI_NOTES_ID);
    }

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_NOTES:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ROW_ID + " ASC";
                }
                break;
            case URI_NOTES_ID:
                String id = uri.getLastPathSegment();
                Log.d(TAG, "URI_NOTES_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ROW_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ROW_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_NOTES:
                return NOTES_CONTENT_TYPE;
            case URI_NOTES_ID:
                return NOTES_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert, " + uri.toString());
        if (uriMatcher.match(uri) != URI_NOTES)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_NOTES:
                Log.d(TAG, "URI_NOTES");
                break;
            case URI_NOTES_ID:
                String id = uri.getLastPathSegment();
                Log.d(TAG, "URI_NOTES_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ROW_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ROW_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int count = db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_NOTES:
                Log.d(TAG, "URI_NOTES");

                break;
            case URI_NOTES_ID:
                String id = uri.getLastPathSegment();
                Log.d(TAG, "URI_NOTES_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ROW_ID + " = " + id;
                } else {
                    selection = selection + " AND " + ROW_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int count = db.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }
}
