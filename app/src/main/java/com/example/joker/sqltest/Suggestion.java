package com.example.joker.sqltest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Suggestion extends AppCompatActivity implements SuggestionAdapeter.SuggestionCallBack {

    GPSTracker gps;

   // private static final String URL = "https://" + MainActivity.IP + "/main/api/mobile/getsugg.php?";
    private static final String TAG = Suggestion.class.getSimpleName();


    ProgressDialog pd;
    ArrayList<SuggestionModel> suggestions = new ArrayList<>();
    ListView listView;
    SuggestionAdapeter adapter;
    LinearLayout noDataTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion);

        listView = (ListView) findViewById(R.id.listView);
        noDataTV = (LinearLayout) findViewById(R.id.nodataTextView);
        updateListView();

        /*TO get the lat and long of ther user*/

        gps = new GPSTracker(Suggestion.this);

        //check if GPS enabled
        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

//            String url = URL + "latitude=" + latitude + "&longitude=" + longitude;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(MainActivity.IP)
                    .appendPath("main")
                    .appendPath("api")
                    .appendPath("mobile")
                    .appendPath("getsugg.php")
                    .appendQueryParameter("latitude", String.valueOf(latitude))
                    .appendQueryParameter("longitude", String.valueOf(longitude));
            String url = builder.build().toString();

            Log.d(TAG, " lat:" + latitude + " " + " log" + longitude);
            new AsyncFetch().execute(url);

        } else {
            gps.showSettingsAlert();
        }


    }


    //this is to update listView with suggestions.
    private void updateListView() {

        if (suggestions.size() == 0 || suggestions == null) {
            listView.setVisibility(View.GONE);
            noDataTV.setVisibility(View.VISIBLE);
        } else {
            adapter = new SuggestionAdapeter(suggestions, Suggestion.this);
            listView.setAdapter(adapter);
            adapter.setCallback(this);
            listView.setVisibility(View.VISIBLE);
            noDataTV.setVisibility(View.GONE);
        }


    }


    //method to Start Google Map on provided Latitude and Longitude
    public void startMap(double lat, double log, String s) {

        double latitude = lat;
        double longitude = log;
        String label = s;
        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }


    //interface method to startMap on button click
    @Override
    public void openGmap(SuggestionModel suggestion) {
        double lat = Double.parseDouble(suggestion.getLat());
        double log = Double.parseDouble(suggestion.getLog());
        String name = suggestion.getShop_name();
        startMap(lat, log, name);
    }


    //Async task to fetch the suggestion from  give url
    private class AsyncFetch extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Suggestion.this);
            pd.setTitle("Fetching");
            pd.setMessage("Please Wait...");
            pd.setCancelable(true);
            pd.show();
        }

        //doInBackground
        @Override
        protected String doInBackground(String... params) {

            String url = params[0];

            String response = HttpHandler.makeServiceCall(url);
            return response;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            pd.dismiss();
            ParseJson(s);
            updateListView();
        }

        private void ParseJson(String s) {

            if (s != null) {
                try {
                    JSONObject response = new JSONObject(s);
                    boolean status = response.getBoolean("status");
                    int statusCode = response.getInt("status_code");

                    if (status) {
                        if (statusCode == 200) {
                            int size = response.getInt("size");
                            for (int i = 0; i < size; i++) {
                                JSONObject item = response.getJSONObject("" + i);

                                String name = item.getString("name");
                                String info = item.getString("info");
                                String url = item.getString("url");
                                String lat = item.getString("latitude");
                                String log = item.getString("longitude");
                                String type = item.getString("type");

                                SuggestionModel suggestion = new SuggestionModel();

                                suggestion.setShop_name(name);
                                suggestion.setShop_info(info);
                                suggestion.setUrl(url);
                                suggestion.setLat(lat);
                                suggestion.setLog(log);
                                suggestion.setType(type);

                                suggestions.add(suggestion);

                            }
                        } else if (statusCode == 300) {
                            Toast.makeText(Suggestion.this, "No Suggestions near you.", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        if (statusCode == 400) {
                            Toast.makeText(Suggestion.this, "Invalid response. Sorry", Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "error " + e);
                }
            } else {
                Toast.makeText(Suggestion.this, "Null response", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
