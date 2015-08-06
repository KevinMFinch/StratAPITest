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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import nyc.monorail.stratapitest.util.Base64;
import nyc.monorail.stratapitest.util.IabHelper;
import nyc.monorail.stratapitest.util.IabResult;
import nyc.monorail.stratapitest.util.Inventory;
import nyc.monorail.stratapitest.util.Purchase;

public class SearchPacksActivity extends ActionBarActivity {

    JSONObject jsonObject;
    JSONObject paginationObject;
    JSONObject creditObject;
    JSONArray jsonArray;
    ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
    ArrayList<JSONObject> franchiseObjectList ;
    String chosenCityShort;
    ArrayList<String> displayedPackIDList = new ArrayList<>();
    ArrayAdapter theAdapter;
    ArrayList<String> listStrings;
    ListView theListView;
    int currentPage = 0;
    int franchisePage = 1 ;
    int totalPages;
    int code;
    int selectedPurchase;
    int counter;
    String errorMessage;
    ArrayList<String> short_name_list;
    ArrayList<String> item_skus;
    ArrayList<String> productNames;
    ArrayList<String> productDescriptions;
    ArrayList<String> productPrices;
    HashMap<String, String> cityMap;
    ArrayList<String> cityNames;
    ArrayList<String> cityShorts;
    String selectedID;
    String successMessage;
    String data;
    String signature;
    HttpUrl httpUrl;
    String year;
    ArrayList<Integer> existingIDList;
    RequestBody body;
    String userApiKey;
    String userID;
    String base64EncodedPublicKey;
    Bundle querySkus;
    URL url;
    HttpURLConnection urlConn;
    InputStream is;
    IabHelper mHelper;
    User user;
    int credits;
    boolean verifySuccessful = false ;
    static final String EXTRA_MESSAGE = "nyc.monorail.stratapitest.MESSAGE";
    static final String EXTRA_LIST = "nyc.monorail.stratapitest.LIST";
    static final String EXTRA_API = "nyc.monorail.stratapitest.API";
    static final String EXTRA_USER = "nyc.monorail.stratapitest.USER";
    private static final String TAG = "nyc.monorail.strat";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_packs);
        listStrings = new ArrayList<>();
        short_name_list = new ArrayList<>();
        item_skus = new ArrayList<>();
        productNames = new ArrayList<>();
        productDescriptions = new ArrayList<>();
        productPrices = new ArrayList<>();
        franchiseObjectList = new ArrayList<>();
        cityNames = new ArrayList<>();
        cityShorts = new ArrayList<>();


        httpUrl = HttpUrl.parse(BaseUrl.getBase()+"/api/v1/packs");
        cityMap = new HashMap<>();
        Intent intent = getIntent();
        existingIDList = intent.getIntegerArrayListExtra(EXTRA_LIST);
        user = intent.getParcelableExtra(EXTRA_USER);
        userApiKey = user.getUserApiKey();
        userID = user.getUserID();
        credits = Integer.parseInt(user.getCredits());
        updateCredits();


        new getFranchises().execute();

        base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQE" +
                "A4GK0UfCYGjuD9qMYlMHfjOW+t+uX++w7lAW5mMaaX4PeuDQlN/MgAU7MDqZ" +
                "mQRlMipxEEtN7exxZXJKt95wT0g75Gwrw1eLFUDRxYPdVruG+BiVjt3iPkhq" +
                "vBv7Jm/1SI58isNlmZEhcezm0hofUNh1hKHLvB0qGZSaMrrJUIrYADup14Wq" +
                "bim39grHjdEBRaUBK3ZrkiRXotrkO1dBGHKWLEv4fAHLlf3h6lprM3ZsU+Rd" +
                "KLmX/dqOgCiYwVIfTDNh66HNY+SGyYX9tHet62JxBiI1Q60YCmT2t7jwBw4X" +
                "6VOhRzl3FFEY9/4RqHN8UQi1RVKRhq59mxDVPSB0nTwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " +
                            result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                    new getJson().execute();
                }
            }
        });

        initializeSpinner();
        addListenerToParameterSpinner();
        theListView = (ListView) findViewById(R.id.listView);
    }

    class getFranchises extends AsyncTask<Void,Void,Boolean>{
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getFranchiseUrl())
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
                            franchiseObjectList.add(new JSONObject(String.valueOf(jsonArray.getJSONObject(i))));
                        }
                    }

                    paginationObject = new JSONObject(String.valueOf(jsonObject.get("pagination")));
                    totalPages = Integer.parseInt(paginationObject.get("total_pages").toString());
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
                if(franchisePage < totalPages)
                {
                    franchisePage++;
                    new getFranchises().execute();
                }
                else
                {
                    parseFranchises();
                }

            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class createUserCards extends AsyncTask<Void, Void, Boolean> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getCreateURL())
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
                if (code == 101) {
                    creditObject = (JSONObject)jsonObject.get("credits");
                    credits = Integer.parseInt(creditObject.get("current_total_unused_credits").toString());
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
            //updateUI();
            if (code == 101) {
                updateCredits();
                Toast.makeText(getApplicationContext(), "Redemption successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class getPackRequest extends AsyncTask<Void, Void, Boolean> {

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
            //updateUI();
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

    public void parseFranchises(){
        int len = franchiseObjectList.size();
        for(int x=0;x<len;x++)
        {
            JSONObject obj = franchiseObjectList.get(x);
            String city_name="";
            String city_short="";
            try {
                city_name = obj.get("name").toString();
                city_short = obj.get("name_short").toString();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            cityNames.add(city_name);
            cityShorts.add(city_short);

        }
        if (cityNames.size() == cityShorts.size()) {
            for (int x = 0; x < cityNames.size(); x++) {
                cityMap.put(cityNames.get(x), cityShorts.get(x));
            }
        }
    }

    public void updateUI() {
        if (theAdapter != null) {
            theAdapter.clear();
            theListView.setAdapter(theAdapter);
        }

        for (int x = 0; x < jsonObjectArrayList.size(); x++) {
            JSONObject obj = jsonObjectArrayList.get(x);
            String listItemText = "";
            try {
                    short_name_list.add(obj.get("name_short").toString());
                    displayedPackIDList.add(obj.get("pack_id").toString());
                    listItemText += obj.get("name").toString() + "\n";
                    listItemText += obj.get("name_short").toString() + "\n";
                    listItemText += "Credit Price: "+obj.get("credit_price").toString()+ "\n";
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                if (!existingIDList.contains(Integer.parseInt(obj.get("pack_id").toString()))) {
                    listStrings.add(listItemText);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        theAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                listStrings);

        theListView.setAdapter(theAdapter);

        TextView tV = (TextView) findViewById(R.id.instructionTextView);
        tV.setText("Click on the pack to buy it.");

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedID = displayedPackIDList.get(i);
                /*Intent intent = new Intent(getApplicationContext(), DisplayPlayersInPack.class);
                intent.putExtra(EXTRA_MESSAGE, selectedID);
                startActivity(intent);*/

                AlertDialog.Builder builder = new AlertDialog.Builder(SearchPacksActivity.this);
                builder.setMessage("Buy this pack?")
                        .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                purchasePack(Integer.parseInt(selectedID));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
    }

    public void initializeSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cityArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void purchasePack(int id) {

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("user_id", userID);
        hashMap.put("pack_id", selectedID);
        JSONObject tempObject = new JSONObject(hashMap);
        String jsonString = tempObject.toString();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        body = RequestBody.create(JSON, jsonString);

        new createUserCards().execute();

    }

    public void applyFilter(View view) {

        EditText editText = (EditText) findViewById(R.id.year_edit);
        year = editText.getText().toString();

        if (!year.isEmpty()) {
            if (listStrings.size() != 0) {
                listStrings.clear();
                jsonObjectArrayList.clear();
                theAdapter.notifyDataSetChanged();
            }
            new getPackRequest().execute();

        } else {
            Toast.makeText(getApplicationContext(), "Please enter a year.", Toast.LENGTH_LONG).show();
        }
    }

    public void addListenerToParameterSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.city_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
                chosenCityShort = cityMap.get(parent.getItemAtPosition(pos).toString());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void clearData(View view) {
        listStrings.clear();
        jsonObjectArrayList.clear();
        theAdapter.notifyDataSetChanged();
    }

    public void startSortCards(View view) {
        Intent intent = new Intent(getApplicationContext(), SortCards.class);
        startActivity(intent);
    }

    public HttpUrl getURL() {
        return httpUrl.newBuilder()
                .setQueryParameter("name_short", chosenCityShort)
                .setQueryParameter("year", year)
                .build();
    }

    public HttpUrl getCreateURL() {
        HttpUrl httpUrl = HttpUrl.parse(BaseUrl.getBase()+"/api/v1/user_cards");
        return httpUrl;
    }

    public void startSignupActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }

    class getJson extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                url = new URL(" http://stagingmobile.strat-o-matic.com/mobile_products.js");
                urlConn = (HttpURLConnection) url.openConnection();

                is = new BufferedInputStream(urlConn.getInputStream());
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                jsonArray = new JSONArray(responseStrBuilder.toString());

                if (jsonArray != null) {
                    int len = jsonArray.length();
                    for (int i = 0; i < len; i++) {
                        item_skus.add(jsonArray.get(i).toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "In onPostExecute");
            fetchPurchases();
        }


    }

    class verifyPurchase extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(getVerifyUrl())
                    .post(body)
                    .addHeader("x-authorization", userApiKey)
                    .build();

            Response response = null;
            String responseString = "";
            try {
                response = client.newCall(request).execute();
                responseString = response.body().string();
                successMessage = responseString;
                Log.d("RESPONSE",successMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                jsonObject = new JSONObject(responseString);
                code = (int) jsonObject.get("result_code");
                if (code == 100)
                {
                    credits = Integer.parseInt(jsonObject.get("current_total_unused_credits").toString());
                    verifySuccessful = true;
                }
                else
                    errorMessage = jsonObject.get("ERROR").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_SHORT).show();
            updateCredits();
            consumeItem();
        }
    }

    public void fetchPurchases() {
        Log.d(TAG, "In fetch purchases");
        querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", item_skus);
        mHelper.queryInventoryAsync(true, item_skus, mQueryFinishedListener);
    }

    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;

            if (result.isFailure()) {
                // handle error
                return;
            } else {
                Log.d(TAG, "In mQueryFinishedListener");
                Log.d(TAG, "getting item info");
                String productName = "";
                String productPrice = "";
                String productDescription = "";

                for (int x = 0; x < item_skus.size(); x++) {
                    if (inventory.hasDetails(item_skus.get(x))) {
                        productName = inventory.getSkuDetails(item_skus.get(x)).getTitle();
                        productPrice = inventory.getSkuDetails(item_skus.get(x)).getPrice();
                        productDescription = inventory.getSkuDetails(item_skus.get(x)).getDescription();
                        productNames.add(productName);
                        productPrices.add(productPrice);
                        productDescriptions.add(productDescription);
                    }

                }

                for (int x = 0; x < item_skus.size(); x++) {
                    if (inventory.hasPurchase(item_skus.get(x))) {
                        mHelper.consumeAsync(inventory.getPurchase(item_skus.get(x)), mConsumeFinishedListener);
                    }
                }
                if (inventory.hasPurchase("android.test.purchased")) {
                    mHelper.consumeAsync(inventory.getPurchase("android.test.purchased"), mConsumeFinishedListener);
                }

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else {
                data = purchase.getOriginalJson();
                signature = purchase.getSignature();
                data = Base64.encode(data.getBytes());
                signature = Base64.encode(signature.getBytes());

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("receipt", data);
                hashMap.put("signature", signature);
                hashMap.put("product_sku", item_skus.get(selectedPurchase));
                hashMap.put("android","1");
                JSONObject tempObject = new JSONObject(hashMap);
                String jsonString = tempObject.toString();

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                body = RequestBody.create(JSON, jsonString);
                new verifyPurchase().execute();
            }
        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {
                for (int x = 0; x < item_skus.size(); x++) {
                    if (inventory.hasPurchase(item_skus.get(x))) {
                        Log.d(TAG, "Consuming items");
                        mHelper.consumeAsync(inventory.getPurchase(item_skus.get(x)), mConsumeFinishedListener);
                    }
                }

            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (mHelper == null) return;

            if (result.isSuccess()) {
                if(verifySuccessful) {
                    verifySuccessful = false;
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Verification unsuccessful.",Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public void openBuyTokens(View view)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SearchPacksActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.purchase_dialog_layout, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Purchases...");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,productNames);
        alertDialog.setSingleChoiceItems(adapter, 1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                buyItem(item_skus.get(position));
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    public HttpUrl getVerifyUrl() {
        return HttpUrl.parse(BaseUrl.getBase()+"/purchases/verify");

    }

    public void buyItem(String item_sku)
    {
        mHelper.launchPurchaseFlow(this, item_sku, 10001, mPurchaseFinishedListener, "mypurchasetoken");
    }

    public HttpUrl getFranchiseUrl()
    {
        return HttpUrl.parse(BaseUrl.getBase()+"/api/v1/franchises").newBuilder()
                .addQueryParameter("page",String.valueOf(franchisePage))
                .build();
    }

    public void updateCredits()
    {
        TextView countText = (TextView)findViewById(R.id.countText);
        countText.setText("You have "+credits+" credits.");
    }
}