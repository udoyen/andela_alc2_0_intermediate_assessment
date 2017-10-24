package com.etechbusinesssolutions.android.cryptoapp.cardview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.etechbusinesssolutions.android.cryptoapp.R;
import com.etechbusinesssolutions.android.cryptoapp.conversion.ConversionActivity;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoCurrencyDBHelper;
import com.etechbusinesssolutions.android.cryptoapp.data.CurrencyHelper;

import java.text.DecimalFormat;
import java.util.List;

public class CardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //TODO: Remove
    // For logging
    public static final String LOG_TAG = CardActivity.class.getSimpleName();
    // String to identify intent source
    private static final String ETH_CODE = "eth_value";
    private static final String BTC_CODE = "btc_value";
    // Create a spinners
    Spinner spinner;
    Spinner curSpinner;
    /**
     * Name of the database currency
     * value from the Intent origin.
     */
    String currency_code;
    /**
     * Name of the column for which
     * the Intent originated
     */
    int columnPosition;
    /**
     * Format to use for displayed currencies
     */
    DecimalFormat df = new DecimalFormat("#,###.###");

    // Get the Card currency value
    TextView curValue;

    // The Currency logo
    TextView logoText;

    // Get the cryto currency image
    ImageView cryptImage;


    //Create an instance of CryptoCurrencyDBHelper
    private CryptoCurrencyDBHelper mDBHelper;
    /**
     * Used to check if the spinner is
     * drawn for the first time
     */
    private boolean spinnerClicked = false;
    /**
     * Used to check if the crypto spinner
     * was drawn for the first time
     */
    private boolean curSpinnerClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Bundle extras = getIntent().getExtras();

        // Make sure extra actually captures something from main
        if (extras == null) {
            Log.i(LOG_TAG, "Extras was null ...");
            return;
        }

        currency_code = extras.getString("CURRENCY_CODE");
        columnPosition = extras.getInt("COLUMN_NAME");

        // Instantiate the spinners
        spinner = (Spinner) findViewById(R.id.currency_name_spinner);
        curSpinner = (Spinner) findViewById(R.id.crypt_cur_spinner);


        // Load the spinner data from database
        loadSpinnerData();
        // Load the crypto spinner
        loadCryptoSpinner();

        //TODO: Remove
        Log.i(LOG_TAG, "Column Position id sent here: " + columnPosition);


        // Spinner listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the Card currency value
                curValue = (TextView) findViewById(R.id.card_currency_value);
                // The Currency logo
                logoText = (TextView) findViewById(R.id.card_currency_logo);

                // Get the cryto currency image
                cryptImage = (ImageView) findViewById(R.id.card_crypto_image);


                // Get the item that was selected or clicked
                String code = parent.getItemAtPosition(position).toString();
                //TODO: Remove
                Log.i(LOG_TAG, "Spinner selected code is: " + code);
                Log.i(LOG_TAG, "currency_code is: " + currency_code + " and code is " + code);


                mDBHelper = new CryptoCurrencyDBHelper(getApplicationContext());

                // Check the state of the spinner
                if (!spinnerClicked) {

                    spinner.setSelection(columnPosition - 1);
                    spinnerChecker();

                }

                if (currency_code != null) {
                    //TODO: Remove
                    Log.i(LOG_TAG, "Inside currency_code if block ...");

                    if (currency_code.equals(ETH_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in eth if block spinner...");

                        String value = mDBHelper.getCurrencyValue(code, ETH_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        logoText.setText(CurrencyHelper.getCurrencySymbol(code));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.ethereum);

                    }
                    if (currency_code.equals(BTC_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in btc if block spinner...");

                        String value = mDBHelper.getCurrencyValue(code, BTC_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        logoText.setText(CurrencyHelper.getCurrencySymbol(code));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.bitcoin);

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(columnPosition - 1);
            }
        });

        curSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the Card currency value
                curValue = (TextView) findViewById(R.id.card_currency_value);
                // The Currency logo
                logoText = (TextView) findViewById(R.id.card_currency_logo);

                // Get the crypto currency image
                cryptImage = (ImageView) findViewById(R.id.card_crypto_image);

                // Get the spinner item that is currently selected
                String code = spinner.getSelectedItem().toString();
                String cryptSelected = parent.getItemAtPosition(position).toString();
                //TODO: Remove
                Log.i(LOG_TAG, "code value in curSpinner: " + code);

                mDBHelper = new CryptoCurrencyDBHelper(getApplicationContext());
                //TODO: Remove
                Log.i(LOG_TAG, "cryptoCurSpinnerChecker() called and the value: " + curSpinnerClicked);

                if (!curSpinnerClicked) {
                    //TODO: Remove
                    Log.i(LOG_TAG, "Setting the displayed cryptoCurSpinner ...");

                    if (currency_code != null) {
                        if (currency_code.equals(ETH_CODE)) {
                            //TODO: Remove
                            Log.i(LOG_TAG, "Setting the displayed cryptoCurSpinner to ETH...");
                            curSpinner.setSelection(0);

                        }
                        if (currency_code.equals(BTC_CODE)) {
                            //TODO: Remove
                            Log.i(LOG_TAG, "Setting the displayed cryptoCurSpinner to BTC...");
                            curSpinner.setSelection(1);

                        }


                    }

                    cryptoCurSpinnerChecker();


                }

                if (currency_code != null) {

                    if (cryptSelected.equals(getString(R.string.code_eth_text))) {
                        //TODO: Remove
                        Log.i(LOG_TAG, "Setting eth_ value currency_code to " + cryptSelected);
                        currency_code = "eth_value";
                    }

                    if (cryptSelected.equals(getString(R.string.code_btc_text))) {
                        //TODO: Remove
                        Log.i(LOG_TAG, "Setting btc_ value currency_code to " + cryptSelected);
                        currency_code = "btc_value";
                    }
                }


                //TODO: Remove
                Log.i(LOG_TAG, "cryptoCurSpinnerChecker() called and the value: " + curSpinnerClicked);


                if (currency_code != null) {
                    //TODO: Remove
                    Log.i(LOG_TAG, "Inside currency_code if block of curSpinner... Value of currency_code " + currency_code + "cryptSelected: " + cryptSelected);

                    if (currency_code.equals(ETH_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in eth if block of curSpinner...Value of currency_code " + currency_code);

                        String value = mDBHelper.getCurrencyValue(code, ETH_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.ethereum);

                    }
                    if (currency_code.equals(BTC_CODE)) {

                        //TODO: Remove
                        Log.i(LOG_TAG, "Calling value from database in btc if block of curSpinner...Value of currency_code " + currency_code);

                        String value = mDBHelper.getCurrencyValue(code, BTC_CODE);
                        double num = Double.parseDouble(value);
                        curValue.setText(df.format(num));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.bitcoin);

                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Set up CardView to take user to conversion view
        CardView cardView = (CardView) findViewById(R.id.card_container);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Remove
                Log.i(LOG_TAG, "CardView onCLick event fired ...");
                Toast.makeText(CardActivity.this, "Clicked on CardView", Toast.LENGTH_LONG).show();

                Intent customConversionRate = new Intent(getApplicationContext(), ConversionActivity.class);
                startActivity(customConversionRate);

            }
        });

    }

    /**
     * Loads currency choice spinner
     */
    private void loadSpinnerData() {

        //TODO: Remove
        // For logging
        Log.i(LOG_TAG, "loadSpinnerData() called...");

        mDBHelper = new CryptoCurrencyDBHelper(getApplicationContext());

        // Spinner dropdown elements
        List<String> codes = mDBHelper.getAllCurrencyCodeNames();


        // Create adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, codes);

        // Dropdown layer style
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach dataAdapter to spinner
        spinner.setAdapter(dataAdapter);


    }

    private void loadCryptoSpinner() {

        //TODO: Remove
        // For logging
        Log.i(LOG_TAG, "loadCryptoSpinner() called ...");

        // Create an adapter from the string array resource and use
        // android's inbuilt layout file simple_spinner_item
        // that represents the default spinner in the UI
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.crypto_array, android.R.layout.simple_spinner_item);

        // Set the layout to use for each dropdown item
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        curSpinner.setAdapter(adapter);


    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        // TODO: What should happen when nothing is selected?

    }


    /**
     * Checks the original state
     * of the currency spinner setOnItemSelectedListener
     * event.
     */
    public void spinnerChecker() {

        if (!spinnerClicked) {

            spinnerClicked = true;

        }
    }

    /**
     * Checks the original state
     * of the crypto spinner setOnItemSelectedListener
     * event.
     */
    public void cryptoCurSpinnerChecker() {

        if (!curSpinnerClicked) {

            curSpinnerClicked = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate the menu options from the menu xml file
        //This add menu items to the app bar
        getMenuInflater().inflate(R.menu.network_available, menu);

        return true;
    }


}
