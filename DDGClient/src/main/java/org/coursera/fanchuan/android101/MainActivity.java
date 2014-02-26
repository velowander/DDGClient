package org.coursera.fanchuan.android101;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity implements DDGQueryObserver {

    private LocalBroadcastManager broadcastManager;
    public final static String JSON_RESULT_INTENT = "org.coursera.fanchuan.android101.jsonresult";
    public final BroadcastReceiver queryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDefinition(intent.getStringExtra(DDGAsyncQuery.updateDefinition));
            updateDefinitionURL(intent.getStringExtra(DDGAsyncQuery.updateDefinitionURL));
            updateRawJson(intent.getStringExtra(DDGAsyncQuery.updateRawJson));
            updateQueryString(intent.getStringExtra(DDGAsyncQuery.updateQueryString));
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
        new DDGAsyncQuery(this).execute(searchWord);
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
}
