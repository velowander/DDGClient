package org.coursera.fanchuan.android101;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

class DDGQuery {

    /* Wrapper class for AsyncQuery; needed because the AsyncTask onPreExecute() method does not have
    access to the search parameters and I wanted to update the queryString on the UI BEFORE the submitting
    the asynchronous network request (better user experience).
     */

    //Key strings for Intent containing query data
    final static String updateDefinition = "updateDefinition";
    final static String updateDefinitionURL = "updateDefinitionURL";
    final static String updateRawJson = "updateRawJson";
    final static String updateQueryString = "updateQueryString";


    private String TAG = DDGQuery.class.getSimpleName();
    private DDGQueryObserver observer;
    private Context context;
    private LocalBroadcastManager broadcastManager;

    public DDGQuery(final Context context) {
        if (context == null)
            throw new IllegalArgumentException("DDGQuery: Context may not be null");
        broadcastManager = LocalBroadcastManager.getInstance(context);
        this.context = context;
    }

    @Deprecated
    public DDGQuery(final Context context, final DDGQueryObserver observer) {
        //Will use the (optionally) supplied ProgressDialog and close it afterwards
        this(context);
        this.observer = observer;
    }


    public void execute(String searchWord) {
        /* Builds the query string, encoding the user's search word(s) and the app name, actual passing
        of the query to the remote server is done in the AsyncTask */
        //Per Duckduckgo public API, includes &t parameter sending name of my app
        final String queryTemplate = "http://api.duckduckgo.com/?q=define+%s&format=json&t=%s&pretty=1";
        try {
            //must encode spaces and other URL unsafe characters
            final String encodingType = "UTF-8";
            searchWord = URLEncoder.encode(searchWord, encodingType);
            CharSequence appName = URLEncoder.encode(TAG, encodingType);
            if (!searchWord.isEmpty()) {
                String queryString = String.format(queryTemplate, searchWord, appName);
                if (observer != null) observer.updateQueryString(queryString);
                Log.i(TAG, "Sending broadcast");
                broadcastManager.sendBroadcast(new Intent(MainActivity.JSON_RESULT_INTENT)
                        .putExtra(updateQueryString, queryString));
                new AsyncQuery(context).execute(queryString);
            }
        } catch (Exception e) {
            Log.e(TAG, "DDGQuery wrapper class execute: failed due to Exception", e);
        }
    }

    protected static class AsyncQuery extends AsyncTask<String, Void, String> {

        private final String TAG = AsyncQuery.class.getSimpleName();
        private DDGQueryObserver observer;
        private Context context;
        private ProgressDialog progressDialog;

        public AsyncQuery(final Context context) {
            this.context = context;
        }

        @Deprecated
        public AsyncQuery(final Context context, final DDGQueryObserver observer) {
            this(context);
            this.observer = observer;
        }

        @Override
        protected void onPreExecute() {
            //Setup a "please wait" dialog if context is available
            if (context != null) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setCancelable(true);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle(context.getText(R.string.queryStartedDialogTitle));
                progressDialog.setMessage(context.getText(R.string.queryStartedDialogText));
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //No access to UI or Activity callback interface in this method, wait for onPostExecute()
            Log.d(TAG, "starting AsyncQuery.doInBackground()");
            if (params.length >= 1) {
                return HttpGetHelper.execute(params[0]);
            } else return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //This method has access to the UI thread
            Intent broadcastIntent = new Intent(MainActivity.JSON_RESULT_INTENT);
            try {
                Log.i(TAG, "DDG REST API json" + result);
                if (observer != null) observer.updateRawJson(result);
                broadcastIntent.putExtra(DDGQuery.updateRawJson, result);
                JSONObject queryJSON = new JSONObject(result);
                String strDefinition = (String) queryJSON.get("Definition");
                String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
                if (observer != null) observer.updateDefinition(strDefinition);
                broadcastIntent.putExtra(DDGQuery.updateDefinition, strDefinition);
                if (observer != null) observer.updateDefinitionURL(strDefinitionURL);
                broadcastIntent.putExtra(DDGQuery.updateDefinitionURL, strDefinitionURL);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to parse json / update definitions", e);
            } finally {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
        }
    }
}

@Deprecated
interface DDGQueryObserver {
    /* Callback methods to give the activity information to update the UI.
    * @Deprecated in favor of using Intents and LocalBroadcastManager */
    @Deprecated
    void updateDefinition(String definition);

    @Deprecated
    void updateDefinitionURL(String definitionURL);

    @Deprecated
    void updateRawJson(String rawJson);

    @Deprecated
    void updateQueryString(String queryString);
}