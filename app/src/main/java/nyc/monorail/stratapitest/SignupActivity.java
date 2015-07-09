package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.net.MalformedURLException;
import java.net.URL;
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
    String username, email, password;
    String userID;
    String userApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        jsonObjectArrayList = new ArrayList<>();
        baseURLString = "http://chrismobile.strat-o-matic.com/api/v1/users";


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

    class getUserAPIKey extends AsyncTask<Void, Void, Boolean> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getApiUrl())
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

        protected void onPostExecute(Boolean aBoolean) {
            if (code == 100) {
                parseOutUserID();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }

    }

    class createApiKey extends AsyncTask<Void, Void, Boolean> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            URL newurl = null;
            try {
                newurl = new URL("http://chrismobile.strat-o-matic.com/api/v1/auth/"+userID);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Request request = new Request.Builder()
                    .url(newurl)
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
                jsonObject = new JSONObject(jsonObject.get("key").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(code==101 || code == 102)
            {
                parseOutApiKey();
            }
        }
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
                    successMessage = jsonObject.get("message").toString();
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
                Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_SHORT).show();
                new getUserAPIKey().execute();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public HttpUrl getURL() {
        httpUrl = HttpUrl.parse(baseURLString);
        return httpUrl;
    }

    public void signUp(View view) {
        EditText userNameText = (EditText) findViewById(R.id.userNameText);
        username = userNameText.getText().toString();

        EditText emailText = (EditText) findViewById(R.id.emailText);
        email = emailText.getText().toString();

        EditText passwordText = (EditText) findViewById(R.id.passWordText);
        password = passwordText.getText().toString();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", username);
        hashMap.put("email", email);
        hashMap.put("password", password);
        JSONObject tempObject = new JSONObject(hashMap);
        String jsonString = tempObject.toString();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        body = RequestBody.create(JSON, jsonString);

        new createUserPost().execute();

    }

    public void parseOutUserID() {
        for (int x = 0; x < jsonObjectArrayList.size(); x++) {
            JSONObject obj = jsonObjectArrayList.get(x);

            try {
                if(obj.get("username").toString().equals(username))
                {
                    userID=obj.get("id").toString();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        new createApiKey().execute();
    }

    public void parseOutApiKey(){
        try {
            userApiKey = jsonObject.get("key").toString();
            Log.d("HELLO", userApiKey);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public HttpUrl getApiUrl() {
        apiUrl = HttpUrl.parse("http://chrismobile.strat-o-matic.com/api/v1/users");
        return apiUrl;
    }
}
