package com.etechbusinesssolutions.android.cryptoapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by george on 10/11/17.
 */

class CryptoCurrencyDBHelper extends SQLiteOpenHelper {

    // Used for loggin
    //TODO: Remove
    public static final String LOG_TAG = CryptoCurrencyDBHelper.class.getSimpleName();

    // Name of database
    private static final String DATABASE_NAME = "currency.db";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + CryptoContract.CurrencyEntry.TABLE_NAME;


    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link CryptoCurrencyDBHelper}.
     *
     * @param context of the app
     */
    public CryptoCurrencyDBHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.i(LOG_TAG, "TEST: CrytoCurrencyDBHelper onCreate() called ...");

        // Create a String that contains the SQl statement to create the pets table
        String SQL_CREATE_CURRENCY_TABLE = "CREATE TABLE " + CryptoContract.CurrencyEntry.TABLE_NAME + " ("
                + CryptoContract.CurrencyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME + " TEXT NOT NULL, "
                + CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE + "REAL NOT NULL DEFAULT 0, "
                + CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE + "REAL NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_CURRENCY_TABLE);

    }


    /**
     * This is called when the databse must be updated
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
        // This is only a cache
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

}
