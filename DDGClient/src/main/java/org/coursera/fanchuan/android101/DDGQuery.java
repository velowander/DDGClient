package org.coursera.fanchuan.android101;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class DDGQuery {

    private DDGQueryObserver observer;
    private String TAG = DDGQuery.class.getSimpleName();

    DDGQuery(DDGQueryObserver observer) {
        this.observer = observer;
    }

    public void execute(String searchWord) {
        /* Builds the query string, encoding the user's search word(s) and the app name, actual passing
        of the query to the remote server is done in the AsyncTask */
        //Per Duckduckgo public API, includes &t parameter sending name of my app
        final String queryTemplate = "http://api.duckduckgo.com/?q=define+%s&format=json&t=%s&pretty=1";
        //show the user a "toast" (on screen notification) that query has started
        try {
            //must encode spaces and other URL unsafe characters
            final String encodingType = "UTF-8";
            searchWord = URLEncoder.encode(searchWord, encodingType);
            CharSequence appName = URLEncoder.encode(TAG, encodingType);
            if (!searchWord.isEmpty()) {
                String queryString = String.format(queryTemplate, searchWord, appName);
                observer.updateQueryString(queryString);
                //TextView txt = (TextView) findViewById(R.id.textViewQuery);
                //txt.setText(queryString);
                new AsyncQuery().execute(queryString);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    protected class AsyncQuery extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //No access to UI in this method, wait for onPostExecute()
            Log.d(TAG, "starting AsyncQuery.doInBackground()");
            if (params.length >= 1) {
                return getJsonRestAPI(params[0]);
            } else return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //This method has access to the UI thread
            Log.d("DDG REST API json", result);
            //TextView txtResult = (TextView) findViewById(R.id.textViewRawJson);
            //txtResult.setText(result);
            observer.updateRawJson(result);
            try {
                JSONObject queryJSON = new JSONObject(result);
                String strDefinition = (String) queryJSON.get("Definition");
                String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
                //TextView txtDefinition = (TextView) findViewById(R.id.textViewDefinition);
                //txtDefinition.setText(strDefinition);
                observer.updateDefinition(strDefinition);
                //TextView txtDefinitionURL = (TextView) findViewById(R.id.textViewDefinitionURL);
                //txtDefinitionURL.setText(strDefinitionURL);
                observer.updateDefinitionURL(strDefinitionURL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getJsonRestAPI(String queryString) {
            /*
            queryString: the entire URL for the search, with http://
            Use URLEncoder.encode(parameter, "UTF-8") on parameters before passing; we don't want spaces
            in the URL
            */
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(queryString);
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
                    Log.e(Integer.toString(statusCode), "HTTP Response Code: " + Integer.toString(statusCode));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return builder.toString();
        }
    }
}

interface DDGQueryObserver {
    //Callback methods to give the activity information to update the UI.
    void updateDefinition(String definition);

    void updateDefinitionURL(String definitionURL);

    void updateRawJson(String rawJson);

    void updateQueryString(String queryString);
}