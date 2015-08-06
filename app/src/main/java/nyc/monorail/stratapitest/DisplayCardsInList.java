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
import android.widget.ListView;
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


public class DisplayCardsInList extends ActionBarActivity {

    Boolean noCardsInList = false;
    String userApiKey;
    String userID;
    int listId;
    int currentPage = 1;
    int totalPages;
    int code;
    int index;
    int selectedCardID;
    JSONObject jsonObject;
    JSONObject paginationObject;
    JSONArray jsonArray;
    JSONArray cardsArray;
    ArrayList<JSONObject> jsonObjectArrayList;
    ArrayList<JSONObject> cardObjectArrayList;
    ArrayList<String> firstNameArrayList;
    ArrayList<String> listStrings;
    ArrayList<String> teamIDList;
    ArrayList<Integer> cardIdList;
    ArrayList<Card> cardList;
    ArrayAdapter theAdapter;
    ListView theListView;
    User user;
    static final String EXTRA_API = "nyc.monorail.stratapitest.API";
    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";
    static final String EXTRA_LISTID = "nyc.monorail.sratapitest.LIST";
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";
    static final String EXTRA_CARD = "nyc.monorail.stratapitest.CARD";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cards_in_list);

        Intent intent = getIntent();
        user = intent.getParcelableExtra(EXTRA_USER);
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        listId = intent.getIntExtra(EXTRA_LISTID, 0);
        jsonObjectArrayList = new ArrayList<>();
        cardObjectArrayList = new ArrayList<>();
        firstNameArrayList = new ArrayList<>();
        cardList = new ArrayList<>();
        listStrings = new ArrayList<>();
        teamIDList = new ArrayList<>();
        cardIdList = new ArrayList<>();
        theListView = (ListView) findViewById(R.id.listView);

        new getCardsInList().execute();
    }

    class getCardInfo extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getCardUrl())
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
                        cardObjectArrayList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;

        }

        protected void onPostExecute(Boolean aBoolean) {
            if (code == 100) {
                if (index < cardIdList.size() - 1) {
                    index++;
                    selectedCardID = cardIdList.get(index);
                    new getCardInfo().execute();
                } else {
                    updateUI();
                }
            } else {
            }
        }

    }

    class getCardsInList extends AsyncTask<Void, Void, Boolean> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getUrl())
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
                    JSONObject tempObject = new JSONObject(String.valueOf(jsonArray.getJSONObject(0)));
                    jsonArray = tempObject.getJSONArray("cards");
                    if (jsonArray != null) {
                        int len = jsonArray.length();
                        if (len > 0) {
                            for (int i = 0; i < len; i++) {
                                jsonObjectArrayList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                            }
                        } else {
                            noCardsInList = true;
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
                    new getCardsInList().execute();
                } else {
                    parseOutData();
                }
            } else {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_cards_in_list, menu);
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

    public void parseOutData() {
        if (!noCardsInList) {
            for (JSONObject obj : jsonObjectArrayList) {
                try {
                    cardIdList.add((int) obj.get("card_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            index = 0;
            selectedCardID = cardIdList.get(index);
            new getCardInfo().execute();

        } else {
            Toast.makeText(getApplicationContext(), "No cards are in this list.", Toast.LENGTH_SHORT).show();
        }

    }

    public void updateUI() {
        for (int x = 0; x < cardObjectArrayList.size(); x++) {
            String listItemText = "";
            HashMap<String,String> propMap = new HashMap<>();

            try {
                JSONObject obj = cardObjectArrayList.get(x);
                if(obj !=null) {
                    listItemText += obj.get("first_name").toString() + " " + obj.get("last_name");
                    firstNameArrayList.add(obj.get("first_name").toString());
                    listStrings.add(listItemText);

                    Iterator<?> keys = obj.keys();

                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        propMap.put(key,obj.get(key).toString());
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
                Card card = cardList.get(i);
                Intent intent = new Intent(getApplicationContext(), DisplayPlayerStats.class);
                intent.putExtra(EXTRA_MESSAGE, user);
                intent.putExtra(EXTRA_CARD,card);
                startActivity(intent);
            }
        });

    }

    public HttpUrl getUrl() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/lists").newBuilder()
                .addQueryParameter("list_id", String.valueOf(listId))
                .addQueryParameter("get_cards", "true")
                .addQueryParameter("page", String.valueOf(currentPage))
                .build();
    }

    public HttpUrl getCardUrl() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/cards").newBuilder()
                .addQueryParameter("card_id", String.valueOf(selectedCardID))
                .build();
    }
}
