package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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


public class ListsActivity extends ActionBarActivity {

    String userApiKey;
    String userID;
    String listName;
    JSONObject paginationObject;
    JSONObject jsonObject;
    JSONArray jsonArray;
    int currentPage = 1;
    int totalPages;
    int code;
    ArrayList<JSONObject> jsonObjectArrayList;
    ArrayList<Integer> listIDs;
    ArrayList<String> listStrings;
    ArrayAdapter theAdapter;
    ListView theListView;
    RequestBody body;
    User user;
    static final String EXTRA_API = "nyc.monorail.stratapitest.API";
    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";
    static final String EXTRA_LISTID = "nyc.monorail.sratapitest.LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        Intent intent = getIntent();
        user = intent.getParcelableExtra(EXTRA_USER);
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        jsonObjectArrayList = new ArrayList<>();
        listStrings = new ArrayList<>();
        listIDs = new ArrayList<>();
        theListView = (ListView) findViewById(R.id.theListView);
        new getLists().execute();

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
                            jsonObjectArrayList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
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
                    updateUI();
                }

            } else {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lists, menu);
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
        for (int x = 0; x < jsonObjectArrayList.size(); x++) {
            JSONObject obj = jsonObjectArrayList.get(x);
            String listItemText = "";
            try {
                listItemText += obj.get("name").toString();
                listIDs.add((int) obj.get("list_id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listStrings.add(listItemText);
        }

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int selectedID = listIDs.get(i);
                Intent intent = new Intent(getApplicationContext(), DisplayCardsInList.class);
                intent.putExtra(EXTRA_USER, user);
                intent.putExtra(EXTRA_LISTID, selectedID);
                startActivity(intent);


            }
        });
    }

    public HttpUrl getListUrl() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/lists").newBuilder()
                .addQueryParameter("page", String.valueOf(currentPage))
                .addQueryParameter("user_id", userID)
                .build();
    }

    class createList extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(createListUrl())
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
                Toast.makeText(getApplicationContext(), "List Creation Successful", Toast.LENGTH_SHORT).show();
                listStrings.clear();
                jsonObjectArrayList.clear();
                currentPage = 1;
                theAdapter.notifyDataSetChanged();
                listIDs.clear();
                new getLists().execute();
            } else {
                Toast.makeText(getApplicationContext(), "List creation unsuccessful. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void createListDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListsActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = ListsActivity.this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        builder.setView(inflater.inflate(R.layout.list_create_text, null))
                // Add action buttons
                .setPositiveButton("Create List", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog f = (Dialog) dialog;
                        EditText listText = (EditText) f.findViewById(R.id.list_name_text);
                        listName = listText.getText().toString();
                        if (listName.equals("")) {
                            Toast.makeText(getApplicationContext(), "Your list name cannot be empty", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("user_id", userID);
                            hashMap.put("name", listName);
                            JSONObject tempObject = new JSONObject(hashMap);
                            String jsonString = tempObject.toString();

                            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            body = RequestBody.create(JSON, jsonString);
                            new createList().execute();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public HttpUrl createListUrl() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/lists");
    }
}