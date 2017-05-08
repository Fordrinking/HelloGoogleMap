package com.hill1942.hellogooglemap.SENetwork;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by ykdac on 3/23/2017.
 */
public class NetworkUtil {
    public static NetworkUtil instance;
    public static final String TAG = "NetworkUtil";

    private static final String URL_KEY = "UrlKey";

    private URLTransCallback mCallback;
    private URLTransTask mURLTransTask;
    private String mUrlString;

    /**
     * Static initializer for NetworkUtil that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkUtil getInstance() {
        if (instance == null) {
            instance = new NetworkUtil();
        }
        return instance;
/*        NetworkUtil networkUtil = new NetworkUtil();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        networkUtil.setArguments(args);
        fragmentManager.beginTransaction().add(networkUtil, TAG).commit();
        return networkUtil;*/
    }

   /* @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Network Fragment: ", "onCreate: ");
        mUrlString = getArguments().getString(URL_KEY);

        Log.i("Network on Create: ", mUrlString);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
        //mCallback = (URLTransCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity to avoid memory leak.
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelURLTrans();
        super.onDestroy();
    }*/

    private String readStream(InputStream stream, int maxLength) throws IOException {
        String result = null;
        // Read InputStream using the UTF-8 charset.
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        // Create temporary buffer to hold Stream data with specified max length.
        char[] buffer = new char[maxLength];
        // Populate temporary buffer with Stream data.
        int numChars = 0;
        int readSize = 0;
        while (numChars < maxLength && readSize != -1) {
            numChars += readSize;
            int pct = (100 * numChars) / maxLength;
            //publishProgress(URLTransCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, pct);
            readSize = reader.read(buffer, numChars, buffer.length - numChars);
        }
        if (numChars != -1) {
            // The stream was not empty.
            // Create String that is actual length of response body if actual length was less than
            // max length.
            numChars = Math.min(numChars, maxLength);
            result = new String(buffer, 0, numChars);
        }
        return result;
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    public void startURLTrans(String url, Map<String, String> params, URLTransCallback<String> callback) {
        cancelURLTrans();

        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey().toString() + "=" + entry.getValue().toString() + "&");
        }

        mURLTransTask = new URLTransTask(callback);
        Log.i("Network Fragment: ", "url is: " + url );
        mURLTransTask.execute(url, sb.toString());
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    public void cancelURLTrans() {
        if (mURLTransTask != null) {
            mURLTransTask.cancel(true);
        }
    }

    private String doURLTrans(URL url, String urlParameters) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        //String urlParameters  = "param1=a&param2=b&param3=c";
        try {
            connection = (HttpURLConnection) url.openConnection();
            //connection.setDoOutput( true );
            connection.setInstanceFollowRedirects( false );
            connection.setRequestMethod( "GET" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty( "charset", "utf-8");
            connection.setUseCaches( false );
            //DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            //wr.writeBytes(urlParameters);
            //wr.flush();
            //wr.close();

            //connection.connect();
            //publishProgress(URLTransCallback.Progress.CONNECT_SUCCESS);
            int responseCode = connection.getResponseCode();

            Log.i("doURLTrans rescode: ", String.valueOf(responseCode));

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();

            Log.i("doURLTrans stream: ", stream.toString());

            //publishProgress(URLTransCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        Log.i("doURLTrans result: ", result);

        return result;
    }

    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class URLTransTask extends AsyncTask<String, Void, Result> {

        private URLTransCallback<String> mCallback;

        public URLTransTask(URLTransCallback<String> callback) {
            setCallback(callback);
        }

        void setCallback(URLTransCallback<String> callback) {
            mCallback = callback;
        }


        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            Log.i("URLTransTask: ", "onPreExecute");
            /*if (mCallback != null) {
                NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    mCallback.updateFromTrans(null);
                    cancel(true);
                }
            }*/
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected Result doInBackground(String... urls) {
            Log.i("URLTransTask: ", "doInBackground");
            Result result = null;
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                String params = urls[1];
                Log.i("URLTransTask: ", "url is: " + urlString );
                Log.i("URLTransTask: ", "params is: " + params );
                try {
                    URL url = new URL(urlString + "?" + params);
                    String resultString = doURLTrans(url, params);
                    if (resultString != null) {
                        result = new Result(resultString);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch(Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Updates the URLTransCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && mCallback != null) {
                if (result.mException != null) {
                    mCallback.updateFromTrans(result.mException.getMessage());
                } else if (result.mResultValue != null) {
                    mCallback.updateFromTrans(result.mResultValue);
                }
                mCallback.finishTrans();
            }
            Log.i("URLTransTask: ", "onPostExecute, result: " + result.toString());
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
            //Log.i("URLTransTask: ", "doInBackground");
        }

    }
}
