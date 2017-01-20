/*
 * Copyright abenefic (c) 2017.
 */

package ru.itmasterskaya.workatwalk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import org.kobjects.base64.Base64;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;


class SoapHelper {

    private static SoapHelper instance;

    public static synchronized SoapHelper getManager() {
        initialize();
        return instance;
    }

    private static void initialize() {
        if (instance == null) {
            instance = new SoapHelper();
        }
    }

    static boolean isOnline(Context context) {

        NetworkInfo netInfo = null;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            netInfo = cm.getActiveNetworkInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return netInfo != null && netInfo.isConnected();
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

    private HttpTransportSE getHttpTransportSE() {
        return getHttpTransportSE(10000);
    }

    private HttpTransportSE getHttpTransportSE(int timeout) {
        HttpTransportSE ht = new HttpTransportSE(Proxy.NO_PROXY, Constant.MAIN_REQUEST_URL, timeout);
        ht.debug = true;
        ht.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");
        return ht;
    }

    private SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        return envelope;
    }

    String regDevice(Context context) {
        SharedPreferences sPref = context.getSharedPreferences(Constant.PREFERENCES_NAME, Context.MODE_PRIVATE);
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String login = Constant.REG_USER;
        String password = Constant.REG_PASS;
        String methodName = "RegisterDevice";
        SoapObject request = new SoapObject(Constant.NAMESPACE, methodName);
        request.addProperty("DeviceID", android_id);
        request.addProperty("User", sPref.getString(Constant.USER_LOGIN, ""));
        request.addProperty("Password", sPref.getString(Constant.USER_PASSWORD, ""));
        request.addProperty("Name", sPref.getString(Constant.USER_NAME, ""));
        SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(request);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE ht = getHttpTransportSE();
        String result = "99";
        List<HeaderProperty> headerList = new ArrayList<>();
        headerList.add(new HeaderProperty("Authorization", "Basic " + Base64.encode((login + ":" + password).getBytes())));
        try {
            ht.call(Constant.SOAP_ACTION, envelope, headerList);

            SoapObject response = (SoapObject) envelope.bodyIn;

            result = response.getPrimitiveProperty("return").toString();

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressLint("HardwareIds")
    synchronized String[] soapRequest(Context context, String method, String requestParams) {
        SharedPreferences sPref = context.getSharedPreferences(Constant.PREFERENCES_NAME, Context.MODE_PRIVATE);
        String login = sPref.getString(Constant.USER_LOGIN, "");
        String password = sPref.getString(Constant.USER_PASSWORD, "");
        String methodName = "ExecuteTask";
        String responseString;
        SoapObject request = new SoapObject(Constant.NAMESPACE, methodName);
        request.addProperty("MethodName", method);
        request.addProperty("Params", requestParams);
        request.addProperty("DeviceID", Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(request);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE ht = getHttpTransportSE(5000);
        String result;
        List<HeaderProperty> headerList = new ArrayList<>();
        headerList.add(new HeaderProperty("Authorization", "Basic " + Base64.encode((login + ":" + password).getBytes())));
        try {
            ht.call(Constant.SOAP_ACTION, envelope, headerList);
            if (envelope.bodyIn instanceof SoapFault) {
                ((SoapFault) envelope.bodyIn).printStackTrace();
                return new String[]{"99"};
            }
            SoapObject response = (SoapObject) envelope.bodyIn;

            result = response.getPrimitiveProperty("return").toString();
            responseString = response.getPrimitiveProperty("Params").toString();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Soap", e.getMessage() + "; \n" + envelope.bodyIn + "; \n" + envelope.bodyOut);
            SqlDataManager sqlDataManager = SqlDataManager.getManager(context);
            sqlDataManager.writeSoapLog(method, requestParams, envelope.bodyOut.toString());
            return new String[]{"99", ""};
        }
        String[] requestResult = new String[2];
        requestResult[0] = result;
        requestResult[1] = responseString;
        SqlDataManager sqlDataManager = SqlDataManager.getManager(context);
        sqlDataManager.writeSoapLog(method, requestParams, result);
        return requestResult;
    }

    XmlPullParser prepareXpp(String input) throws XmlPullParserException {
        // получаем фабрику
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        // включаем поддержку namespace (по умолчанию выключена)
        factory.setNamespaceAware(true);
        // создаем парсер
        XmlPullParser xpp = factory.newPullParser();
        // даем парсеру на вход Reader
        xpp.setInput(new StringReader(input));
        return xpp;
    }
}
