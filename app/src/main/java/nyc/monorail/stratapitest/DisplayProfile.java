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


public class DisplayProfile extends ActionBarActivity {

    String userApiKey;
    String userID;
    String errorMessage;
    String name;
    String date;
    int code;
    int buddyID;
    JSONObject buddyObj;
    JSONObject jsonObject;
    JSONArray jsonArray;
    User user;
    ArrayList<JSONObject> jsonObjectArrayList;
    ArrayList<JSONObject> buddyList;

    static final String EXTRA_API = "nyc.monorail.stratapitest.API";
    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_profile);

        Intent intent = getIntent();
        user = intent.getParcelableExtra(EXTRA_USER) ;
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        jsonObjectArrayList = new ArrayList<>();
        buddyList = new ArrayList<>();
        new getUserInfo().execute();
    }

    class getBuddy extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getBuddyURL())
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
                buddyObj = new JSONObject(responseString);
                code = (int) buddyObj.get("result_code");
                if (code == 100) {
                    buddyObj = new JSONObject(buddyObj.get("data").toString());
                } else {
                    errorMessage = buddyObj.get("error").toString();
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

    class getUserInfo extends AsyncTask<Void, Void, Boolean> {
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
                    jsonObject = new JSONObject(jsonObject.get("data").toString());
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
                parseData();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_profile, menu);
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

    public void parseData() {
        try {
            name = jsonObject.get("username").toString();
            JSONObject dateObj = new JSONObject(jsonObject.get("member_since").toString());
            date = dateObj.get("date").toString();
            buddyID = (int) jsonObject.get("buddy_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new getBuddy().execute();
    }


    public void updateUI() {
        String buddyName = "";
        try {
            buddyName = buddyObj.get("username").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView nameView = (TextView) findViewById(R.id.userNameDisplay);
        TextView dateView = (TextView) findViewById(R.id.memberSinceDisplay);
        TextView buddyView = (TextView) findViewById(R.id.buddyDisplay);

        nameView.setText("Username: " + name);
        dateView.setText("Member since: " + date);
        buddyView.setText("Your buddy's username: " + buddyName);
    }

    public HttpUrl getURL() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/users/" + userID);
    }

    public HttpUrl getBuddyURL() {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/users/" + buddyID);
    }

    public void startAssignBuddy(View view) {
        Intent intent = new Intent(getApplicationContext(), AssignBuddy.class);
        intent.putExtra(EXTRA_USER, user);
        startActivity(intent);
    }
}
