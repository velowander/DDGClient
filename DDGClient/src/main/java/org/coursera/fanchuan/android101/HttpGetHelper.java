package org.coursera.fanchuan.android101;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class HttpGetHelper {
    /* Helper class to submit an HTTPGet request to a server, useful for REST API work
    * Constructor: no argument only but no need to instantiate as it has only static methods
     */
    final static String TAG = HttpGetHelper.class.getSimpleName();

    static String execute(String Url) {
        /*
        Url: the entire URL for the search, with http://
        Use URLEncoder.encode(parameter, "UTF-8") on parameters before passing; we don't want spaces
        in the URL
        */
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(Url);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.d(TAG, "HTTP Response Code: " + Integer.toString(statusCode));
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to retrieve remote string", e);
        }
        return builder.toString();
    }
}
