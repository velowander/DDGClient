package org.coursera.fanchuan.android101;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
                return HttpGetHelper.execute(params[0]);
            } else return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //This method has access to the UI thread
            Log.d("DDG REST API json", result);
            observer.updateRawJson(result);
            try {
                JSONObject queryJSON = new JSONObject(result);
                String strDefinition = (String) queryJSON.get("Definition");
                String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
                observer.updateDefinition(strDefinition);
                observer.updateDefinitionURL(strDefinitionURL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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