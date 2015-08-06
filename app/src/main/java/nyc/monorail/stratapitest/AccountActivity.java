package nyc.monorail.stratapitest;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class AccountActivity extends ActionBarActivity {

    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";
    User user;
    String credits;
    String userID;
    String userApiKey;
    JSONObject jsonObject;
    String buddy_credits_given ;
    String buddy_credits_received;
    int code;
    TextView creditText;
    TextView buddyText;
    TextView buddyGiven;
    TextView buddyReceived;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        creditText = (TextView)findViewById(R.id.creditText);
        buddyGiven = (TextView)findViewById(R.id.buddyCreditGivenText);
        buddyReceived = (TextView)findViewById(R.id.buddyCreditReceivedText);

        Intent intent = getIntent();
        user = intent.getParcelableExtra(EXTRA_USER);
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        credits = user.getCredits();
        new getAccountInfo().execute();

    }

    class getAccountInfo extends AsyncTask<Void,Void,Boolean>{
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
                    credits = jsonObject.get("current_total_unused_credits").toString();
                    buddy_credits_given = jsonObject.get("buddy_credits_given").toString();
                    buddy_credits_received = jsonObject.get("buddy_credits_received").toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;

        }

        protected void onPostExecute(Boolean aBoolean) {
            if(code==100){
                updateUI();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account, menu);
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

    public HttpUrl getUrl()
    {
        return HttpUrl.parse(BaseUrl.getBase() + "/purchases/credits");
    }

    public void updateUI(){
        creditText.setText("Credits: "+credits);
        buddyGiven.setText("Buddy Credits Given: "+buddy_credits_given);
        buddyReceived.setText("Buddy Credits Received: "+buddy_credits_received);

    }

    public void logOut(View view)
    {
        user = null;
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
