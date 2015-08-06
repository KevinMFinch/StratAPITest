package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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


public class AssignBuddy extends ActionBarActivity {

    EditText editText;
    ArrayAdapter theAdapter;
    ListView theListView;
    String currentText;
    String userApiKey;
    String userID;
    String errorMessage;
    int code;
    int selectedID;
    JSONObject jsonObject;
    JSONArray jsonArray;
    ArrayList<JSONObject> jsonObjectArrayList;
    ArrayList<String> idList;
    ArrayList<String> usernameList;
    ArrayList<String> listStrings;
    RequestBody body;
    User user;
    static final String EXTRA_API = "nyc.monorail.stratapitest.API";
    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_buddy);
        Intent intent = getIntent();
        user = intent.getParcelableExtra(EXTRA_USER);
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        editText = (EditText) findViewById(R.id.buddy_edit_text);
        theListView = (ListView) findViewById(R.id.buddyListView);
        jsonObjectArrayList = new ArrayList<>();
        usernameList = new ArrayList<>();
        listStrings = new ArrayList<>();
        idList = new ArrayList<>();

        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                currentText = editText.getText().toString();
                if (theAdapter != null) {
                    jsonObjectArrayList.clear();
                    listStrings.clear();
                    usernameList.clear();
                    idList.clear();
                    theAdapter.notifyDataSetChanged();
                }
                if (!currentText.matches("")) {
                    new getUsers().execute();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

    }

    class setBuddy extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getPatchURL())
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
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

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (code == 100) {
                Toast.makeText(getApplicationContext(), "Buddy pairing successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Buddy Pairing unsuccessful", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class getUsers extends AsyncTask<Void, Void, Boolean> {
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
            if (code == 100) {
                updateUI();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_assign_buddy, menu);
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
                listStrings.add(obj.get("username").toString());
                usernameList.add(obj.get("username").toString());
                idList.add(obj.get("id").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);


        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedID = Integer.parseInt(idList.get(i));
                AlertDialog.Builder builder = new AlertDialog.Builder(AssignBuddy.this);
                LayoutInflater inflater = AssignBuddy.this.getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout

                builder.setView(inflater.inflate(R.layout.buddy_dialog_layout, null))
                        // Add action buttons
                        .setPositiveButton("Make buddy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                try {
                                    JSONObject temp = new JSONObject();
                                    temp.put("buddy_id", selectedID);
                                    JSONObject tempObject = new JSONObject();
                                    tempObject.put("update", temp);
                                    String jsonString = tempObject.toString();
                                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                                    body = RequestBody.create(JSON, jsonString);

                                    View view = getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }

                                    new setBuddy().execute();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                View view = getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.setMessage("Make " + usernameList.get(i) + " your buddy?");
                dialog.show();

            }
        });
    }

    public HttpUrl getURL() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/users").newBuilder()
                .addQueryParameter("username", currentText + "%")
                .build();
    }

    public HttpUrl getPatchURL() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/users/" + userID);
    }
}
