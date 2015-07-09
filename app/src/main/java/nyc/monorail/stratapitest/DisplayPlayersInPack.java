package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DisplayPlayersInPack extends ActionBarActivity {

    HttpUrl url;
    JSONObject jsonObject;
    JSONArray jsonArray;
    String teamID;
    ArrayList<JSONObject> jsonObjectArrayList;
    JSONObject paginationObject;
    int currentPage = 1;
    int totalPages;
    ArrayAdapter theAdapter;
    ArrayList<String> listStrings;
    ListView theListView;
    ArrayList<String> firstNameArrayList;
    ProgressDialog progDialog;
    String baseURLString;
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_players_in_pack);
        Intent intent = getIntent();
        teamID = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        jsonObjectArrayList = new ArrayList<>();
        theListView = (ListView) findViewById(R.id.listView);
        listStrings = new ArrayList<>();
        firstNameArrayList = new ArrayList<>();

        baseURLString = "http://chrismobile.strat-o-matic.com/api/v1/cards";
        new getCardsInPack().execute();


    }

    class getCardsInPack extends AsyncTask<Void, Void, Boolean> {

        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(DisplayPlayersInPack.this);
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

                jsonArray = jsonObject.getJSONArray("data");
                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int i = 0; i < len; i++) {
                        jsonObjectArrayList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                    }
                }

                paginationObject = new JSONObject(String.valueOf(jsonObject.get("pagination")));
                totalPages = Integer.parseInt(paginationObject.get("total_pages").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //updateUI();
            progDialog.dismiss();
            if (currentPage < totalPages) {
                currentPage++;
                new getCardsInPack().execute();
            } else {
                updateUI();
            }
        }

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

        for (int x = 0; x < jsonObjectArrayList.size(); x++) {
            JSONObject obj = jsonObjectArrayList.get(x);

            String listItemText = "";

            try {
                listItemText += obj.get("first_name").toString() + " " + obj.get("last_name");
                firstNameArrayList.add(obj.get("first_name").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listStrings.add(listItemText);
        }


        theAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String dataToSend = firstNameArrayList.get(i) + " " + teamID;
                Intent intent = new Intent(getApplicationContext(), DisplayPlayerStats.class);
                intent.putExtra(EXTRA_MESSAGE, dataToSend);
                startActivity(intent);
            }
        });
    }

    public HttpUrl getURL() {
        url = HttpUrl.parse(baseURLString);
        return url.newBuilder()
                .addQueryParameter("mlb_team_id", teamID)
                .addQueryParameter("page", String.valueOf(currentPage))
                .build();
    }
}
