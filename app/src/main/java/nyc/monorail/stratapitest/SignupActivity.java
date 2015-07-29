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
import android.widget.EditText;
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


public class SignupActivity extends ActionBarActivity {

    HttpUrl httpUrl;
    HttpUrl apiUrl;
    String baseURLString;
    JSONObject jsonObject;
    JSONArray jsonArray;
    int code;
    String errorMessage;
    ArrayList<JSONObject> jsonObjectArrayList;
    String successMessage;
    RequestBody body;
    String email, password;
    String userID;
    String userApiKey;
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        jsonObjectArrayList = new ArrayList<>();
        baseURLString = BaseUrl.getBase() + "/api/v1/users/www";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    class createUserPost extends AsyncTask<Void, Void, Boolean> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getURL())
                    .post(body)
                    .addHeader("x-authorization", "e05ebfdadb91ce6937c7341672ef3b72e84b35e3")
                    .addHeader("Content-Type", "form-data")
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
                if (code == 101) {
                    JSONObject createdObject = new JSONObject(jsonObject.get("created").toString());
                    JSONObject apiObject = new JSONObject(createdObject.get("api").toString());
                    userApiKey = apiObject.get("key").toString();
                    userID = apiObject.get("user_id").toString();

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
            if (code == 101) {
                startSeeAvailablePacks();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startSeeAvailablePacks() {
        Intent intent = new Intent(getApplicationContext(), SeeAvailablePacks.class);
        String message = email + " " + userID + " " + userApiKey;
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public HttpUrl getURL() {
        httpUrl = HttpUrl.parse(baseURLString);
        return httpUrl;
    }

    public void signUp(View view) {
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

        new createUserPost().execute();

    }
}
