/*
 * Copyright abenefic (c) 2017.
 */

package ru.itmasterskaya.workatwalk;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by benefic on 30.01.17.
 */

class NetworkServices {
    public NetworkServices() {
    }

    static void setServerAddress(Context context, String server) {
        SharedPreferences preferences = context.getSharedPreferences(Constant.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constant.SERVER_ADDRESS, server);
        editor.apply();
    }

    static String getServerAddress(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constant.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(Constant.SERVER_ADDRESS, "");
    }

    static String autenticationRequest(Context context, String mUser, String mPassword) {
        // TODO: 04.02.17 change thith
        return "OK";
        /*if (SoapHelper.isOnline(context)) {
            SharedPreferences preferences = context.getSharedPreferences(Constant.PREFERENCES_NAME, Context.MODE_PRIVATE);
            SoapHelper.autenticate(preferences.getString(Constant.SERVER_ADDRESS, ""), mUser, mPassword);
            SoapHelper soapHelper = SoapHelper.getManager();
            String params = mUser + "#" + mPassword;
            String[] result = soapHelper.soapRequest(context, "autenticate", params);
            if (result[0].equals("0")) {
                return result[1];
            } else {
                return getErrorMessage(context, result[0]);
            }
        }
        return "false";*/
    }

    static String getErrorMessage(Context context, String errorCode) {
        String message;
        int code = Integer.parseInt(errorCode);

        message = getErrorMessage(context, code);

        return message;
    }

    static String getErrorMessage(Context context, int code) {
        String message;

        switch (code) {
            case 0:
                message = context.getString(R.string.no_error);
                break;
            //TODO add errors codes
            default:
                message = context.getString(R.string.error_unknown);
                break;
        }

        return message;
    }

}
