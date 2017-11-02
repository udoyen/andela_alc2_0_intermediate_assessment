package com.etechbusinesssolutions.android.cryptoapp;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by george on 10/10/17.
 */

public class CrytocurrencyQueryUtils {

    // Array for names of currencies
    private static String[] majorCur = {
            "USD",
            "EUR",
            "NGN",
            "RUB",
            "CAD",
            "JPY",
            "GBP",
            "AUD",
            "INR",
            "HKD",
            "IDR",
            "SGD",
            "CHF",
            "CNY",
            "ZAR",
            "THB",
            "SAR",
            "KRW",
            "GHS",
            "BRL"
    };
    // int array used to access the update database
    private static int[] virtualId = {

            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20
    };
    /**
     * Format to use for displayed currencies
     */
    DecimalFormat df = new DecimalFormat("#,###.###");


    /**
     * Create a private constructor because no one should ever create a {@link CrytocurrencyQueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private CrytocurrencyQueryUtils() {

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Currency> extractFeatureFromJson(String cryptoJSON) {

        // if the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(cryptoJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding currency values
        List<Currency> cryptoValues = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(cryptoJSON);

            // Get the two JSONObject within the baseJsonResponse
            JSONObject eth = baseJsonResponse.getJSONObject("ETH");
            JSONObject btc = baseJsonResponse.getJSONObject("BTC");

            // Iterate over the returned object and get the keys and values
            // and add that to the List
            for (int i = 0; i < eth.length(); i++) {


                double ethValue = eth.getDouble(majorCur[i]);

                //BigDecimal ethValue = BigDecimal.valueOf(eth.getDouble(majorCur[i]));

                //String btcCurrency = btc.keys().next();
                double btcValue = btc.getDouble(majorCur[i]);
                //BigDecimal btcValue = BigDecimal.valueOf(eth.getDouble(majorCur[i]));


                // Create a new {@link Currency} object with the key, and value
                Currency cur = new Currency(majorCur[i], ethValue, btcValue, virtualId[i]);

                // Add the new {@link Currency} object to the list of currencies
                cryptoValues.add(cur);

            }


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("CrytoCurrencyQueryUtils", "Problem parsing the cryptocompare API results", e);
        }

        // Return the list of currencies
        return cryptoValues;

    }


    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e("Error", "Problem building the URL ", e);
        }
        return url;
    }


    private static String makeHttpsRequest(URL url) throws IOException {

        String jsonResponse = "";

        // If the url is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e("Error", "Error response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e("Error", "Problem retrieving the earthquake JSON results.", e);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }

        return jsonResponse;
    }


    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    @TargetApi(Build.VERSION_CODES.N)
    public static List<Currency> fetchCurrencyData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpsRequest(url);

        } catch (IOException e) {
            Log.e("Error", "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Currency}
        List<Currency> currency = extractFeatureFromJson(jsonResponse);


        // Return the list of {@link Github}
        return currency;
    }


}
