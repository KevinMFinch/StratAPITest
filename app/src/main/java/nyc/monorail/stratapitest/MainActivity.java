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
import android.widget.EditText;

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


public class MainActivity extends ActionBarActivity {

    String email;
    String password;
    String errorMessage;
    User user;
    String userID;
    String userApiKey;
    String credits;
    HttpUrl url;
    JSONArray jsonArray;
    JSONObject jsonObject;
    JSONObject creditObject;
    int code;
    RequestBody body;
    ArrayList<JSONObject> jsonObjectArrayList;
    ProgressDialog progDialog;

    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jsonObjectArrayList = new ArrayList<>();



    }

    class loginRequest extends AsyncTask<Void, Void, Boolean> {

        protected void onPreExecute()
        {
            progDialog = ProgressDialog.show(MainActivity.this, "Please wait ...", "Signing in...", true);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getURL())
                    .post(body)
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
                    creditObject = (JSONObject) jsonObject.get("credits");
                    jsonObject = (JSONObject) jsonObject.get("api_key");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;

        }

        protected void onPostExecute(Boolean aBoolean) {
            progDialog.dismiss();
            if (code == 100) {
                parseOutData();
            } else {
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

    public void parseOutData() {
        try {
            userID = jsonObject.get("user_id").toString();
            userApiKey = jsonObject.get("key").toString();
            credits = creditObject.get("current_total_unused_credits").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startSeeAvailablePacks();

    }


    public void startSeeAvailablePacks() {
        Intent intent = new Intent(getApplicationContext(), SeeAvailablePacks.class);
        user = new User(userID, userApiKey, credits);
        intent.putExtra(EXTRA_MESSAGE, user);
        startActivity(intent);
    }

    public void startLogin(View view) {
        EditText emailText = (EditText) findViewById(R.id.emailText);
        email = emailText.getText().toString();

        EditText passwordText = (EditText) findViewById(R.id.passWordText);
        password = passwordText.getText().toString();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("email", email);
        hashMap.put("pass", password);
        JSONObject tempObject = new JSONObject(hashMap);
        String jsonString = tempObject.toString();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        body = RequestBody.create(JSON, jsonString);

        new loginRequest().execute();
    }

    public void startSignup(View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);

    }

    public HttpUrl getURL() {
        return HttpUrl.parse(BaseUrl.getBase() + "/api/v1/users/www/session");
    }


}
