package com.etechbusinesssolutions.android.cryptoapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by george on 10/16/17.
 */

public class CurrencyProvider extends ContentProvider {


    // URI matcher code for the content URI for currency table
    private static final int CURRENCY = 100;
    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int CURRENCY_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.


        sUriMatcher.addURI(CryptoContract.CONTENT_AUTHORITY, CryptoContract.PATH_CURRENCY, CURRENCY);
        sUriMatcher.addURI(CryptoContract.CONTENT_AUTHORITY, CryptoContract.PATH_CURRENCY + "/#", CURRENCY);
    }

    // Database helper that will provide access to the database
    private CryptoCurrencyDBHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new CryptoCurrencyDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();


        // this cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CURRENCY:
                // For the CURRENCY code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the CURRENCY table
                cursor = database.query(CryptoContract.CurrencyEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CURRENCY_ID:
                // For the CURRENCY_ID code, extract out the ID from the URI,
                // For an example URI such as "content://com.example.android.pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of # in this case.
                //
                // For entry "?" in the selection, we need to have an element in the selection
                // argument that will fill in the "?". Since we have 1 question mark in the
                // selection we have 1 String in the selection arguments' String array.
                selection = CryptoContract.CurrencyEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(CryptoContract.CurrencyEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        // Set notification URI on the Cursor,
        // so we know what content URI the cursor was created for.
        // If the data at this URI changes, then we need to update the Cursor.

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CURRENCY:
                return insertCurrency(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertCurrency(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Currency requires a name");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Insert new currency with the given values
        long id = database.insert(CryptoContract.CurrencyEntry.TABLE_NAME, null, values);
        // If the ID is -1, then insertion failed. Log an error and return null.
        if (id == -1) {

            return null;
        }

        // Notify all listeners that the data has changed for the currency content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CURRENCY:
                return updateCurrency(uri, contentValues, selection, selectionArgs);
            case CURRENCY_ID:
                // For the CURRENCY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = CryptoContract.CurrencyEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCurrency(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);

        }

    }

    private int updateCurrency(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link CryptoContract.CurrencyEntry#COLUMN_BTC_VALUE} key is present,
        // check that the name value is not null.
        if (values.containsKey(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE)) {
            String name = values.getAsString(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE);
            if (name == null) {
                throw new IllegalArgumentException("Currency btc requires a value");
            }
        }

        // If the {@link CryptoContract.CurrencyEntry#COLUMN_ETH_VALUE} key is present,
        // check that the name value is not null.
        if (values.containsKey(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE)) {
            String name = values.getAsString(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE);
            if (name == null) {
                throw new IllegalArgumentException("Currency eth requires a value");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }


        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowUpdated = database.update(CryptoContract.CurrencyEntry.TABLE_NAME, values, selection, selectionArgs);

        // If I or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowUpdated != 0) {
            // Notify all listeners that the data has changed for the currency content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdated;


    }
}
