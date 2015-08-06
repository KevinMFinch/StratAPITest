package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.Iterator;


public class SeeAvailablePacks extends ActionBarActivity {

    String email;
    String userID;
    String userApiKey;
    String errorMessage;
    HttpUrl url;
    JSONObject jsonObject;
    JSONObject paginationObject;
    JSONArray jsonArray;
    int code;
    int currentPage = 1;
    int totalPages;
    ArrayList<String> short_name_list;
    ArrayList<String> displayedPackIDList;
    ArrayList<String> listStrings;
    ArrayList<JSONObject> jsonObjectArrayList;
    ArrayList<JSONObject> packObjectArrayList;
    ArrayList<Integer> packIDList;
    ArrayList<Pack> packList;
    ArrayAdapter theAdapter;
    ListView theListView;
    User user;
    Pack pack;

    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";
    static final String EXTRA_LIST = "nyc.monorail.stratapitest.LIST";
    static final String EXTRA_API = "nyc.monorail.stratapitest.API";
    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";
    static final String EXTRA_PACK = "nyc.monorail.stratapitest.PACK";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_see_available_packs);

        Intent startIntent = getIntent();
        user =startIntent.getParcelableExtra(EXTRA_MESSAGE);

        userID = user.getUserID();
        userApiKey = user.getUserApiKey();
        jsonObjectArrayList = new ArrayList<>();
        packObjectArrayList = new ArrayList<>();
        packIDList = new ArrayList<>();
        short_name_list = new ArrayList<>();
        displayedPackIDList = new ArrayList<>();
        listStrings = new ArrayList<>();
        packList = new ArrayList<>();

        theListView = (ListView) findViewById(R.id.theListView);


        new getPacks().execute();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        currentPage = 1;
        listStrings.clear();
        jsonObjectArrayList.clear();
        packObjectArrayList.clear();
        packIDList.clear();
        short_name_list.clear();
        packList.clear();
        displayedPackIDList.clear();
        theAdapter.notifyDataSetChanged();

        new getPacks().execute();

    }

    class getPacks extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getPackUrl())
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
                code = (int) jsonObject.get("result_code");
                if (code == 100) {
                    jsonArray = jsonObject.getJSONArray("packs");
                    if (jsonArray != null) {
                        int len = jsonArray.length();
                        for (int i = 0; i < len; i++) {
                            packObjectArrayList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                        }
                    }

                    paginationObject = new JSONObject(String.valueOf(jsonObject.get("pagination")));
                    totalPages = Integer.parseInt(paginationObject.get("total_pages").toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;

        }

        protected void onPostExecute(Boolean aBoolean) {
            if (code == 100) {
                if (currentPage < totalPages) {
                    currentPage++;
                    new getPacks().execute();

                }
                else
                    updateUI();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_see_available_packs, menu);
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

        for (int x = 0; x < packObjectArrayList.size(); x++) {
            JSONObject obj = packObjectArrayList.get(x);
            Iterator<?> keys = obj.keys();
            HashMap<String,String> propMap = new HashMap<String, String>();

            String listItemText = "";
            String activeID="";

            while (keys.hasNext()) {
                String key = (String) keys.next();

                try {
                    if (key.equals("name_short")) {
                        short_name_list.add(obj.get(key).toString());
                    }
                    if (key.equals("pack_id")) {
                        displayedPackIDList.add(obj.get(key).toString());
                        activeID = obj.get(key).toString();
                    }
                    if (key.equals("name")) {
                        listItemText += obj.get(key).toString() + "\n";
                    }
                    if (key.equals("name_short")) {
                        listItemText += obj.get(key).toString();
                    }
                    propMap.put(key,obj.get(key).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            packList.add(new Pack(activeID,propMap));
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
                Pack packToSend = packList.get(i);
                Intent intent = new Intent(getApplicationContext(), DisplayPlayersInPack.class);
                intent.putExtra(EXTRA_MESSAGE, user);
                intent.putExtra(EXTRA_PACK,packToSend);
                startActivity(intent);
            }
        });
    }

    public HttpUrl getPackUrl() {
        HttpUrl packURL = HttpUrl.parse(BaseUrl.getBase()+"/api/v1/packs/user/"+userID);
        return packURL.newBuilder()
                .addQueryParameter("page", String.valueOf(currentPage))
                .build();
    }

    public void openSearchPacksActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SearchPacksActivity.class);
        intent.putIntegerArrayListExtra(EXTRA_LIST, packIDList);
        intent.putExtra(EXTRA_USER, user);
        startActivity(intent);
    }

    public void startListActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ListsActivity.class);
        intent.putExtra(EXTRA_USER, user);
        startActivity(intent);
    }

    public void startProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), DisplayProfile.class);
        intent.putExtra(EXTRA_USER, user);
        startActivity(intent);
    }

    public void startAccount(View view){
        Intent intent = new Intent(getApplicationContext(),AccountActivity.class);
        intent.putExtra(EXTRA_USER,user);
        startActivity(intent);
    }
}