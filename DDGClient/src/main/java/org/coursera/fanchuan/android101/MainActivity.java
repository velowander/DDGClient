package org.coursera.fanchuan.android101;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class MainActivity extends ActionBarActivity {

    final String queryTemplate = "http://api.duckduckgo.com/?q=define+%s&format=json&t=DDGClient&pretty=0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickRunQuery(View vw) {
        EditText editSearch = (EditText) findViewById(R.id.editTextSearchWord);
        String searchWord = (String) editSearch.getText().toString();
        if (!searchWord.isEmpty()) {
            String queryString = String.format(queryTemplate,searchWord);
            TextView txt = (TextView) findViewById(R.id.textViewQuery);
            txt.setText(queryString);
            new runAsyncQuery().execute(queryString);
        }
    }

    private class runAsyncQuery extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //No access to UI in this method, wait for onPostExecute()
            if (params.length >= 1) {
                return getJsonRestAPI(params[0]);
            }
            else return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //This method has access to the UI thread
            Log.d("DDG REST API json",result);
            TextView txtResult = (TextView) findViewById(R.id.textViewResult);
            txtResult.setText(result);
            try {
                JSONObject queryJSON = new JSONObject(result);
                String strDefinition = (String) queryJSON.get("Definition");
                String strDefinitionURL = (String) queryJSON.get("DefinitionURL");
                TextView txtDefinition = (TextView) findViewById(R.id.textViewDefinition);
                txtDefinition.setText(strDefinition);
                TextView txtDefinitionURL = (TextView) findViewById(R.id.textViewDefinitionURL);
                txtDefinitionURL.setText(strDefinitionURL);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getJsonRestAPI(String queryString) {
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


        /*
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        */
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    /**
     * A placeholder fragment containing a simple view.
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    */

}