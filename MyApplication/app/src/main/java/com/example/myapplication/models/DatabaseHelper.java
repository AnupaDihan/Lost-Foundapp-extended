package com.example.myapplication.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lostandfound.db";
    private static final int DATABASE_VERSION = 3; // Increment version
    private static final String TABLE_NAME = "items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_CONTACT = "contact_info";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_IMAGE = "image_path";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_RESOLVED = "is_resolved";
    // New columns for geo features
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_TYPE + " TEXT NOT NULL, " +
                COLUMN_LOCATION + " TEXT NOT NULL, " +
                COLUMN_CONTACT + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_IMAGE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                COLUMN_RESOLVED + " INTEGER DEFAULT 0, " +
                COLUMN_LATITUDE + " REAL DEFAULT 0, " +
                COLUMN_LONGITUDE + " REAL DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add new columns for existing database
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LATITUDE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL DEFAULT 0");
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public long insertItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_DESCRIPTION, item.getDescription());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_TYPE, item.getType());
        values.put(COLUMN_LOCATION, item.getLocation());
        values.put(COLUMN_CONTACT, item.getContactInfo());
        values.put(COLUMN_DATE, item.getDate());
        values.put(COLUMN_IMAGE, item.getImagePath());
        values.put(COLUMN_TIMESTAMP, item.getTimestamp());
        values.put(COLUMN_RESOLVED, item.isResolved() ? 1 : 0);
        values.put(COLUMN_LATITUDE, item.getLatitude());
        values.put(COLUMN_LONGITUDE, item.getLongitude());
        return db.insert(TABLE_NAME, null, values);
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    items.add(extractItemFromCursor(cursor));
                }
            } finally {
                cursor.close();
            }
        }
        return items;
    }

    public Item getItemById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return extractItemFromCursor(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public int deleteItem(long id) {
        return this.getWritableDatabase().delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateItemResolved(long id, boolean resolved) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESOLVED, resolved ? 1 : 0);
        return this.getWritableDatabase().update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private Item extractItemFromCursor(Cursor cursor) {
        return new Item(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESOLVED)) == 1,
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
        );
    }
}