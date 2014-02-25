package org.coursera.fanchuan.android101;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

class DDGQuery {

    /* Wrapper class for AsyncQuery; needed because the AsyncTask onPreExecute() method does not have
    access to the search parameters and I wanted to update the queryString on the UI BEFORE the submitting
    the asynchronous network request (better user experience).
     */

    private String TAG = DDGQuery.class.getSimpleName();
    private DDGQueryObserver observer;
    private Context context;

    DDGQuery(final DDGQueryObserver observer) {
        this.observer = observer;
    }

    public DDGQuery(final DDGQueryObserver observer, final Context context) {
        //Will use the (optionally) supplied ProgressDialog and close it afterwards
        this.observer = observer;
        this.context = context;
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
                observer.updateQueryString(queryString);
                new AsyncQuery(observer, context).execute(queryString);
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

        public AsyncQuery(final DDGQueryObserver observer) {
            this.observer = observer;
        }

        public AsyncQuery(final DDGQueryObserver observer, final Context context) {
            this.observer = observer;
            this.context = context;
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
            try {
                Log.i(TAG, "DDG REST API json" + result);
                observer.updateRawJson(result);
                JSONObject queryJSON = new JSONObject(result);
                String strDefinition = (String) queryJSON.get("Definition");
                String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
                observer.updateDefinition(strDefinition);
                observer.updateDefinitionURL(strDefinitionURL);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to parse json / update definitions", e);
            } finally {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
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