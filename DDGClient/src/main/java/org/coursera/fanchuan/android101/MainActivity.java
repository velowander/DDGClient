package org.coursera.fanchuan.android101;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements DDGQueryObserver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    @SuppressWarnings("unused")
    public void onClickRunQuery(View vw) {
        startQuery();
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

    protected void startQuery() {
        //Get the word to search (send to DDGQuery)
        final EditText editSearch = (EditText) findViewById(R.id.editTextSearchWord);
        final String searchWord = editSearch.getText().toString();
        //Show status update Toast to user
        //Toast toast = Toast.makeText(this, getText(R.string.queryStartedDialogText), Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.CENTER, 0, 0);
        //toast.show();
        ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(true);
        pd.setIndeterminate(true);
        pd.setTitle(getText(R.string.queryStartedDialogTitle));
        pd.setMessage(getText(R.string.queryStartedDialogText));
        pd.show();
        //ProgressDialog.show(this, getText(R.string.queryStartedDialogTitle), getText(R.string.queryStartedDialogText), true, true);
        new DDGQuery(this, pd).execute(searchWord);
    }

    //methods from DDGQueryObserver interface:
    public void updateDefinition(String definition) {
        TextView textView = (TextView) findViewById(R.id.textViewDefinition);
        textView.setText(definition);
    }

    public void updateDefinitionURL(String definitionURL) {
        TextView textView = (TextView) findViewById(R.id.textViewDefinitionURL);
        textView.setText(definitionURL);
    }

    public void updateRawJson(String rawJson) {
        TextView textView = (TextView) findViewById(R.id.textViewRawJson);
        textView.setText(rawJson);
    }

    public void updateQueryString(String queryString) {
        TextView textView = (TextView) findViewById(R.id.textViewQuery);
        textView.setText(queryString);
    }
}
