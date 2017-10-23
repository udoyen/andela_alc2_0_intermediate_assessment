package com.etechbusinesssolutions.android.cryptoapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.etechbusinesssolutions.android.cryptoapp.cardview.CardActivity;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoContract;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoCurrencyDBHelper;

/**
 * Created by george on 10/10/17.
 */

public class EthFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    // Used for logging
    //TODO: Remove
    public static final String LOG_TAG = EthFragment.class.getName();
    /**
     * Constant value for the github loader ID. We can choose any integer
     * This really comes into play when you're using multiple loaders
     */
    private static final int DATABASE_LOADER_ID = 3;

    /**
     *  String to identify intent source
     */
    private static final String ETH_CODE = "eth_value";

    /**
     * Adapter for the list of currencies values gotten from database
     */
    private EthCurrencyAdapter mAdapter;

    //TODO: Remove
    /**
     * Create an instance of CryptoCurrencyDBHelper
     */
    private CryptoCurrencyDBHelper mDBHelper;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;
    /**
     * Progressbar that is displayed before loader loads data
     */
    private ProgressBar progressBar;

    /**
     * SwipeRefreshLayout
     */
    private SwipeRefreshLayout mySwipeRefreshLayout;


    public EthFragment() {

        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.currency_base, container, false);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        // currency_base.xml layout file.
        final ListView listView = rootView.findViewById(R.id.list);

        //TODO: Add an empty view for when no data exists
        // Get the empty  Text view
        mEmptyStateTextView = rootView.findViewById(R.id.empty);
        //TODO: Adda nice empty view to the layout file for this fragment
        listView.setEmptyView(mEmptyStateTextView);

        progressBar = rootView.findViewById(R.id.loading_spinner);

        // Create an {@link BtcCurrencyAdapter}, whose data source is a list of {@link Currency}.
        // The adapter knows how to create the list items for each item in the list.
        mAdapter = new EthCurrencyAdapter(getContext(), null, false);

        // Make the {@link ListView} use the {@link WordAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Word} in the list.
        listView.setAdapter(mAdapter);


        // Respond to click event on user item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get the current currency that was clicked
                int number = (int) mAdapter.getItemId(position);

                //TODO: Remove
                Log.i(LOG_TAG, "Position of eth item clicked: " + number);



                // Create new intent to view CardView
                Intent cardViewIntent = new Intent(rootView.getContext(), CardActivity.class);

                // Send the "eth_value" to CardView so the right database columns will be accessed
                // and send the column name too so the Spinner default value
                // will be set.
                cardViewIntent.putExtra("CURRENCY_CODE", ETH_CODE);
                cardViewIntent.putExtra("COLUMN_NAME", number);

                startActivity(cardViewIntent);


            }
        });


        if (mAdapter.isEmpty()) {
            //****LoadManager will load information****
            // Get a reference to the loader manager in order to interact with loaders
            //TODO: Remove
            Log.i(LOG_TAG, "TEST: Get the LoadManager being used ...");
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            //TODO: Remove
            Log.i(LOG_TAG, "TEST: Calling initloader()...");
            loaderManager.initLoader(DATABASE_LOADER_ID, null, this);

            // progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
            progressBar.setVisibility(View.GONE);

        } else {

            // Make sure the ListView is empty before displaying "No Internet Connection"
            if (mAdapter.isEmpty()) {

                //if there's no data to show. display TextView to no internet connection
                mEmptyStateTextView.setText(R.string.no_data);
            }
        }

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (listView.getChildAt(0) != null) {

                    mySwipeRefreshLayout.setEnabled(listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() == 0);

                }
            }
        });


        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        //final ListView listView = view.findViewById(R.id.list);

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies which columns from the
        // database that will be used after this query
        String[] projection = {
                CryptoContract.CurrencyEntry._ID,
                CryptoContract.CurrencyEntry.COLUMN_CURRENCY_NAME,
                CryptoContract.CurrencyEntry.COLUMN_ETH_VALUE
        };

        return new CursorLoader(getContext(),
                CryptoContract.CurrencyEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //TODO: Send cursor information back to cursorAdapter
        mAdapter.swapCursor(data);




    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clears out the adapter's reference to the Cursor.
        // This prevents memory leaks.
        mAdapter.swapCursor(null);

    }

    public void userPageRefreshAction() {

        // Get a reference to the loader manager in order to interact with loaders
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(DATABASE_LOADER_ID, null, this);

        // Remove "No Internet Connection" TextView on reconnecting
        // if Github Adapter was empty
        mEmptyStateTextView.setVisibility(View.INVISIBLE);

    }


    @Override
    public void onRefresh() {
        userPageRefreshAction();
    }
}
