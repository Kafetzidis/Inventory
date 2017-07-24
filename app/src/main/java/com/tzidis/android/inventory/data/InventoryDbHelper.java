package com.tzidis.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tzidis.android.inventory.data.InventoryContract.ProductsEntry;


public class InventoryDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Inventory.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProductsEntry.TABLE_NAME + " (" +
                    ProductsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ProductsEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                    ProductsEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT," +
                    ProductsEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL," +
                    ProductsEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT," +
                    ProductsEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL," +
                    ProductsEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductsEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
