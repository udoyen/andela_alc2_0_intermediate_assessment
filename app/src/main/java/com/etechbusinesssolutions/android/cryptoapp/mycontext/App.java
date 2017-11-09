package com.etechbusinesssolutions.android.cryptoapp.mycontext;


import android.app.Application;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by george on 11/9/17.
 */

public class App extends Application {

    private static Context context;

    public static Context getAppContext() {
        return context;
    }

    public static void showToast(String text) {
        Toast.makeText(App.getAppContext(), text, Toast.LENGTH_LONG).show();
    }


}
