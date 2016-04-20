package com.atrware.gcphealthcare;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.accounts.AccountManager;
import android.accounts.Account;

import com.atrware.gcshealthcare.backend.getfilename.Getfilename;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
//import com.google.appengine.repackaged.com.google.api.client.http.HttpTransport;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import com.google.appengine.repackaged.com.google.common.base.Flag;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.api.services.storage.Storage;


import com.google.appengine.repackaged.com.google.protobuf.TextFormat;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int PICKFILE_RESULT_CODE = 1111;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3333;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private static final String LOG_TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;

    private BroadcastReceiver mMessageReceivedReceiver;
    private boolean isMessageReceiverRegistered;

    // for google credentials
    private AuthorizationCheckTask mAuthTask;
    private String mEmailAccount = "";
    private GoogleCredential credential = new GoogleCredential();
    private JsonFactory JSON_FACTORY = new JacksonFactory();
    private HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private String authorization = "";
    private String URI = "";
    private String mEmail = "";
    private String oauthToken = "";

    private Bitmap bmp = null;

    private String firebaseID = "https://sweltering-inferno-1859.firebaseio.com/resultsItems";

    // for GCS
    private String BUCKET_NAME = "gcphealthcare";
    private String SERVICE_ACCOUNT_EMAIL = "atrware@appspot.gserviceaccount.com";
    private String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";

    private long timeDifference = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get Google Account credentials
        //onClickSignIn(MainActivity.this.findViewById(id.email_address_tv));

        // Begin GCM code
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        Log.i(LOG_TAG, "GCM setting up the registration receiver.");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "GCM received token.");
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        Log.i(LOG_TAG, "GCM setting up the message receiver.");
        mMessageReceivedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(LOG_TAG, "GCM received message. File: " +
                        intent.getExtras().getString("filename"));
                // End timer
                timeDifference = System.currentTimeMillis() - timeDifference;
                // Add the information to Firebase
                new Firebase(firebaseID)
                        .push()
                        .child("text")
                        .setValue("GCM: " + timeDifference + "ms");
                new Firebase(firebaseID)
                        .push()
                        .child("text")
                        .setValue(intent.getExtras().getString("filename"));

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);

                // download the file
                downloadFile(intent.getExtras().getString("filename"));

            }
        };

        // Registering BroadcastReceiver
        registerReceiver();
        // Registering MessageReceiver
        registerMessageReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
            Log.i(LOG_TAG, "GCM play services available.");
        }
        // End GCM code

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        // Create a new Adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(this);

        Firebase ref = new Firebase(firebaseID);
        ref.authWithOAuthToken("google", oauthToken, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                // the Google user is now authenticated with your Firebase app
                Log.i(LOG_TAG, "OAuthenticated with: " + authData.getToken().toString());
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
                Log.e(LOG_TAG, "Error with OAuth: " + firebaseError.toString());
            }
        });

        ref.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.add((String) dataSnapshot.child("text").getValue());
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove((String) dataSnapshot.child("text").getValue());
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(FirebaseError firebaseError) {
            }
        });



        // Add items via the Button and EditText at the bottom of the window.
        final Button button = (Button) findViewById(R.id.addButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get permission to read file
                getPermissions();

                // Pick and upload a file to GCS
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICKFILE_RESULT_CODE);

            }
        });

        // Delete items when clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Show file
                String fileName = (String) listView.getItemAtPosition(position);
                String mimetype = URLConnection.guessContentTypeFromName(fileName);
                if(mimetype != null){
                    // Show file
                    Log.i(LOG_TAG, "Got mime type: " + mimetype);
                    File file = new File(
                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                                    + "/" + fileName);
                    Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(Uri.fromFile(file), mimetype);
                    target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(target);
                } else {
                    // remove object
                    new Firebase(firebaseID)
                            .orderByChild("text")
                            .equalTo((String) listView.getItemAtPosition(position))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        firstChild.getRef().removeValue();
                                    }
                                }

                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                }

            }
        });



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    // With the account name acquired, go get the auth token
                    //getUsername();
                    TextView emailAccountTextView = (TextView) this.findViewById(R.id.email_address_tv);
                    emailAccountTextView.setText(mEmail);
                    performAuthCheck(mEmail);
                } else if (resultCode == RESULT_CANCELED) {
                    // The account picker dialog closed without selecting an account.
                    // Notify users that they must pick an account to proceed.
                    Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
                }
                break;

            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String realPath = RealPathUtil.getPath(this, data.getData());

                    //String FilePath = data.getData().getPath();
                    Log.i(LOG_TAG, "Path is: " + realPath);
                    uploadFile(realPath);
                }
                break;
        }
    }

    public void getPermissions()
    {
        Log.i(LOG_TAG, "Getting permissions");
        requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    // Begin GCM code II
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        registerMessageReceiver();
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceivedReceiver);
        isMessageReceiverRegistered = false;
        super.onPause();
    }
    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    private void registerMessageReceiver() {
        if (!isMessageReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceivedReceiver,
                    new IntentFilter(QuickstartPreferences.MESSAGE_RECEIVED));
            isMessageReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    // End GCM code II

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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.atrware.gcphealthcare/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.atrware.gcphealthcare/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    public void downloadFile(final String fileName) {
        getCredential();
        URI = "https://www.googleapis.com" +
                "/storage/v1/b/" +
                BUCKET_NAME +
                "/o/" + fileName + "?alt=media";
        //"/o/" + fileName;



        new AsyncTask<Void, Void, GoogleCredential>() {

            @Override
            protected GoogleCredential doInBackground(Void... view) {
                // First run an async credential update.
                // refresh credentials
                try {
                    credential.refreshToken();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException: " + e.getMessage());
                }
                return credential;
            }

            @Override
            protected void onPostExecute(final GoogleCredential credential) {
                Log.i(LOG_TAG, "Download URI: " + URI);


                new AsyncTask<Void, Void, GoogleCredential>() {
                    @Override
                    protected GoogleCredential doInBackground(Void... view) {
                        // Begin timer
                        timeDifference = System.currentTimeMillis();

                        try {
                            Storage storage = StorageFactory.getService(credential);
                            Storage.Objects.Get getObject = storage.objects().get(BUCKET_NAME, fileName);
                            // Downloading data.
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            getObject.getMediaHttpDownloader().setDirectDownloadEnabled(true);
                            getObject.executeMediaAndDownloadTo(out);

                            byte[] b = out.toByteArray();

                            final byte[] delimiter = BaseEncoding.base16().lowerCase().decode("0d0a0d0a".toLowerCase());
                            List<byte[]> byteArrays = new LinkedList<>();
                            int begin = 0;
                            outer:
                            for (int i = 0; i < b.length - delimiter.length + 1; i++) {
                                for (int j = 0; j < delimiter.length; j++) {
                                    if (b[i + j] != delimiter[j]) {
                                        continue outer;
                                    }
                                }
                                byteArrays.add(Arrays.copyOfRange(b, begin, i));
                                begin = i + delimiter.length;
                                break;
                            }
                            byteArrays.add(Arrays.copyOfRange(b, begin, b.length));

                            // byteArrays.get(1) contains the file
                            // byteArrays.get(0) contains information about the file
                            //String fileMetadata = Arrays.toString(byteArrays.get(0));
                            String fileMetadata = new String(byteArrays.get(0));


                            String strCD = "";
                            BufferedReader reader = new BufferedReader(new StringReader(fileMetadata));
                            reader.readLine();
                            strCD = reader.readLine();

                            String[] cdParts = strCD.split("filename=\"");
                            String[] filenameParts = cdParts[1].split("\"");

                            // This won't appear in the logs! Bizarre!
                            //Log.i(LOG_TAG, "Got metadata: " + fileMetadata);

                            Log.i(LOG_TAG, "Here's the cd line: " + strCD);
                            Log.i(LOG_TAG, "Filename from header is " + filenameParts[0]);

                            OutputStream outputStream = new FileOutputStream(
                                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                                            + "/" + fileName);
                            outputStream.write(byteArrays.get(1));
                            outputStream.close();
                            Log.i(LOG_TAG, "Created: " +
                                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                                    + "/" + fileName);

                        } catch (GeneralSecurityException e) {
                            Log.e(LOG_TAG, "GCS Api fail: " + e);
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "GCS Api fail: " + e);
                        } catch (NullPointerException e) {
                            Log.e(LOG_TAG, "GCS Api fail: " + e);
                        }
                        return credential;
                    }
                    @Override
                    protected void onPostExecute(final GoogleCredential credential) {
                        Log.i(LOG_TAG, "GCS completed.");
                        // End timer
                        timeDifference = System.currentTimeMillis() - timeDifference;
                        // Add the information to Firebase
                        final EditText text = (EditText) findViewById(R.id.todoText);
                        new Firebase(firebaseID)
                                .push()
                                .child("text")
                                .setValue("DL: " + timeDifference + "ms");

                        /*
                        // Show file - this doesn't work when fired by broadcast receiver
                        String mimetype = URLConnection.guessContentTypeFromName(
                                fileName);
                        Log.i(LOG_TAG, "Got mime type: " + mimetype);

                        File file = new File(
                                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                                        + "/" + fileName);
                        Intent target = new Intent(Intent.ACTION_VIEW);
                        target.setDataAndType(Uri.fromFile(file), mimetype);
                        target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(target);
                        */

                    }
                }.execute();
            }
        }.execute();
    }


    public void uploadFile(final String strFilePath) {
        final File file = new File(strFilePath);
        getCredential();
        // Everything is set to post the file.
        URI = "https://www.googleapis.com" +
                "/upload/storage/v1/b/" +
                BUCKET_NAME +
                "/o?uploadType=media&" +
                "name=" + file.getName();

        new AsyncTask<Void, Void, GoogleCredential>() {

            @Override
            protected GoogleCredential doInBackground(Void... view) {
                // First run an async credential update.
                // refresh credentials
                try {
                    credential.refreshToken();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException: " + e.getMessage());
                }
                return credential;
            }

            @Override
            protected void onPostExecute(final GoogleCredential credential) {
                // Begin timer
                timeDifference = System.currentTimeMillis();
                // Then run async put.
                AsyncHttpClient client = new AsyncHttpClient();
                Log.i(LOG_TAG, "URL for upload is: " + URI);
                RequestParams params = new RequestParams();
                //params.put("Content-Type", "binary/octet-stream");
                //params.put("Content-Length", "" + file.length());
                client.addHeader("Content-Type", "binary/octet-stream");
                client.addHeader("Authorization", "Bearer " + credential.getAccessToken());
                try {
                    params.put(file.getName(), file);
                } catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, "File not found: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "File not found",
                            Toast.LENGTH_LONG).show();
                }

                Log.i(LOG_TAG, "Client: " + client);

                client.post(URI, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        // called before request is started
                        Log.i(LOG_TAG, "Request starting with credential: " + credential.getAccessToken());
                        // Generate the required information
                        // email address
                        // GCM InstanceID
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        // End timer
                        timeDifference = System.currentTimeMillis() - timeDifference;
                        // Add the information to Firebase
                        final EditText text = (EditText) findViewById(R.id.todoText);
                        new Firebase(firebaseID)
                                .push()
                                .child("text")
                                .setValue("UP: " + timeDifference + "ms");

                        String strResponse = new String(response);
                        Log.i(LOG_TAG, "Success: " + strResponse);
                        Toast.makeText(MainActivity.this, "File uploaded",
                                Toast.LENGTH_LONG).show();

                        try{
                            JSONObject jsonObj = new JSONObject(strResponse);
                            Log.i(LOG_TAG, jsonObj.getString("selfLink"));

                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "JSON problem");
                        }

                        // Send a request to Appengine to notify the recepient using GCM
                        // Start the timer first
                        timeDifference = System.currentTimeMillis();
                        // Use the Endpoint API
                        new sendGCMrequest().execute(new Pair<Context, String>(MainActivity.this, file.getName()));

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        String strErrorResponse = new String(errorResponse);
                        Log.e(LOG_TAG, "Status code: " + statusCode + " Headers: " + Arrays.toString(headers) +
                                " Error response: " + strErrorResponse + " Exception: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error uploading file",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        Log.i(LOG_TAG, "Retrying...");
                        Toast.makeText(MainActivity.this, "Retrying...",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.execute();
    }


    public class sendGCMrequest extends AsyncTask<Pair<Context, String>, Void, String> {
        private Getfilename myApiService = null;
        private Context context;

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            if(myApiService == null) {  // Only do this once
                Getfilename.Builder builder = new Getfilename.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        //.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        //    @Override
                        //    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        //        abstractGoogleClientRequest.setDisableGZipContent(true);
                        //    }
                        //});
                        .setRootUrl("https://atrware.appspot.com/_ah/api/");
                // end options for devappserver

                myApiService = builder.build();
            }

            context = params[0].first;
            String name = params[0].second;

            try {
                return myApiService.sayHi(name).execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "Sent get request to server. Response: " + result.toString());
        }
    }

    // This method is invoked when the "Sign In" button is clicked. See activity_main.xml for the
    // dynamic reference to this method.
    public void onClickSignIn(View view) {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
        /*
        TextView emailAddressTV = (TextView) view.getRootView().findViewById(R.id.email_address_tv);
        // Check to see how many Google accounts are registered with the device.
        int googleAccounts = AppConstants.countGoogleAccounts(this);
        if (googleAccounts == 0) {
            // No accounts registered, nothing to do.
            Toast.makeText(this, R.string.toast_no_google_accounts_registered,
                    Toast.LENGTH_LONG).show();
        } else if (googleAccounts == 1) {
            // If only one account then select it.
            Toast.makeText(this, R.string.toast_only_one_google_account_registered,
                    Toast.LENGTH_LONG).show();
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            if (accounts != null && accounts.length > 0) {
                // Select account and perform authorization check.
                emailAddressTV.setText(accounts[0].name);
                mEmailAccount = accounts[0].name;
                performAuthCheck(accounts[0].name);
            }
        } else {
            // More than one Google Account is present, a chooser is necessary.

            // Reset selected account.
            emailAddressTV.setText("");

            // Invoke an {@code Intent} to allow the user to select a Google account.
            Intent accountSelector = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false,
                    "Select the account to access Google Compute Engine API.", null, null, null);
            startActivityForResult(accountSelector,
                    ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION);
        }
        */
    }

    // Schedule the authorization check in an {@code Tasks}.
    public void performAuthCheck(String emailAccount) {
        // Cancel previously running tasks.
        if (mAuthTask != null) {
            try {
                mAuthTask.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // Start task to check authorization.
        mAuthTask = new AuthorizationCheckTask();
        mAuthTask.execute(emailAccount);
    }


    // Verifies OAuth2 token access for the application and Google account combination with
    // the {@code AccountManager} and the Play Services installed application. If the appropriate
    // OAuth2 access hasn't been granted (to this application) then the task may fire an
    // {@code Intent} to request that the user approve such access. If the appropriate access does
    // exist then the button that will let the user proceed to the next activity is enabled.
    class AuthorizationCheckTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... emailAccounts) {
            Log.i(LOG_TAG, "Background auth check task started.");

            if (!AppConstants.checkGooglePlayServicesAvailable(MainActivity.this)) {
                return false;
            }

            String emailAccount = emailAccounts[0];
            // Ensure only one task is running at a time.
            mAuthTask = this;

            // Ensure an email was selected.
            if (Strings.isNullOrEmpty(emailAccount)) {
                publishProgress(R.string.toast_no_google_account_selected);
                // Failure.
                return false;
            }

            Log.i(LOG_TAG, "Attempting to get AuthToken for account: " + emailAccount);


            try {
                // If the application has the appropriate access then a token will be retrieved, otherwise
                // an error will be thrown.
                /*
                GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                        MainActivity.this, AppConstants.AUDIENCE);
                credential.setSelectedAccountName(emailAccount);
                */

                String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

                oauthToken = GoogleAuthUtil.getToken(MainActivity.this, emailAccount, SCOPE);
                Log.i(LOG_TAG, "AccessToken retrieved: " + oauthToken);

                // Success.
                return true;
            } catch (UserRecoverableAuthException userRecoverableException) {
                handleException(userRecoverableException);
                return false;
            } catch (GoogleAuthException unrecoverableException) {
                Log.e(LOG_TAG, "Exception checking OAuth2 authentication.", unrecoverableException);
                publishProgress(R.string.toast_exception_checking_authorization);
                // Failure.
                return false;
            } catch (IOException ioException) {
                Log.e(LOG_TAG, "Exception checking OAuth2 authentication.", ioException);
                publishProgress(R.string.toast_exception_checking_authorization);
                // Failure or cancel request.
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... stringIds) {
            // Toast only the most recent.
            Integer stringId = stringIds[0];
            Toast.makeText(MainActivity.this, stringId, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            mAuthTask = this;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            TextView emailAddressTV = (TextView) MainActivity.this.findViewById(R.id.email_address_tv);
            if (success) {
                // Authorization check successful, set internal variable.
                mEmailAccount = emailAddressTV.getText().toString();
            } else {
                // Authorization check unsuccessful, reset TextView to empty.
                emailAddressTV.setText("");
            }
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    // This method is a hook for background threads and async tasks that need to
    // provide the user a response UI when an exception occurs.
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    /*
    private boolean isSignedIn() {
        if (!Strings.isNullOrEmpty(mEmailAccount)) {
            return true;
        } else {
            return false;
        }
    }
    */

    private File getTempPkc12File() throws IOException {
        // xxx.p12 export from google API console
        InputStream pkc12Stream = getResources().openRawResource(R.raw.atrware_db9aa67ad391);
        File tempPkc12File = File.createTempFile("temp_pkc12_file", "p12");
        OutputStream tempFileStream = new FileOutputStream(tempPkc12File);

        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = pkc12Stream.read(bytes)) != -1) {
            tempFileStream.write(bytes, 0, read);
        }
        return tempPkc12File;
    }

    private void getCredential()
    {
        Log.i(LOG_TAG, "checking if I can create a credential");
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            Log.i(LOG_TAG, "Cred-1");
            keystore.load(getResources().openRawResource(R.raw.atrware_db9aa67ad391),
                    "notasecret".toCharArray());
            PrivateKey key = (PrivateKey) keystore.getKey("privatekey", "notasecret".toCharArray());
            Log.i(LOG_TAG, "Cred0");
            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountPrivateKey(key)
                    .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
                    .setServiceAccountScopes(Collections.singleton(STORAGE_SCOPE))
                    // .setServiceAccountUser(SERVICE_ACCOUNT_EMAIL)
                    // .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                    .build();
        } catch (KeyStoreException e) {
            Log.e(LOG_TAG, "KeyStoreException: " + e.getMessage());
        } catch (CertificateException e) {
            Log.e(LOG_TAG, "CertificateException: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "NoSuchAlgorithmException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            Log.e(LOG_TAG, "UnrecoverableKeyException: " + e.getMessage());
        }

    }
}

