package com.etechbusinesssolutions.android.cryptoapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.etechbusinesssolutions.android.cryptoapp.data.CryptoContract;
import com.etechbusinesssolutions.android.cryptoapp.data.CurrencyHelper;

import java.text.DecimalFormat;

/**
 * Created by george on 10/17/17.
 */

public class EthCurrencyAdapter extends CursorAdapter {
    /**
     * Format to use for displaying currencies
     */
    private DecimalFormat df = new DecimalFormat("#,###.###");

    public EthCurrencyAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    // The newView method id used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.currency_list_item, parent, false);
    }

    // The bindView methos is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView curCode = view.findViewById(R.id.currency_code);
        TextView curValue = view.findViewById(R.id.rate);
        TextView curSymbol = view.findViewById(R.id.currency_symbol);

        // FInd the columns of currency index we want
        int nameColumnIndex = cursor.getColumnIndex(CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME);
        int currencyValueIndex = cursor.getColumnIndex(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE);


        // Read the pet attribute from the Cursor for the current currency
        double cValue = cursor.getDouble(currencyValueIndex);
        String cName = cursor.getString(nameColumnIndex);


        // Populate fields with extracted properties
        curCode.setText(cName);
        curValue.setText(df.format(cValue));

        // Set the symbol of the currency
        // using the CurrencyHelper class static method getCurrencySymbol
        curSymbol.setText(CurrencyHelper.getCurrencySymbol(cName));


    }
}
