package com.etechbusinesssolutions.android.cryptoapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.etechbusinesssolutions.android.cryptoapp.cryptservice.JobSchedulerService;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoContract;
import com.etechbusinesssolutions.android.cryptoapp.networkutil.NetworkUtil;

import java.util.IllegalFormatException;
import java.util.List;
import java.util.Objects;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HomeActivity extends AppCompatActivity implements LoaderCallbacks<List<Currency>> {

    // URL for the currency data from cryptocompare
    private static final String CRYPTO_CURRENRY_URL = "https://min-api.cryptocompare.com/data/pricemulti";
    /**
     * Constant value for the earthquake loader ID. We can choose any integer
     * This really comes into play when you're using multiple loaders
     */
    private static final int CRYPTOCURRENCY_LOADER_ID = 1;

    /**
     * JobScheduler Job ID
     */
    private static final int JOB_ID = 1;
    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final String MY_INTENT = "com.etechbusinesssolutions.android.cryptoapp.cryptservice.CUSTOM_INTENT";
    private static final String CONNECTION_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";

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
     * Used to change the
     * display state of the
     * data loading icon.
     */
    MenuItem refreshMenuItem;


    /**
     * Use this to catch the intent sent from the JobSchedulerService class
     */
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {


            if (Objects.equals(intent.getAction(), MY_INTENT)) {


                receiverLoad();
            }

            // Set the network menu status
            if (Objects.equals(intent.getAction(), CONNECTION_INTENT)) {

                status = NetworkUtil.getConnectivityStatusString(context);
                online = (Objects.equals(status, "Wifi enabled") || Objects.equals(status, "Mobile data enabled"));
                supportInvalidateOptionsMenu();

            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the intent here
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_INTENT);
        intentFilter.addAction(CONNECTION_INTENT);
        registerReceiver(this.broadcastReceiver, intentFilter);

        // Initialize JobScheduler
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        mJobScheduler.schedule(new JobInfo.Builder(JOB_ID,
                new ComponentName(this, JobSchedulerService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(30000)
                .build());

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //check internet connection
        assert connMgr != null;
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {


            //call.run();
            //This is where my sync code will be, but for testing purposes I only have a Log statement            L
            // Get a reference to the loader manager in order to interact with loaders
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(CRYPTOCURRENCY_LOADER_ID, null, HomeActivity.this);

        }


        // set the content activity to use for the activity_home.xml layout file
        setContentView(R.layout.activity_home);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);


        //Create an adapter that knows which fragment should be shown on each page
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(),
                HomeActivity.this);

        // Set the adapter onto the view pager
        assert viewPager != null;
        viewPager.setAdapter(adapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tab);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);


    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(MY_INTENT));
        registerReceiver(broadcastReceiver, new IntentFilter(CONNECTION_INTENT));


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);

    }

    @Override
    public Loader<List<Currency>> onCreateLoader(int id, Bundle args) {

        // Setup the baseURI
        Uri baseUri = Uri.parse(CRYPTO_CURRENRY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("fsyms", "ETH,BTC");
        uriBuilder.appendQueryParameter("tsyms", "USD,EUR,NGN,RUB,CAD,JPY,GBP,AUD,INR,HKD,IDR,SGD,CHF,CNY,ZAR,THB,SAR,KRW,GHS,BRL");


        return new CrytoCurrencyLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Currency>> loader, List<Currency> data) {


        //Used to delay the API dataload icon on the Actionbar
        final Handler handler = new Handler();

        // Create a ContentValues class object
        ContentValues values = new ContentValues();

        // Check if database table already present, if it exists
        // then update current records instead of inserting.
        boolean found = isTableExists();

        try {

            if (found) {

                try {
                    for (Currency element : data) {
                        values.put(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE, element.getcEthValue());
                        values.put(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE, element.getcBtcValue());

                        // Update database
                        getContentResolver().update(
                                CryptoContract.CurrencyEntry.CONTENT_URI,
                                values,
                                "_id = ?",
                                new String[]{String.valueOf(element.getcId())}
                        );


                    }


                } catch (NullPointerException e) {

                    Log.i("Error", "Update error iterating over the data ... " + e);
                } catch (IllegalFormatException f) {

                    Log.i("Error", "Update format error ... " + f);
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Remove the API call icon in Actionbar
                        refreshMenuItem = menu.findItem(R.id.menu_refresh);
                        refreshMenuItem.setVisible(false);
                    }
                }, 8000);


            } else {

                try {

                    for (Currency element : data) {

                        values.put(CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME, element.getcName());
                        values.put(CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE, element.getcEthValue());
                        values.put(CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE, element.getcBtcValue());

                        // Insert data into SQLiteDatabase
                        getContentResolver().insert(CryptoContract.CurrencyEntry.CONTENT_URI, values);


                    }

                } catch (NullPointerException e) {

                    Log.i("Error", "database insert error no data to iterate over ... " + e);
                } catch (IllegalFormatException f) {

                    Log.i("Error", "Update format error ... " + f);
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Remove the API call icon in Actionbar
                        refreshMenuItem = menu.findItem(R.id.menu_refresh);
                        refreshMenuItem.setVisible(false);
                    }
                }, 8000);


            }
        } catch (NullPointerException g) {

            Log.i("Error", "Database existent confirmation error " + g);

        } catch (IllegalFormatException f) {

            Log.i("Error", "Update format error ... " + f);
        }


    }

    @Override
    public void onLoaderReset(Loader<List<Currency>> loader) {


        getLoaderManager().destroyLoader(CRYPTOCURRENCY_LOADER_ID);

    }

    /**
     * Used to determine if the database exists
     * so either an update is done or insert.
     *
     * @return true
     */
    public boolean isTableExists() {

        String[] projection = {

                CryptoContract.CurrencyEntry._ID,
                CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME,
                CryptoContract.CurrencyEntry.COLUMN_BTC_VALUE,
                CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE

        };

        Cursor cursor = getContentResolver().query(CryptoContract.CurrencyEntry.CONTENT_URI, projection, null, null, null);

        assert cursor != null;
        boolean exists = (cursor.getCount() > 0);
        cursor.close();

        return exists;


    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        //inflate the menu options from the menu xml file
        //This add menu items to the app bar
        getMenuInflater().inflate(R.menu.network_available, menu);
        this.menu = menu;

        if (menu != null) {

            if (isConnected()) {
                // Let user know the status of the device network
                menu.findItem(R.id.menu_network_available).setVisible(true);
                menu.findItem(R.id.menu_network_absent).setVisible(false);
            } else {
                // Let user know the status of the device network
                menu.findItem(R.id.menu_network_available).setVisible(false);
                menu.findItem(R.id.menu_network_absent).setVisible(true);
            }


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


    /**
     * Used to display the data loading
     * icon.
     */
    private void receiverLoad() {

        refreshMenuItem = menu.findItem(R.id.menu_refresh);
        refreshMenuItem.setVisible(true);
        getLoaderManager().restartLoader(CRYPTOCURRENCY_LOADER_ID, null, HomeActivity.this);
        getLoaderManager().getLoader(CRYPTOCURRENCY_LOADER_ID);

    }


}




