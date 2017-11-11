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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.etechbusinesssolutions.android.cryptoapp.cardview.CardActivity;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoContract;
import com.etechbusinesssolutions.android.cryptoapp.data.CryptoCurrencyDBHelper;

/**
 * Created by george on 10/10/17.
 */

public class EthFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * Constant value for the github loader ID. We can choose any integer
     * This really comes into play when you're using multiple loaders
     */
    private static final int DATABASE_LOADER_ID = 3;

    /**
     * String to identify intent source
     */
    private static final String ETH_CODE = "eth_value";
    LoaderManager loaderManager;

    /**
     * Adapter for the list of currencies values gotten from database
     */
    private EthCurrencyAdapter mAdapter;
    /**
     * Create an instance of CryptoCurrencyDBHelper
     */
    private CryptoCurrencyDBHelper mDBHelper;
    /**
     * TextView that is displayed when the list is empty
     */
    private View mEmptyStateTextView;
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

        progressBar = rootView.findViewById(R.id.loading_spinner);

        // Get the empty  Text view
        mEmptyStateTextView = rootView.findViewById(R.id.empty);
        //Add a nice empty view to the layout file for this fragment
        listView.setEmptyView(mEmptyStateTextView);



        // Create an {@link BtcCurrencyAdapter}, whose data source is a list of {@link Currency}.
        // The adapter knows how to create the list items for each item in the list.
        mAdapter = new EthCurrencyAdapter(getContext(), null, false);

        // Make the {@link ListView} use the {@link WordAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Word} in the list.
        listView.setAdapter(mAdapter);
        // Notify listview of data changes on adapter
        mAdapter.notifyDataSetChanged();


        // Respond to click event on user item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get the current currency that was clicked
                int number = (int) mAdapter.getItemId(position);

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

            loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).

            loaderManager.initLoader(DATABASE_LOADER_ID, null, this);


        }


        /*
          Use this code to prevent SwipeRefreshLayout from interfering with
          Scrolling in ListView
         */
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

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        // Set the color of the swiperefresh animation
        mySwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark,R.color.colorPrimary,R.color.colorPrimaryLight, R.color.colorSecondary);
        mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                userPageRefreshAction();

            }
        });
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

        mAdapter.swapCursor(data);

        // Stop the refreshing animation
        mySwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Clears out the adapter's reference to the Cursor.
        // This prevents memory leaks.
        mAdapter.swapCursor(null);

    }

    public void userPageRefreshAction() {


        // Get a reference to the loader manager in order to interact with loaders
        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        getLoaderManager().getLoader(DATABASE_LOADER_ID);
        getLoaderManager().restartLoader(DATABASE_LOADER_ID, null, EthFragment.this);


    }



}
