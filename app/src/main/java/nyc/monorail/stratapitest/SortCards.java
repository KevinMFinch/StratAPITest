package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
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
import java.util.ArrayList;
import java.util.Iterator;


public class SortCards extends ActionBarActivity {

    String baseURLString;
    HttpUrl url;
    String parameter;
    String sortValue;
    JSONObject jsonObject;
    JSONArray jsonArray;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    String itemSelectedInParameterSpinner;
    String itemSelectedInSortSpinner;
    ArrayList<String> displayedPackIDList = new ArrayList<>();
    ArrayAdapter theAdapter;
    ArrayList<String> listStrings;
    ListView theListView;
    ArrayList<String> equalityTesters;
    String itemSelectedInEqualitySpinner;
    int totalPages;
    int currentPage = 1;
    JSONObject paginationObject;
    ArrayList<String> displayedNameList;
    int code;
    String errorMessage;
    String sortString;
    Boolean isSorting = false;
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_cards);
        listStrings = new ArrayList<>();
        displayedNameList = new ArrayList<>();
        initializeSortSpinner();
        addListenerToSortSpinner();
        baseURLString = BaseUrl.getBase() + "/api/v1/cards";

        equalityTesters = new ArrayList<>();
        equalityTesters.add(">");
        equalityTesters.add(">=");
        equalityTesters.add("=");
        equalityTesters.add("<");
        equalityTesters.add("<=");
        initializeParameterSpinner();
        addListenerToParameterSpinner();
        initializeEqualitySpinner();
        addListenerToEqualitySpinner();
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
                if (currentPage < totalPages && !listStrings.isEmpty()) {
                    new getCardRequest().execute();
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

    class getCardRequest extends AsyncTask<Void, Void, Boolean> {

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

                    paginationObject = new JSONObject(String.valueOf(jsonObject.get("pagination")));
                    totalPages = Integer.parseInt(paginationObject.get("total_pages").toString());
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
            if (code == 100) {
                currentPage++;
                updateUI();
            } else {
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

            try {
                displayedPackIDList.add(obj.get("mlb_team_id").toString());
                displayedNameList.add(obj.get("first_name").toString());

                listItemText += obj.get("first_name").toString() + " " + obj.get("last_name").toString() + ", "
                        + obj.get("display_year").toString() + " Team ID: " + obj.get("mlb_team_id") + " "
                        + parameter + ": " + obj.get(parameter).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            listStrings.add(listItemText);
        }

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);

        TextView tV = (TextView) findViewById(R.id.instructionTextView);
        tV.setText("Choose one of these players to see all the stats of that player.");

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String message = displayedNameList.get(i) + " " + displayedPackIDList.get(i);

                Intent intent = new Intent(getApplicationContext(), DisplayPlayerStats.class);
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        });
    }

    public void applyFilter(View view) {
        EditText editText = (EditText) findViewById(R.id.value_edit);
        sortValue = editText.getText().toString();

        String str = itemSelectedInParameterSpinner.toLowerCase();
        str = str.replace(" ", "_");

        parameter = str;

        if(listStrings.size() != 0)
        {
            clearList();
        }

        if (itemSelectedInSortSpinner.equals("No Sort")) {
            isSorting = false;
        } else {
            isSorting = true;
            sortString = "";
            if (itemSelectedInSortSpinner.equals("Ascending")) {
                sortString = parameter + " asc";
            } else {
                sortString = parameter + " dsc";
            }
        }
        new getCardRequest().execute();


    }

    public void initializeParameterSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.parameter_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cardParameterArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void initializeEqualitySpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.equality_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, equalityTesters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void initializeSortSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.sort_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sortOptionsArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void addListenerToParameterSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.parameter_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                itemSelectedInParameterSpinner = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void addListenerToEqualitySpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.equality_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                itemSelectedInEqualitySpinner = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void addListenerToSortSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.sort_type_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                itemSelectedInSortSpinner = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void clearList() {
        displayedPackIDList.clear();
        currentPage = 1;
        jsonObjectArrayList.clear();
        displayedNameList.clear();
        listStrings.clear();
        theAdapter.notifyDataSetChanged();
        theListView.setOnScrollListener(new EndlessScrollListener());
    }

    public HttpUrl getURL() {
        url = HttpUrl.parse(baseURLString);
        if (isSorting) {
            return url.newBuilder()
                    .addQueryParameter("page", String.valueOf(currentPage))
                    .addQueryParameter(parameter, itemSelectedInEqualitySpinner + sortValue)
                    .addQueryParameter("sort", sortString)
                    .build();
        } else {
            return url.newBuilder()
                    .addQueryParameter("page", String.valueOf(currentPage))
                    .addQueryParameter(parameter, itemSelectedInEqualitySpinner + sortValue)
                    .build();
        }

    }
}
