package com.example.joker.sqltest;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ListAdapter.EditContactDetailsCallBack, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String IP = "smartqlabs.com";

    private static final String URL = "http://" + IP + "/main/api/mobile/";

    private DrawerLayout drawerLayout;
    private FloatingActionButton fab;
    private LinearLayout noDataTV;
    private ListView listView;

    private ImageView imageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private UserInfo userInfo;

    ProgressDialog pd;

//    private EditText otpTV;
//    private Button scanBtn;

    private Handler uihandler = new Handler();
    private ListAdapter listAdapter;

    protected SQLiteHelper sqLiteHelper;

    private ArrayList<QueueModel> queues = new ArrayList<>();

    private GoogleApiClient googleApiClient;
    private IntentIntegrator qrScan;
    AlertDialog alertDialog;

    //sanu notification manager variables
    NotificationManager notificationManager;
    private int notifynum = 001;
    final long period = 5000;
    QueueModel refreshQueue;
    ArrayList<QueueModel> ResponseRefreshList = new ArrayList<>();

    public GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noDataTV = (LinearLayout) findViewById(R.id.nodataTextView);
        listView = (ListView) findViewById(R.id.listView);

        sqLiteHelper = new SQLiteHelper(MainActivity.this);

        userInfo = new UserInfo();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //updating the listView.
        updateListView();


        //for toolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                new RefreshTask().execute();

            }
        }, 0, period);


        //for FloatingActionButton for prompt for otp and scan
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                qrScan = new IntentIntegrator(MainActivity.this);
                qrScan.initiateScan();

                /*This is alertDialog for otp*/
//                View promptView = LayoutInflater.from(MainActivity.this).inflate(R.layout.people_details_prompt, null);
//                final AlertDialog.Builder alerDialogBuilder = new AlertDialog.Builder(MainActivity.this);
//                alerDialogBuilder.setView(promptView);
//
//                otpTV = (EditText) promptView.findViewById(R.id.otp_codeTV);
//                scanBtn = (Button) promptView.findViewById(R.id.scanBtn);
//
//
//                //scan button click
//                scanBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                    }
//                });
//
//                alerDialogBuilder.setCancelable(false)
//                        .setPositiveButton(" ADD ", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                String otp = otpTV.getText().toString().trim();
//
//                                if (otp.length() == 0) {
//                                    Toast.makeText(MainActivity.this, "Please input valid details.", Toast.LENGTH_LONG).show();
//
//                                } else {
//                                    //performing async task
//                                    //new AsyncTaks().execute(otp);
//
//                                }
//
//                            }
//                        })
//                        .setNegativeButton(" cancel ", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(MainActivity.this, " Contact not inserted. ", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                //create alert Dialog
//                alertDialog = alerDialogBuilder.create();
//
//                //show it.
//                alertDialog.show();
//

            }
        });


        //for DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        //handling the clicks on DrawerItems
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                drawerLayout.closeDrawer(GravityCompat.START);

                switch (id) {

                    case R.id.show_me:
                        gps = new GPSTracker(MainActivity.this);
                        if (gps.canGetLocation()) {
                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();
                            startMap(latitude, longitude, userInfo.getName());
                        } else {
                            gps.showSettingsAlert();
                        }
                        break;  //this is for about_me

                    case R.id.suggestion:
                        startActivity(new Intent(MainActivity.this, Suggestion.class));
                        break;  //This is for suggestion


                    case R.id.about_us:
                        //TODO , pending to add new activity about team
                        break;

                    case R.id.share:{
                        ApplicationInfo app = getApplicationContext().getApplicationInfo();
                        String filePath = app.sourceDir;

                        Intent intent = new Intent(Intent.ACTION_SEND);

                        // MIME of .apk is "application/vnd.android.package-archive".
                        // but Bluetooth does not accept this. Let's use "*/*" instead.
                        intent.setType("*/*");


                        // Append file and send Intent
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
                        startActivity(Intent.createChooser(intent, "Share app via"));

                    }

                        break;

                    case R.id.logout:
                        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess())
                                    goLogInScreen();
                                else {
                                    Toast.makeText(getApplicationContext(), " Could not Logout Plesase try again. ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break;   // This si for logout.

                }

                return true;
            }
        });

        //for navigation by setting email and name
        View hView = navigationView.getHeaderView(0);

        imageView = (ImageView) hView.findViewById(R.id.imageViewNav);
        nameTextView = (TextView) hView.findViewById(R.id.nameTextView);
        emailTextView = (TextView) hView.findViewById(R.id.emailTextView);


    }

    //whent use clicks logout .
    private void goLogInScreen() {

        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    //to update ListView WhenEver there is a change in record
    public void updateListView() {

        queues = sqLiteHelper.getAllRecords();

        if (queues.size() == 0 || queues == null) {
            listView.setVisibility(View.GONE);
            noDataTV.setVisibility(View.VISIBLE);
        } else {
            listAdapter = new ListAdapter(queues, MainActivity.this, uihandler);
            listAdapter.setCallback(this);
            listView.setAdapter(listAdapter);
            listView.setVisibility(View.VISIBLE);
            noDataTV.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Fount ", Toast.LENGTH_SHORT).show();
            } else {
                //if qr contains data;

                String sha1code = result.getContents();
                //Toast.makeText(this, sha1code, Toast.LENGTH_SHORT).show();

                //sending data after scan
                new AsyncTaks().execute(sha1code);

                Log.d(TAG,"SHA "+sha1code);


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //onBackPress
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount accoutn = result.getSignInAccount();
            userInfo.setName(accoutn.getDisplayName());
            userInfo.setEmail(accoutn.getEmail());
            userInfo.setId(accoutn.getId());
            Uri image = accoutn.getPhotoUrl();
            if (image != null) {
                userInfo.setPhotourl(accoutn.getPhotoUrl().toString());
            }

            if (userInfo.getPhotourl() != null) {
                Glide.with(getApplicationContext()).load(userInfo.getPhotourl()).into(imageView);
            }
            nameTextView.setText(userInfo.getName());
            emailTextView.setText(userInfo.getEmail());


        } else {
            Log.e(TAG, "Error " + result);
            goLogInScreen();
        }

    }

    //on Option Item Selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Handle action bar item clicks here. The action bar will
        //automatically handle clicks on the Home/Up button ,so long
        //as you specify a parent activity in AndroidManifest.xml

        int id = item.getItemId();

        switch (id) {
            //inflation the drawer layout.
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
//            case R.id.action_settings:
//                return true;        //this is to handle option menu options
        }

        return super.onOptionsItemSelected(item);
    }

    //delete queue from list ....interface override function
    @Override
    public void cancelQueue(QueueModel queue) {
        queue.setCode(2);

        String id = queue.getSanuid();
        String store_id = queue.getStore_id();
        String counter_id = queue.getCounter_no();
        String email = userInfo.getEmail();

        //send sanu a cancel request with these credentials

        //localhost/main/api/mobile/cancel.php?id=6&email=sanu@gmail.com&store_id=hellodata&counter_id=2


        // String cancelUrl = URL + "cancel.php?id=" + queue.getSanuid() + "&email=" + userInfo.getEmail() + "&store_id=" + queue.getStore_id() + "&counter_id=" + queue.getCounter_no();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority(IP)
                .appendPath("main")
                .appendPath("api")
                .appendPath("mobile")
                .appendPath("cancel.php")
                .appendQueryParameter("id", queue.getSanuid())
                .appendQueryParameter("email", userInfo.getEmail())
                .appendQueryParameter("store_id", queue.getStore_id())
                .appendQueryParameter("counter_id", queue.getCounter_no());

        String cancelUrl = builder.build().toString();

        new AsyncTakscancel(queue).execute(cancelUrl);

    }

    @Override
    public void navPlace(QueueModel contact) {

        double lat = Double.parseDouble(contact.getLat());
        double log = Double.parseDouble(contact.getLog());
        String name = contact.getShop_name();
        startMap(lat, log, name);
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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //async task to fetch the queue data after scan.
    class AsyncTaks extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Fetching");
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected String doInBackground(String... params) {

            String qrdata = params[0];

            // http://172.21.5.167/main/api/mobile/scan.php?email=shriomtripati33@gmail.cmo&qr_data=hellodata&name=shriom

            String name = userInfo.getName();

//            int f = name.indexOf(" ");
//            if (f != -1) {
//                name = name.substring(0, f);
//            } else {
//                name = userInfo.getEmail().substring(0, userInfo.getEmail().indexOf("@"));
//            }

//          String url = URL + "scan.php?email=" + userInfo.getEmail() + "&name=" + name + "" + "&qr_data=" + qrdata;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(IP)
                    .appendPath("main")
                    .appendPath("api")
                    .appendPath("mobile")
                    .appendPath("scan.php")
                    .appendQueryParameter("email", userInfo.getEmail())
                    .appendQueryParameter("qr_data", qrdata)
                    .appendQueryParameter("name", userInfo.getName());
            String url = builder.build().toString();

            String response = HttpHandler.makeServiceCall(url);
            Log.d(TAG, " URL " + url);
            // Log.d(TAG, " Response " + response);

            return response;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseJSON(s);
            pd.dismiss();

            int size = queues.size();
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    QueueModel notifyQueue = queues.get(i);
                    if (Integer.parseInt(notifyQueue.getQueue_no()) < 6) {
                        showNotification(notifyQueue);
                    }
                }
            }
        }
    }

    //function to show notification on a single queue details
    private void showNotification(QueueModel notifyQueue) {

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(MainActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Queue Status:" + notifyQueue.getShop_name())
                .setContentText("Queue No. " + (notifyQueue.getQueue_no()))
                .setAutoCancel(true)
                .setOngoing(false);  ///this is just try so that notification baar is cancelable...

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifynum, mBuilder.build());

//        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,100);
//        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,200);
//        toneG.stopTone();

    }

    //async task to cancel the queue from the list.
    class AsyncTakscancel extends AsyncTask<String, Void, String> {

        QueueModel queue;

        public AsyncTakscancel(QueueModel queue) {
            this.queue = queue;
        }

        //onPreExecute
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Removing");
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
        }

        //doInBackground
        @Override
        protected String doInBackground(String... params) {

            String url = params[0];

            String response = HttpHandler.makeServiceCall(url);
            return response;

        }


        //onPostExecute
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();

            if (s != null) {
                try {
                    JSONObject cancel = new JSONObject(s);
                    boolean status = cancel.getBoolean("status");
                    int statusCode = cancel.getInt("status_code");

                    if (status) {
                        if (statusCode == 200) {
                            Toast.makeText(MainActivity.this, "Queue canceld", Toast.LENGTH_SHORT).show();
                            sqLiteHelper.deleteRecord(queue);
                            Log.d(TAG, "Removed from  queue");
                            updateListView();
                        }
                    } else {
                        if (statusCode == 400) {
                            Toast.makeText(MainActivity.this, "Not Found Queue", Toast.LENGTH_SHORT).show();
                        } else if (statusCode == 500) {
                            Toast.makeText(MainActivity.this, "PDO Exception", Toast.LENGTH_SHORT).show();
                        } else if (statusCode == 540) {
                            Toast.makeText(MainActivity.this, "Incomplete Data", Toast.LENGTH_SHORT).show();
                        }
                    }

                } catch (JSONException e) {

                    e.printStackTrace();
                    Log.e(TAG, "Error in Cancel JSON" + e);

                }
            } else {
                Toast.makeText(MainActivity.this, "Null response", Toast.LENGTH_SHORT).show();
            }


        }
    }


    //async task to refresh the data.
    class RefreshTask extends AsyncTask<Object, Object, ArrayList<QueueModel>> {

        //doInBackground
        @Override
        protected ArrayList<QueueModel> doInBackground(Object... params) {

            // http://localhost/main/api/mobile/update.php?id=5&counter_id=1&store_id=1&email=sanu1@gmail.com&group_id=1
            int size = queues.size();
            String response = null;
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    refreshQueue = queues.get(i);
                    //String url = URL + "update.php?id=" + refreshQueue.getSanuid() + "&counter_id=" + refreshQueue.getCounter_no() + "&store_id=" + refreshQueue.getStore_id() + "&email=" + userInfo.getEmail() + "&group_id=" + refreshQueue.getStore_no();

                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("https")
                            .authority(IP)
                            .appendPath("main")
                            .appendPath("api")
                            .appendPath("mobile")
                            .appendPath("update.php")
                            .appendQueryParameter("id", refreshQueue.getSanuid())
                            .appendQueryParameter("counter_id", refreshQueue.getCounter_no())
                            .appendQueryParameter("store_id", refreshQueue.getStore_id())
                            .appendQueryParameter("email", userInfo.getEmail())
                            .appendQueryParameter("group_id", refreshQueue.getStore_no());

                    String url = builder.build().toString();

                    response = HttpHandler.makeServiceCall(url);


                    if (response.length() > 0) {
                        try {

                            JSONObject refResponse = new JSONObject(response);
                            Log.d(TAG, "refresh response " + refResponse);
                            boolean status = refResponse.getBoolean("status");
                            int statusCode = refResponse.getInt("status_code");

                            Log.d(TAG, "STATUS CODE " + statusCode);

                            if (status) {
                                if (statusCode == 200) {
                                    refreshQueue.setTime(refResponse.getString("time"));
                                    refreshQueue.setQueue_no(refResponse.getString("queue_no"));
                                    refreshQueue.setCounter_no(refResponse.getString("counter_id"));

                                    //COMPLETED update sqlite.
                                    sqLiteHelper.updateRecord(refreshQueue);
                                    showNotification(refreshQueue);
                                    ResponseRefreshList.add(refreshQueue);

                                } else if (statusCode == 300) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "You are already in queue.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } else if (statusCode == 500) {
                                Log.e(TAG, "PDO Exception");
                            } else if (statusCode == 400) {
                                sqLiteHelper.deleteRecord(refreshQueue);
                                Log.d(TAG, "NO queue found for given data");
                            } else if (statusCode == 430) {
                                Log.e(TAG, "Data dose not match with given format");
                            } else if (statusCode == 201) {
                                Log.d(TAG, "No Update on queue");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error in refresher Async task " + e);
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "No response", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }
            }
            return ResponseRefreshList;
        }

        @Override
        protected void onPostExecute(ArrayList<QueueModel> response) {
            super.onPostExecute(response);

            for (int i = 0; i < response.size(); i++) {
                sqLiteHelper.updateRecord(response.get(i));
            }

            updateListView();


        }
    }


    private void ParseJSON(String s) {

        //parsing JSON file and saving data

        Log.d(TAG, "RESPONSE " + s);

        if (s != null) {
            try {

                JSONObject response = new JSONObject(s);
                QueueModel queueModel = new QueueModel();


                boolean status = response.getBoolean("status");
                int statusCode = response.getInt("status_code");

                if (status) {
                    String id = response.getString("id");
                    String otp = response.getString("otp");
                    String qCode = response.getString("queue_code");

                    if (statusCode == 200) {
                        JSONObject queue = response.getJSONObject("queue");
                        queueModel.setQueue_no(queue.getString("queue_no"));
                        queueModel.setStore_id(queue.getString("store_id"));
                        queueModel.setCounter_no(queue.getString("counter_id"));
                        queueModel.setPby(queue.getString("queue_no"));

                        queueModel.setTime(queue.getString("time"));
                        queueModel.setSanuid(id); // in case of sanuId
                        queueModel.setStore_no(queue.getString("group_id"));


                        JSONObject company = response.getJSONObject("company");
                        queueModel.setShop_name(company.getString("comp_name"));
                        queueModel.setUrl(company.getString("image_url"));
                        queueModel.setShop_info(company.getString("comp_info"));
                        queueModel.setLat(company.getString("latitude"));
                        queueModel.setLog(company.getString("longitude"));

                        queueModel.setOtp(otp);
                        queueModel.setqCode(qCode);

                        queueModel.setCode(0);

                        //load the data to the queue
                        queues.add(queueModel);
                        sqLiteHelper.insertRecord(queueModel);

                        //update the listVIew of the Suggestion
                        updateListView();
                    } else if (statusCode == 300) {
                        Toast.makeText(MainActivity.this, "Posted in virtual Standing", Toast.LENGTH_SHORT).show();
                    }else if(statusCode == 310){
                        Toast.makeText(MainActivity.this, "QR code already scanned",Toast.LENGTH_SHORT).show();
                    }
                } else if (statusCode == 400) {
                    Toast.makeText(MainActivity.this, "No Counter Active for specific QR Code.", Toast.LENGTH_SHORT).show();
                } else if (statusCode == 402) {
                    Toast.makeText(MainActivity.this, "Wrong.", Toast.LENGTH_SHORT).show();
                } else if (statusCode == 500) {
                    Toast.makeText(MainActivity.this, "PDO Exception.", Toast.LENGTH_SHORT).show();
                }


            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "error is in onPostExecute " + " MainActiviyt" + e);
            }
        } else {
            Toast.makeText(MainActivity.this, "Null response", Toast.LENGTH_SHORT).show();
        }

    }

}
