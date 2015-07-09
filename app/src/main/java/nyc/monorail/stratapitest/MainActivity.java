package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    JSONObject jsonObject;
    JSONArray jsonArray;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    String chosenCityShort;
    ArrayList<String> displayedPackIDList = new ArrayList<>();
    ArrayAdapter theAdapter;
    ArrayList<String> listStrings;
    ListView theListView;
    int currentPage = 0;
    int totalPages;
    ProgressDialog progDialog;
    int code;
    String errorMessage;
    ArrayList<String> short_name_list;
    HashMap<String, String> cityMap;
    String[] cityNames;
    String[] cityShorts;
    HttpUrl httpUrl;
    String year;
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listStrings = new ArrayList<>();
        short_name_list = new ArrayList<>();
        httpUrl = HttpUrl.parse("http://chrismobile.strat-o-matic.com/api/v1/packs");
        cityMap = new HashMap<>();

        cityNames = getResources().getStringArray(R.array.cityArray);
        cityShorts = getResources().getStringArray(R.array.shortNameList);

        if (cityNames.length == cityShorts.length) {
            for (int x = 0; x < cityNames.length; x++) {
                cityMap.put(cityNames[x], cityShorts[x]);
            }
        }

        initializeSpinner();
        addListenerToParameterSpinner();
        theListView = (ListView) findViewById(R.id.listView);
        theListView.setOnScrollListener(new EndlessScrollListener());
    }

    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 3;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                if (currentPage < totalPages) {
                    new getPackRequest().execute();
                    loading = true;
                } else {
                    String str = "There is no more data to display";
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    class getPackRequest extends AsyncTask<Void, Void, Boolean> {

        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(MainActivity.this);
            progDialog.setMessage("Loading...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();

        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getURL())
                    .get()
                    .addHeader("x-authorization", "e05ebfdadb91ce6937c7341672ef3b72e84b35e3")
                    .build();

            Response response = null;
            String responseString = "";
            try {
                response = client.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                jsonObject = new JSONObject(responseString);
                code = (int) jsonObject.get("result_code");
                if (code == 100) {
                    jsonArray = jsonObject.getJSONArray("data");
                    if (jsonArray != null) {
                        int len = jsonArray.length();
                        for (int i = 0; i < len; i++) {
                            jsonObjectArrayList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                        }
                    }
                } else {
                    errorMessage = jsonObject.get("error").toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //updateUI();
            if (code == 100) {
                progDialog.dismiss();
                updateUI();
            } else {
                progDialog.dismiss();
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        if (theAdapter != null) {
            theAdapter.clear();
            theListView.setAdapter(theAdapter);
        }

        for (int x = 0; x < jsonObjectArrayList.size(); x++) {
            JSONObject obj = jsonObjectArrayList.get(x);
            Iterator<?> keys = obj.keys();

            String listItemText = "";

            while (keys.hasNext()) {
                String key = (String) keys.next();

                try {
                    if (key.equals("name_short")) {
                        short_name_list.add(obj.get(key).toString());
                    }
                    if (key.equals("team_id")) {
                        displayedPackIDList.add(obj.get(key).toString());
                    }
                    if (key.equals("name")) {
                        listItemText += obj.get(key).toString() + "\n";
                    }
                    if (key.equals("name_short")) {
                        listItemText += obj.get(key).toString();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            listStrings.add(listItemText);
        }

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);

        TextView tV = (TextView) findViewById(R.id.instructionTextView);
        tV.setText("Click on the deck to see all the cards in the deck.");

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedID = displayedPackIDList.get(i);
                Intent intent = new Intent(getApplicationContext(), DisplayPlayersInPack.class);
                intent.putExtra(EXTRA_MESSAGE, selectedID);
                startActivity(intent);
            }
        });
    }

    public void initializeSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cityArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void applyFilter(View view) {

        EditText editText = (EditText) findViewById(R.id.year_edit);
        year = editText.getText().toString();

        if (!year.isEmpty()) {
            if (listStrings.size() != 0) {
                listStrings.clear();
                jsonObjectArrayList.clear();
                theAdapter.notifyDataSetChanged();
            }
            new getPackRequest().execute();

        } else {
            Toast.makeText(getApplicationContext(), "Please enter a year.", Toast.LENGTH_LONG).show();
        }
    }

    public void addListenerToParameterSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.city_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                chosenCityShort = cityMap.get(parent.getItemAtPosition(pos).toString());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void clearData(View view) {
        listStrings.clear();
        jsonObjectArrayList.clear();
        theAdapter.notifyDataSetChanged();
    }

    public void startSortCards(View view) {
        Intent intent = new Intent(getApplicationContext(), SortCards.class);
        startActivity(intent);
    }

    public HttpUrl getURL() {
        return httpUrl.newBuilder()
                .setQueryParameter("name_short", chosenCityShort)
                .setQueryParameter("year", year)
                .build();
    }

    public void startSignupActivity(View view)
    {
        Intent intent = new Intent(getApplicationContext(),SignupActivity.class);
        startActivity(intent);
    }
}
