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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DisplayPlayerStats extends ActionBarActivity {
    HttpUrl url;
    String baseURLString;
    String playerID;
    String userApiKey;
    String userID;
    String selectedListID;
    ArrayList<String> listNames;
    ArrayList<Integer> listIDs;
    ArrayList<JSONObject> jsonObjectArrayList;
    ArrayList<JSONObject> popupObjectList;
    ArrayList<String> listStrings;
    ListView theListView;
    JSONArray jsonArray;
    JSONObject jsonObject;
    JSONObject paginationObject;
    int currentPage = 1;
    int totalPages;
    int code;
    ArrayAdapter theAdapter;
    PopupMenu popup;
    Button listButton;
    RequestBody body;
    boolean loading = true;
    User user;
    Card card;
    HashMap properties;
    static final String EXTRA_CARD = "nyc.monorail.stratapitest.CARD";
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_player_stats);

        Intent intent = getIntent();
        user = (User) intent.getParcelableExtra(EXTRA_MESSAGE);
        card = (Card) intent.getParcelableExtra(EXTRA_CARD);
        properties = card.getProperties();
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        jsonObjectArrayList = new ArrayList<>();
        popupObjectList = new ArrayList<>();
        listNames = new ArrayList<>();
        listIDs = new ArrayList<>();
        theListView = (ListView) findViewById(R.id.listView);
        listStrings = new ArrayList<>();

        baseURLString = BaseUrl.getBase() + "/api/v1/cards";

        setButtonListener();

        new getLists().execute();
        updateUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_player_stats, menu);
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

        Map<Integer, Integer> map = properties;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            String listItemText = "";
            listItemText += entry.getKey() + ": " + entry.getValue();
            listStrings.add(listItemText);
        }
        playerID = properties.get("card_id").toString();
        theAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    class getLists extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getListUrl())
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
                    jsonArray = jsonObject.getJSONArray("data");
                    if (jsonArray != null) {
                        int len = jsonArray.length();
                        for (int i = 0; i < len; i++) {
                            popupObjectList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                        }
                    }
                }

                paginationObject = new JSONObject(String.valueOf(jsonObject.get("pagination")));
                totalPages = Integer.parseInt(paginationObject.get("total_pages").toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;

        }

        protected void onPostExecute(Boolean aBoolean) {
            if (code == 100) {
                if (currentPage < totalPages) {
                    currentPage++;
                    new getLists().execute();
                } else {
                    for (int x = 0; x < popupObjectList.size(); x++) {
                        JSONObject obj = popupObjectList.get(x);
                        try {
                            listNames.add(obj.get("name").toString());
                            listIDs.add((int) obj.get("list_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
            }
        }
    }

    public void addCard() {
        try {
            JSONObject inside = new JSONObject();
            inside.put("card_id", playerID);

            JSONArray array = new JSONArray();
            array.put(inside);

            JSONObject mainObj = new JSONObject();
            mainObj.put("cards", array);

            String jsonString = mainObj.toString();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            body = RequestBody.create(JSON, jsonString);
            new addCardToList().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setButtonListener() {
        listButton = (Button) findViewById(R.id.add_to_list_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                popup = new PopupMenu(DisplayPlayerStats.this, listButton);

                popup.getMenu().clear();
                for (int x = 0; x < listNames.size(); x++) {
                    popup.getMenu().add(listNames.get(x));
                }

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        selectedListID = String.valueOf(listIDs.get(listNames.indexOf(item.getTitle().toString())));
                        addCard();
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });
    }

    class addCardToList extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(addCardUrl())
                    .post(body)
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean aBoolean) {
            if (code == 101) {
                Toast.makeText(getApplicationContext(), "Added card to list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Did not add card to list", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public HttpUrl getListUrl() {
        return HttpUrl.parse(BaseUrl.getBase() + "/api/v1/lists").newBuilder()
                .addQueryParameter("page", String.valueOf(currentPage))
                .addQueryParameter("user_id", userID)
                .build();
    }

    public HttpUrl addCardUrl() {
        return HttpUrl.parse(BaseUrl.getBase() + "/api/v1/lists/" + selectedListID + "/cards");
    }
}
