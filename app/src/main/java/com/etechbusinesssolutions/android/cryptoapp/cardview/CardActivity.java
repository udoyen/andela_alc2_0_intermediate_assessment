package com.etechbusinesssolutions.android.cryptoapp.cardview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.etechbusinesssolutions.android.cryptoapp.R;
import com.etechbusinesssolutions.android.cryptoapp.conversion.ConversionActivity;
import com.etechbusinesssolutions.android.cryptoapp.cryptservice.JobSchedulerService;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoContract;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoCurrencyDBHelper;
import com.etechbusinesssolutions.android.cryptoapp.data.CurrencyHelper;
import com.etechbusinesssolutions.android.cryptoapp.networkutil.NetworkUtil;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class CardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    // String to identify intent source
    private static final String ETH_CODE = "eth_value";
    private static final String BTC_CODE = "btc_value";
    private static final String CONNECTION_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";

    /**
     * JobScheduler Job ID
     */
    private static final int JOB_ID = 1;
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
    /**
     * Create an instance of the JobScheduler class
     */
    JobScheduler mJobScheduler;

    /**
     * Used to set the menu items
     */
    Menu menu = null;
    /**
     * Used to check network status
     */
    String status;

    /**
     * Used to check network status
     */
    boolean online;


    /**
     * Use this to catch the intent sent from the JobSchedulerService class
     */
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {

            // Set the network menu status
            if (Objects.equals(intent.getAction(), CONNECTION_INTENT)) {

                status = NetworkUtil.getConnectivityStatusString(context);
                online = (Objects.equals(status, "Wifi enabled") || Objects.equals(status, "Mobile data enabled"));
                supportInvalidateOptionsMenu();

            }
        }
    };
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


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // Register the intent here
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTION_INTENT);
        registerReceiver(this.broadcastReceiver, intentFilter);

        // Initialize JobScheduler
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(new JobInfo.Builder(JOB_ID,
                new ComponentName(this, JobSchedulerService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(30000)
                .build());

        Bundle extras = getIntent().getExtras();

        // Make sure extra actually captures something from main
        if (extras == null) {

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


                mDBHelper = new CryptoCurrencyDBHelper(getApplicationContext());

                // Check the state of the spinner
                if (!spinnerClicked) {

                    spinner.setSelection(columnPosition - 1);
                    spinnerChecker();

                }

                if (currency_code != null) {


                    if (currency_code.equals(ETH_CODE)) {

                        double ethValue = 0;


                        //String value = mDBHelper.getCurrencyValue(code, ETH_CODE);
                        String[] projection = {
                                CryptoContract.CurrencyEntry._ID,
                                CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE
                        };
                        Cursor cursor = getApplicationContext().getContentResolver().query(CryptoContract.CurrencyEntry.CONTENT_URI,
                                projection,
                                "cur_name = ?",
                                new String[]{code},
                                null);
                        assert cursor != null;
                        int currencyValueIndex = cursor.getColumnIndex(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE);
                        if (cursor.moveToFirst()) {
                            ethValue = cursor.getDouble(currencyValueIndex);
                        }

                        curValue.setText(df.format(ethValue));
                        logoText.setText(CurrencyHelper.getCurrencySymbol(code));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.ethereum);
                        cursor.close();

                    }
                    if (currency_code.equals(BTC_CODE)) {

                        double btcValue = 0;

                        String[] projection = {
                                CryptoContract.CurrencyEntry._ID,
                                CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE
                        };
                        Cursor cursor = getApplicationContext().getContentResolver().query(CryptoContract.CurrencyEntry.CONTENT_URI,
                                projection,
                                "cur_name = ?",
                                new String[]{code},
                                null);
                        assert cursor != null;
                        int currencyValueIndex = cursor.getColumnIndex(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE);
                        if (cursor.moveToFirst()) {
                            btcValue = cursor.getDouble(currencyValueIndex);
                        }
                        curValue.setText(df.format(btcValue));
                        logoText.setText(CurrencyHelper.getCurrencySymbol(code));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.bitcoin);
                        cursor.close();

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(columnPosition - 1);
            }
        });

        curSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @TargetApi(Build.VERSION_CODES.O)
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


                mDBHelper = new CryptoCurrencyDBHelper(getApplicationContext());


                if (!curSpinnerClicked) {


                    if (currency_code != null) {
                        if (currency_code.equals(ETH_CODE)) {

                            curSpinner.setSelection(0);

                        }
                        if (currency_code.equals(BTC_CODE)) {

                            curSpinner.setSelection(1);

                        }


                    }

                    cryptoCurSpinnerChecker();


                }

                if (currency_code != null) {

                    if (cryptSelected.equals(getString(R.string.code_eth_text))) {

                        currency_code = "eth_value";
                    }

                    if (cryptSelected.equals(getString(R.string.code_btc_text))) {

                        currency_code = "btc_value";
                    }
                }


                if (currency_code != null) {

                    if (currency_code.equals(ETH_CODE)) {

                        double ethValue = 0;


                        String[] projection = {
                                CryptoContract.CurrencyEntry._ID,
                                CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE
                        };
                        Cursor cursor = getApplicationContext().getContentResolver().query(CryptoContract.CurrencyEntry.CONTENT_URI,
                                projection,
                                "cur_name = ?",
                                new String[]{code},
                                null);
                        assert cursor != null;
                        int currencyValueIndex = cursor.getColumnIndex(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE);
                        if (cursor.moveToFirst()) {
                            ethValue = cursor.getDouble(currencyValueIndex);
                        }

                        curValue.setText(df.format(ethValue));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.ethereum);
                        cursor.close();

                    }
                    if (currency_code.equals(BTC_CODE)) {

                        double btcValue = 0;

                        String[] projection = {
                                CryptoContract.CurrencyEntry._ID,
                                CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE
                        };
                        Cursor cursor = getApplicationContext().getContentResolver().query(CryptoContract.CurrencyEntry.CONTENT_URI,
                                projection,
                                "cur_name = ?",
                                new String[]{code},
                                null);
                        assert cursor != null;
                        int currencyValueIndex = cursor.getColumnIndex(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE);
                        if (cursor.moveToFirst()) {
                            btcValue = cursor.getDouble(currencyValueIndex);
                        }

                        curValue.setText(df.format(btcValue));
                        // Top image for CardView
                        cryptImage.setImageResource(R.drawable.bitcoin);
                        cursor.close();

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

                Intent customConversionRate = new Intent(getApplicationContext(), ConversionActivity.class);
                startActivity(customConversionRate);

            }
        });

    }

    /**
     * Loads currency choice spinner
     */
    private void loadSpinnerData() {


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

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate the menu options from the menu xml file
        //This add menu items to the app bar
        getMenuInflater().inflate(R.menu.network_available, menu);
        this.menu = menu;

        if (isConnected()) {
            // Let user know the status of the device network
            menu.findItem(R.id.menu_network_available).setVisible(true);
            menu.findItem(R.id.menu_network_absent).setVisible(false);
        } else {
            // Let user know the status of the device network
            menu.findItem(R.id.menu_network_available).setVisible(false);
            menu.findItem(R.id.menu_network_absent).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem netMenuItem = menu.findItem(R.id.menu_network_available);
        MenuItem nonetMenuItem = menu.findItem(R.id.menu_network_absent);

        netMenuItem.setVisible(online);
        nonetMenuItem.setVisible(!online);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(CONNECTION_INTENT));

    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


}
