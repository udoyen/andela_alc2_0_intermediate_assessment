package com.etechbusinesssolutions.android.cryptoapp.conversion;

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
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.etechbusinesssolutions.android.cryptoapp.R;
import com.etechbusinesssolutions.android.cryptoapp.cryptservice.JobSchedulerService;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoContract;
import com.etechbusinesssolutions.android.cryptoapp.networkutil.NetworkUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Objects;

public class ConversionActivity extends AppCompatActivity {

    private static final String CONNECTION_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";
    /**
     * JobScheduler Job ID
     */
    private static final int JOB_ID = 1;
    /**
     * Value to convert
     */
    double userInput;
    /**
     * Create a spinner
     */
    Spinner spinner;
    /**
     * Used to determine if user has entered a number
     * default is false.
     */
    boolean editBoxWithText = false;
    /**
     * Used to check if conversion button
     * has been clicked
     */
    boolean conversionBtn = false;
    /**
     * Create EditText object
     */
    EditText value1;
    /**
     * Create an instance of the JobScheduler class
     */
    JobScheduler mJobScheduler;

    /**
     * Used to check network status
     */
    String status;
    /**
     * Used to check network status
     */
    boolean online;

    /**
     * Used to set the currently selected
     * currency spinner value
     */
    int currencyName;

    /**
     * Used to check if the spinner is
     * drawn for the first time
     */
    private boolean curSpinnerClicked = false;

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
            if (intent.getAction().equals(CONNECTION_INTENT)) {

                status = NetworkUtil.getConnectivityStatusString(context);
                online = (Objects.equals(status, "Wifi enabled") || Objects.equals(status, "Mobile data enabled"));
                supportInvalidateOptionsMenu();

            }
        }
    };
    /**
     * Radio button state
     */
    private String radioBtnState;
    /**
     * Currency to convert
     */
    private String code;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

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

        value1 = (EditText) findViewById(R.id.value_to_convert_box);

        //Catch the data sent from the
        // CardView
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        currencyName = extras.getInt("CURRENCY_NAME");
        Log.i("VALUE", "Value of spinner from cardview: " + currencyName);

        value1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Set this to true on user entering
                // a number
                editBoxWithText = true;

            }

            @Override
            public void afterTextChanged(Editable s) {

                // Set this to true on user entering
                // a number
                editBoxWithText = true;

            }
        });


        // Setup the spinner data
        // Instantiate the spinner
        spinner = (Spinner) findViewById(R.id.conversion_spinner);

        // Load the spinner data from database
        loadSpinnerData();


        // Spinner listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the item that was selected or clicked
                code = parent.getItemAtPosition(position).toString();

                if (!curSpinnerClicked) {
                    spinner.setSelection(currencyName);
                    spinnerChecker();
                }

                if (editBoxWithText) {

                    conversion();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });



    }


    private void loadSpinnerData() {

        List<String> codes = new ArrayList<>();

        String[] projection = {
                CryptoContract.CurrencyEntry._ID,
                CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME
        };

        // Get the currency names from the database
        Cursor cursor = getApplicationContext().getContentResolver().query(
                CryptoContract.CurrencyEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        //Add currency names to List
        assert cursor != null;
        if (cursor.moveToFirst()) {
            do {
                codes.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // Create adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, codes);

        // Dropdown layer style
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach dataAdapter to spinner
        spinner.setAdapter(dataAdapter);
        cursor.close();

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onRadioButtonClicked(View view) {


        final TextView currencyLogo = (TextView) findViewById(R.id.conversion_currency_logo);

        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_btc:
                if (checked)
                    // Set the value of the
                    // radioBtnState variable.
                    radioBtnState = "btc_value";
                // Set the currency logo text and make it visible
                currencyLogo.setText(R.string.conversion_radio_btc_btn);
                currencyLogo.setVisibility(View.VISIBLE);

                //Check if user has entered some text, and
                //whether conversion button was clicked.
                if (editBoxWithText && conversionBtn) {

                    conversion();
                }

                break;
            case R.id.radio_eth:
                if (checked)
                    // Set the value of the
                    // radioBtnState variable.
                    radioBtnState = "eth_value";
                // Set the currency logo text and make it visible
                currencyLogo.setText(R.string.conversion_radio_eth_btn);
                currencyLogo.setVisibility(View.VISIBLE);


                //Check if user has entered some text, and
                //whether conversion button was clicked.
                if (editBoxWithText && conversionBtn) {

                    conversion();
                }

                break;
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void conversion() {

        // Calculation result
        double cal;

        // Create a radio button object
        RadioGroup rBtn = (RadioGroup) findViewById(R.id.radio_container);
        int checked = rBtn.getCheckedRadioButtonId();


        // Grab the user input from EditText box
        EditText value1 = (EditText) findViewById(R.id.value_to_convert_box);


        // Grab the TextViews to update
        TextView resultTextView = (TextView) findViewById(R.id.conversion_value);


        //Checked to make sure user input isn't empty
        if (value1.getText().toString().trim().length() > 0) {

            // Grab user input.
            userInput = Double.parseDouble(value1.getText().toString().trim());


        } else {

            Toast.makeText(getApplicationContext(), "Please enter a value in the number box", Toast.LENGTH_LONG).show();

        }

        String result;

        // Check if user has selected a crypto currency type
        if (radioBtnState == null || checked == -1) {

            Toast.makeText(ConversionActivity.this,
                    "Please select a crypto currency type to convert to!",
                    Toast.LENGTH_LONG).show();

        } else {

            try {

                // Format to use for calculated conversion
                DecimalFormat df = new DecimalFormat("#,###.###");

                String currencyToConvert;
                double returnValue = 0;

                if (Objects.equals(radioBtnState, "eth_value")) {
                    currencyToConvert = CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE;
                } else {
                    currencyToConvert = CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE;
                }

                String[] projection = {
                        CryptoContract.CurrencyEntry._ID,
                        currencyToConvert
                };

                Cursor cursor = getApplicationContext().getContentResolver().query(
                  CryptoContract.CurrencyEntry.CONTENT_URI,
                        projection,
                        "cur_name = ?",
                        new String[]{code},
                        null,
                        null);

                assert cursor != null;
                int currencyValueIndex = cursor.getColumnIndex(currencyToConvert);
                if (cursor.moveToFirst()) {
                    returnValue = cursor.getDouble(currencyValueIndex);
                }

                // Calculate the conversion rate
                cal = userInput / returnValue;

                // Used to format the calculation output
                result = df.format(cal);

                //Set the Conversion Result TextView
                resultTextView.setText(result);

                // Close database connection
                cursor.close();


            } catch (NumberFormatException e) {

                Toast.makeText(ConversionActivity.this,
                        "Please make sure number entered is a real number",
                        Toast.LENGTH_LONG).show();

            } catch (IllegalFormatException g) {

                Toast.makeText(ConversionActivity.this,
                        "Please make sure number entered is a real number",
                        Toast.LENGTH_LONG).show();
            }


        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onConvertBtnClick(View view) {

        // Signal button click has occurred
        conversionBtn = true;

        conversion();


    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate the menu options from the menu xml file
        //This add menu items to the app bar
        getMenuInflater().inflate(R.menu.network_available, menu);

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
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(CONNECTION_INTENT));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);

    }

    /**
     * Checks the original state
     * of the currency spinner setOnItemSelectedListener
     * event.
     */
    public void spinnerChecker() {

        if (!curSpinnerClicked) {

            curSpinnerClicked = true;

        }
    }
}
