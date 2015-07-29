package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class DisplayPlayersInPack extends ActionBarActivity {

    HttpUrl url;
    JSONObject jsonObject;
    JSONArray jsonArray;
    String packID;
    String userApiKey;
    String userID;
    ArrayList<JSONObject> jsonObjectArrayList;
    JSONObject paginationObject;
    int currentPage = 1;
    int totalPages;
    ArrayAdapter theAdapter;
    ArrayList<String> listStrings;
    ListView theListView;
    ArrayList<String> firstNameArrayList;
    ArrayList<String> playerIdList;
    ArrayList<Card> cardList;
    ProgressDialog progDialog;
    String baseURLString;
    User user;
    Pack pack;
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";
    static final String EXTRA_PACK = "nyc.monorail.stratapitest.PACK";
    static final String EXTRA_CARD = "nyc.monorail.stratapitest.CARD";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_players_in_pack);
        Intent intent = getIntent();
        pack = (Pack) intent.getParcelableExtra(EXTRA_PACK);
        user = (User) intent.getParcelableExtra(EXTRA_MESSAGE);
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        packID = pack.getPackID();
        jsonObjectArrayList = new ArrayList<>();
        cardList = new ArrayList<>();
        theListView = (ListView) findViewById(R.id.listView);
        listStrings = new ArrayList<>();
        firstNameArrayList = new ArrayList<>();
        playerIdList = new ArrayList<>();

        baseURLString = BaseUrl.getBase() + "/api/v1/user_cards/";
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
                    .addHeader("x-authorization", userApiKey)
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
            String listItemText = "";
            HashMap<String, String> propMap = new HashMap<>();

            try {
                JSONObject obj = new JSONObject(jsonObjectArrayList.get(x).get("card").toString());
                if (obj != null) {
                    listItemText += obj.get("first_name").toString() + " " + obj.get("last_name");
                    firstNameArrayList.add(obj.get("first_name").toString());
                    playerIdList.add(obj.get("card_id").toString());
                    listStrings.add(listItemText);

                    Iterator<?> keys = obj.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        propMap.put(key, obj.get(key).toString());
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            cardList.add(new Card(propMap));

        }


        theAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), DisplayPlayerStats.class);
                intent.putExtra(EXTRA_MESSAGE, user);
                intent.putExtra(EXTRA_CARD, cardList.get(i));
                startActivity(intent);
            }
        });
    }

    public HttpUrl getURL() {
        url = HttpUrl.parse(baseURLString + userID);
        return url.newBuilder()
                .addQueryParameter("pack_id", packID)
                .addQueryParameter("format", "extended")
                .addQueryParameter("page", String.valueOf(currentPage))
                .build();
    }
}
