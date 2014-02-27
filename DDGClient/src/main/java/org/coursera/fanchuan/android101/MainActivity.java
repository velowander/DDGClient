package org.coursera.fanchuan.android101;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements DDGQueryObserver, LoaderManager.LoaderCallbacks {

    private final String TAG = MainActivity.class.getSimpleName();
    private byte queryLoaderId = 0;
    private ProgressDialog dialog;

    private LocalBroadcastManager broadcastManager;
    public final static String JSON_RESULT_INTENT = "org.coursera.fanchuan.android101.jsonresult";
    public final BroadcastReceiver queryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDefinition(intent.getStringExtra(DDGAsyncQuery.UPDATE_DEFINITION));
            updateDefinitionURL(intent.getStringExtra(DDGAsyncQuery.UPDATE_DEFINITION_URL));
            updateRawJson(intent.getStringExtra(DDGAsyncQuery.UPDATE_RAW_JSON));
            updateQueryString(intent.getStringExtra(DDGAsyncQuery.UPDATE_QUERY_STRING));
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        final EditText editSearch = (EditText) findViewById(R.id.editTextSearchWord);
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startQuery();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        broadcastManager.unregisterReceiver(queryReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastManager.registerReceiver(queryReceiver, new IntentFilter(this.JSON_RESULT_INTENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.help_action) {
            Dialog help = new Dialog(this);
            help.setContentView(R.layout.dialog_help);
            help.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    public void onClickRunQuery(View vw) {
        startQuery();
    }

    protected void startQuery() {
        //Get the word to search (send to DDGAsyncQuery)
        final EditText editSearch = (EditText) findViewById(R.id.editTextSearchWord);
        final String searchWord = editSearch.getText().toString();
        //new DDGAsyncQuery(this).execute(searchWord);
        //DDGLoader implementation does not have a
        try {
            this.dialog = new ProgressDialog(this);
            this.dialog.setCancelable(true);
            this.dialog.setIndeterminate(true);
            this.dialog.setTitle(getText(R.string.queryStartedDialogTitle));
            this.dialog.setMessage(getText(R.string.queryStartedDialogText));
            this.dialog.show();
        } catch (Exception e) {
            Log.w(TAG, "startQuery(): unable to show dialog");
        }

        //Create the actual loader that will perform the query submission to remote server
        Bundle args = new Bundle(1);
        args.putString(DDGLoader.KEY_SEARCH_WORD, searchWord);
        /* Quirk in support library implementation of AsyncTaskLoader which baffled me before
        * requires this .forceLoad() method to actually start the loader. This should not be
        * necessary if using the API11+ framework implementation but I haven't tried it.
        * See http://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
        * Also, I found for this application .restartLoader makes sure it fetches new data for every
        * query; with .initLoader on subsequent queries it would return data from the previous query
        * seemingly without even accessing the network */
        getSupportLoaderManager().restartLoader(queryLoaderId, args, this).forceLoad();
    }

    //methods from LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader loader, Object data) {
        if (dialog != null & dialog.isShowing()) {
            dialog.dismiss();
        }
        Intent broadcastIntent = new Intent(MainActivity.JSON_RESULT_INTENT);
        String result = (String) data;
        try {
            Log.i(TAG, "DDG REST API json" + result);
            //Here MainActivity is broadcasting to itself, an artifact of when this code was in another class.
            broadcastIntent.putExtra(DDGLoader.UPDATE_RAW_JSON, result);
            JSONObject queryJSON = new JSONObject(result);
            String strDefinition = (String) queryJSON.get("Definition");
            String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
            broadcastIntent.putExtra(DDGLoader.UPDATE_DEFINITION, strDefinition);
            broadcastIntent.putExtra(DDGLoader.UPDATE_DEFINITION_URL, strDefinitionURL);
        } catch (JSONException e) {
            Log.e(TAG, "Unable to parse json / update definitions", e);
        }
        if (broadcastManager != null) broadcastManager.sendBroadcast(broadcastIntent);
    }

    public Loader onCreateLoader(int id, Bundle args) {
        return new DDGLoader(this, args);
    }

    public void onLoaderReset(Loader loader) {
        //not used
    }

    //methods from @Deprecated DDGQueryObserver interface, however they are in active use:
    public void updateDefinition(String definition) {
        if (definition != null) {
            TextView textView = (TextView) findViewById(R.id.textViewDefinition);
            textView.setText(definition);
        }
    }

    public void updateDefinitionURL(String definitionURL) {
        if (definitionURL != null) {
            TextView textView = (TextView) findViewById(R.id.textViewDefinitionURL);
            textView.setText(definitionURL);
        }
    }

    public void updateRawJson(String rawJson) {
        if (rawJson != null) {
            TextView textView = (TextView) findViewById(R.id.textViewRawJson);
            textView.setText(rawJson);
        }
    }

    public void updateQueryString(String queryString) {
        if (queryString != null) {
            TextView textView = (TextView) findViewById(R.id.textViewQuery);
            textView.setText(queryString);
        }
    }

    static class DDGLoader extends AsyncTaskLoader<String> {
    /* Replacement class for DDGQuery that avoids needing a "wrapper" around the AsyncTask
    * DDGAsyncQuery no longer supports the @Deprecated DDGObserver interface */

        private final String TAG = DDGAsyncQuery.class.getSimpleName();
        private Context context;
        private String searchWord;

        final static String KEY_SEARCH_WORD = "keySearchWord"; //

        //Key strings for Intent containing query data
        final static String UPDATE_QUERY_STRING = "UPDATE_QUERY_STRING";
        final static String UPDATE_DEFINITION = "UPDATE_DEFINITION";
        final static String UPDATE_DEFINITION_URL = "UPDATE_DEFINITION_URL";
        final static String UPDATE_RAW_JSON = "UPDATE_RAW_JSON";

        public DDGLoader(Context context, Bundle args) {
        /* Supplying a non-null Context is strongly recommended
        * If context is null, this class can't report the results to the UI (to the Log only)!! */
            super(context);
            this.context = context;
            this.searchWord = args.getString(KEY_SEARCH_WORD);
        }

        @Override
        public String loadInBackground() {
        /* Runs on worker thread; onPostExecute runs on UI thread
        * params[0] should be searchWord, no other indexes defined */
            final String encodingType = "UTF-8";
            final String queryTemplate = "http://api.duckduckgo.com/?q=define+%s&format=json&t=%s&pretty=1";
            Log.d(TAG, "starting AsyncQuery.doInBackground()");
            if (searchWord == null) searchWord = "";
            try {
                searchWord = URLEncoder.encode(searchWord, encodingType);
                String appName = URLEncoder.encode(TAG, encodingType);
                String queryString = String.format(queryTemplate, searchWord, appName);
                Log.i(TAG, "Sending broadcast");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(MainActivity.JSON_RESULT_INTENT)
                        .putExtra(UPDATE_QUERY_STRING, queryString));
                return HttpGetHelper.execute(queryString);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "doInBackground(): problem encoding URL");
            } catch (NullPointerException e) {
                Log.e(TAG, "doInBackground(): NullPointer");
            }
            return null;
        }
    }
}
