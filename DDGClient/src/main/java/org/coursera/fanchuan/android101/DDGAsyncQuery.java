package org.coursera.fanchuan.android101;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Deprecated
@SuppressWarnings("unused")
class DDGAsyncQuery extends AsyncTask<String, Void, String> {
    /* Replacement class for DDGQuery that avoids needing a "wrapper" around the AsyncTask
    * DDGAsyncQuery no longer supports the @Deprecated DDGObserver interface
    * I designed DDGLoader to supercede this class though I am working on an issue (see forceLoad)*/

    private final String TAG = DDGAsyncQuery.class.getSimpleName();
    private LocalBroadcastManager broadcastManager;
    private ProgressDialog dialog;

    //Key strings for Intent containing query data
    final static String updateDefinition = "UPDATE_DEFINITION";
    final static String updateDefinitionURL = "UPDATE_DEFINITION_URL";
    final static String updateRawJson = "UPDATE_RAW_JSON";
    final static String updateQueryString = "UPDATE_QUERY_STRING";

    public DDGAsyncQuery(final Context context) {
        /* Supplying a non-null Context is strongly recommended
        * If context is null, this class can't report the results to the UI (to the Log only)!! */
        if (context != null) {
            broadcastManager = LocalBroadcastManager.getInstance(context);
            this.dialog = new ProgressDialog(context);
            this.dialog.setCancelable(true);
            this.dialog.setIndeterminate(true);
            this.dialog.setTitle(context.getText(R.string.queryStartedDialogTitle));
            this.dialog.setMessage(context.getText(R.string.queryStartedDialogText));
        }
    }

    @Override
    protected void onPreExecute() {
        /* This method runs on the UI thread
        * Setup a "please wait" dialog if context is available */
        if (dialog != null) dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        /* Runs on worker thread; onPostExecute runs on UI thread
        * params[0] should be searchWord, no other indexes defined */
        final String encodingType = "UTF-8";
        final String queryTemplate = "http://api.duckduckgo.com/?q=define+%s&format=json&t=%s&pretty=1";
        Log.d(TAG, "starting AsyncQuery.doInBackground()");
        if (params == null || params.length == 0)
            throw new IllegalArgumentException("params[0] must be a String");
        try {
            String searchWord = URLEncoder.encode(params[0], encodingType);
            String appName = URLEncoder.encode(TAG, encodingType);
            String queryString = String.format(queryTemplate, searchWord, appName);
            Log.i(TAG, "Sending broadcast");
            if (broadcastManager != null)
                broadcastManager.sendBroadcast(new Intent(MainActivity.JSON_RESULT_INTENT)
                        .putExtra(updateQueryString, queryString));
            return HttpGetHelper.execute(queryString);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "doInBackground(): problem encoding URL");
        } catch (NullPointerException e) {
            Log.e(TAG, "doInBackground(): NullPointer");
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        //This method runs on the UI thread
        if (dialog != null & dialog.isShowing()) {
            dialog.dismiss();
        }
        Intent broadcastIntent = new Intent(MainActivity.JSON_RESULT_INTENT);
        try {
            Log.i(TAG, "DDG REST API json" + result);
            broadcastIntent.putExtra(DDGAsyncQuery.updateRawJson, result);
            JSONObject queryJSON = new JSONObject(result);
            String strDefinition = (String) queryJSON.get("Definition");
            String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
            broadcastIntent.putExtra(DDGAsyncQuery.updateDefinition, strDefinition);
            broadcastIntent.putExtra(DDGAsyncQuery.updateDefinitionURL, strDefinitionURL);
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse json / update definitions", e);
        }
        if (broadcastManager != null) broadcastManager.sendBroadcast(broadcastIntent);
    }
}