/* Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devrel.samples.helloendpoints;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;


import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.appspot.your_app_id.helloworld.Helloworld;
import com.appspot.your_app_id.helloworld.Helloworld.Greetings.Authed;
import com.appspot.your_app_id.helloworld.Helloworld.Greetings.GetGreeting;
import com.appspot.your_app_id.helloworld.Helloworld.Greetings.ListGreeting;
import com.appspot.your_app_id.helloworld.Helloworld.Greetings.Multiply;
import com.appspot.your_app_id.helloworld.model.HelloGreeting;
import com.appspot.your_app_id.helloworld.model.HelloGreetingCollection;
import com.google.common.base.Strings;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import java.io.File;


import com.google.devrel.samples.helloendpoints.R.id;

import static com.google.devrel.samples.helloendpoints.BuildConfig.DEBUG;

/**
 * Sample Android application for the Hello World tutorial for Google Cloud Endpoints. The sample
 * code shows many of the better practices described in the links below.
 *
 * @see <a href="https://developers.google.com/appengine/docs/java/endpoints">https://developers.google.com/appengine/docs/java/endpoints</a>
 * @see <a href="https://developers.google.com/appengine/docs/java/endpoints/consume_android">https://developers.google.com/appengine/docs/java/endpoints/consume_android</a>
 *
 */
public class MainActivity extends Activity {
  private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
  private static final String LOG_TAG = "MainActivity";

  /**
   * Activity result indicating a return from the Google account selection intent.
   */
  private static final int ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION = 2222;

  private AuthorizationCheckTask mAuthTask;
  private String mEmailAccount = "";
  private GreetingsDataAdapter listAdapter;

  // for google credentials
  private GoogleCredential credential = new GoogleCredential();
  private String BUCKET_NAME = "thomasmhardy.appspot.com";
  private String SERVICE_ACCOUNT_EMAIL = "helloendpointsexample@thomasmhardy.iam.gserviceaccount.com";
  private String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";
  private JsonFactory JSON_FACTORY = new JacksonFactory();
  private HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
  private String authorization = "";
  private String URI = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Prevent the keyboard from being visible upon startup.
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    /*
    ListView listView = (ListView)this.findViewById(R.id.greetings_list_view);
    listAdapter = new GreetingsDataAdapter((Application)this.getApplication());
    listView.setAdapter(listAdapter);
    */

    TextView topTitleTV = (TextView) MainActivity.this.findViewById(id.top_title_tv);
    if(Constants.type == Constants.Type.PATIENT) {
      topTitleTV.setText("Patient App");
    } else if (Constants.type == Constants.Type.PCP) {
      topTitleTV.setText("Provider App");
    } else if (Constants.type == Constants.Type.LAB) {
      topTitleTV.setText("Ancillary App");
    } else {
      topTitleTV.setText("Unknown App");
    }

    // Get Google Account credentials
    onClickSignIn(MainActivity.this.findViewById(id.email_address_tv));

    if (checkPlayServices()) {
      // Start IntentService to register this application with GCM.
      Intent intent = new Intent(this, RegistrationIntentService.class);
      startService(intent);
      Log.i(LOG_TAG, "Should have gotten a token.");
    } else {
      Log.i(LOG_TAG, "Play services not available.");
    }
  }

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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mAuthTask!=null) {
      mAuthTask.cancel(true);
      mAuthTask = null;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == ACTIVITY_RESULT_FROM_ACCOUNT_SELECTION && resultCode == RESULT_OK) {
      // This path indicates the account selection activity resulted in the user selecting a
      // Google account and clicking OK.

      // Set the selected account.
      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      TextView emailAccountTextView = (TextView)this.findViewById(id.email_address_tv);
      emailAccountTextView.setText(accountName);

      // Fire off the authorization check for this account and OAuth2 scopes.
      performAuthCheck("Logged in as: " + accountName);
    }
  }

  private boolean isSignedIn() {
    if (!Strings.isNullOrEmpty(mEmailAccount)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method is invoked when the "Get Greeting" button is clicked. See activity_main.xml for
   * the dynamic reference to this method.
   */
  /*
  public void onClickGetGreeting(View view) {
    View rootView = view.getRootView();
    TextView greetingIdInputTV = (TextView)rootView.findViewById(R.id.greeting_id_edit_text);
    if (greetingIdInputTV.getText()==null ||
            Strings.isNullOrEmpty(greetingIdInputTV.getText().toString())) {
      Toast.makeText(this, "Input a Greeting ID", Toast.LENGTH_SHORT).show();
      return;
    };

    String greetingIdString = greetingIdInputTV.getText().toString();
    int greetingId = Integer.parseInt(greetingIdString);

    // Use of an anonymous class is done for sample code simplicity. {@code AsyncTasks} should be
    // static-inner or top-level classes to prevent memory leak issues.
    // @see http://goo.gl/fN1fuE @26:00 for an great explanation.
    AsyncTask<Integer, Void, HelloGreeting> getAndDisplayGreeting =
            new AsyncTask<Integer, Void, HelloGreeting> () {
              @Override
              protected HelloGreeting doInBackground(Integer... integers) {
                // Retrieve service handle using null credential since this is an unauthenticated call.
                Helloworld apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                  GetGreeting getGreetingCommand = apiServiceHandle.greetings().getGreeting(integers[0]);
                  HelloGreeting greeting = getGreetingCommand.execute();
                  return greeting;
                } catch (IOException e) {
                  Log.e(LOG_TAG, "Exception during API call", e);
                }
                return null;
              }

              @Override
              protected void onPostExecute(HelloGreeting greeting) {
                if (greeting!=null) {
                  displayGreetings(greeting);
                } else {
                  Log.e(LOG_TAG, "No greetings were returned by the API.");
                }
              }
            };

    getAndDisplayGreeting.execute(greetingId);
  }
  */

  /**
   * This method is invoked when the "List Greetings" button is clicked. See activity_main.xml for
   * the dynamic reference to this method.
   */
  public void onClickListGreetings(View unused) {

    // Use of an anonymous class is done for sample code simplicity. {@code AsyncTasks} should be
    // static-inner or top-level classes to prevent memory leak issues.
    // @see http://goo.gl/fN1fuE @26:00 for an great explanation.
    AsyncTask<Void, Void, HelloGreetingCollection> getAndDisplayGreeting =
            new AsyncTask<Void, Void, HelloGreetingCollection> () {
              @Override
              protected HelloGreetingCollection doInBackground(Void... unused) {
                // Retrieve service handle using null credential since this is an unauthenticated call.
                Helloworld apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                  ListGreeting getGreetingCommand = apiServiceHandle.greetings().listGreeting();
                  HelloGreetingCollection greeting = getGreetingCommand.execute();
                  return greeting;
                } catch (IOException e) {
                  Log.e(LOG_TAG, "Exception during API call", e);
                }
                return null;
              }

              @Override
              protected void onPostExecute(HelloGreetingCollection greeting) {
                if (greeting!=null && greeting.getItems()!=null) {
                  displayGreetings(greeting.getItems().toArray(new HelloGreeting[] {}));
                } else {
                  Log.e(LOG_TAG, "No greetings were returned by the API.");
                }
              }
            };

    getAndDisplayGreeting.execute((Void) null);
  }

  public void onClickFileChooser(View view)
  {
    FileChooser filechooser = new FileChooser(MainActivity.this);
    filechooser.setFileListener(new FileChooser.FileSelectedListener() {
      @Override
      public void fileSelected(final File file) {
        // do something with the file
        /*
        HelloGreeting greeting = new HelloGreeting();
        greeting.setMessage(file.getAbsolutePath());
        displayGreetings(greeting);
        Log.i(LOG_TAG, "Finished list.");
        */

        // List files - not implemented yet

        /*
        // Get upload URL for blobstore option

        // Use of an anonymous class is done for sample code simplicity. {@code AsyncTasks} should be
        // static-inner or top-level classes to prevent memory leak issues.
        // @see http://goo.gl/fN1fuE @26:00 for an great explanation.
        AsyncTask<Integer, Void, HelloGreeting> getAndDisplayGreeting =
                new AsyncTask<Integer, Void, HelloGreeting> () {
                  @Override
                  protected HelloGreeting doInBackground(Integer... integers) {
                    // Retrieve service handle using null credential since this is an unauthenticated call.
                    Helloworld apiServiceHandle = AppConstants.getApiServiceHandle(null);

                    try {
                      GetGreeting getGreetingCommand = apiServiceHandle.greetings().getGreeting(integers[0]);
                      HelloGreeting greeting = getGreetingCommand.execute();
                      return greeting;
                    } catch (IOException e) {
                      Log.e(LOG_TAG, "Exception during API call", e);
                    }
                    return null;
                  }

                  @Override
                  protected void onPostExecute(HelloGreeting greeting) {
                    if (greeting!=null) {
                      displayGreetings(greeting);
                    } else {
                      Log.e(LOG_TAG, "No greetings were returned by the API.");
                    }
                  }
                };

        String strUploadURL = "";
        try {
          greeting = getAndDisplayGreeting.execute(99).get();
          strUploadURL = greeting.getMessage();
        } catch (Exception e) {
          Log.e(LOG_TAG, "Error waiting for async task to end: " + e.getMessage());
        }

        Log.i(LOG_TAG, "Got upload URL: " + strUploadURL);
        */


        Log.i(LOG_TAG, "checking if I can create a credential");
        try {
          KeyStore keystore = KeyStore.getInstance("PKCS12");
          Log.i(LOG_TAG, "Cred-1");
          keystore.load(getResources().openRawResource(R.raw.thomasmhardy_ebc515c808a6),
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

        // Everything is set to post the file.
        URI = "https://www.googleapis.com" +
                "/upload/storage/v1/b/" +
                BUCKET_NAME +
                "/o?uploadType=media&" +
                "name=" + file.getName();

        new AsyncTask<Void, Void, GoogleCredential>(){

          @Override
          protected GoogleCredential doInBackground(Void... view) {
            // First run an async credential update.
            Log.i(LOG_TAG, "Cred3");
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
            // Then run async put.
            AsyncHttpClient client = new AsyncHttpClient();
            Log.i(LOG_TAG, "URL is: " + URI);
            RequestParams params = new RequestParams();
            //params.put("Content-Type", "binary/octet-stream");
            //params.put("Content-Length", "" + file.length());
            client.addHeader("Content-Type", "binary/octet-stream");
            client.addHeader("Authorization", "Bearer " + credential.getAccessToken());
            try {
              params.put(file.getName(), file);
            } catch (FileNotFoundException e) {
              Log.e(LOG_TAG, "File not found: " + e.getMessage());
            }
            Log.i(LOG_TAG, "Client?: " + client);

            client.post(URI, params, new AsyncHttpResponseHandler() {
              @Override
              public void onStart() {
                // called before request is started
                Log.i(LOG_TAG, "Request starting with credential: " + credential.getAccessToken());
              }

              @Override
              public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                String strResponse = new String(response);
                Log.i(LOG_TAG, "Success: " + strResponse);
              }

              @Override
              public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                String strErrorResponse = new String(errorResponse);
                Log.e(LOG_TAG, "Status code: " + statusCode + " Headers: " + Arrays.toString(headers) +
                        " Error response: " + strErrorResponse + " Exception: " + e.getMessage());
              }

              @Override
              public void onRetry(int retryNo) {
                // called when request is retried
                Log.i(LOG_TAG, "Retrying...");
              }
            });
          }
        }.execute();
      };
    });
    filechooser.showDialog();
  }


  /**
   * This method is invoked when the "Multiply Greeting" button is clicked. See activity_main.xml
   * for the dynamic reference to this method.
   */

  /*
  public void onClickSendGreetings(View view) {
    View rootView = view.getRootView();

    TextView greetingCountInputTV = (TextView)rootView.findViewById(id.greeting_count_edit_text);
    if (greetingCountInputTV.getText()==null ||
            Strings.isNullOrEmpty(greetingCountInputTV.getText().toString())) {
      Toast.makeText(this, "Input a Greeting Count", Toast.LENGTH_SHORT).show();
      return;
    };

    String greetingCountString = greetingCountInputTV.getText().toString();
    final int greetingCount = Integer.parseInt(greetingCountString);

    TextView greetingTextInputTV = (TextView)rootView.findViewById(id.greeting_text_edit_text);
    if (greetingTextInputTV.getText()==null ||
            Strings.isNullOrEmpty(greetingTextInputTV.getText().toString())) {
      Toast.makeText(this, "Input a Greeting Message", Toast.LENGTH_SHORT).show();
      return;
    };

    final String greetingMessageString = greetingTextInputTV.getText().toString();

    // Use of an anonymous class is done for sample code simplicity. {@code AsyncTasks} should be
    // static-inner or top-level classes to prevent memory leak issues.
    // @see http://goo.gl/fN1fuE @26:00 for an great explanation.
    AsyncTask<Void, Void, HelloGreeting> sendGreetings = new AsyncTask<Void, Void, HelloGreeting> () {
      @Override
      protected HelloGreeting doInBackground(Void... unused) {
        // Retrieve service handle using null credential since this is an unauthenticated call.
        Helloworld apiServiceHandle = AppConstants.getApiServiceHandle(null);

        try {
          HelloGreeting greeting = new HelloGreeting();
          greeting.setMessage(greetingMessageString);

          Multiply multiplyGreetingCommand = apiServiceHandle.greetings().multiply(greetingCount,
                  greeting);
          greeting = multiplyGreetingCommand.execute();
          return greeting;
        } catch (IOException e) {
          Log.e(LOG_TAG, "Exception during API call", e);
        }
        return null;
      }

      @Override
      protected void onPostExecute(HelloGreeting greeting) {
        if (greeting!=null) {
          displayGreetings(greeting);
        } else {
          Log.e(LOG_TAG, "No greetings were returned by the API.");
        }
      }
    };

    sendGreetings.execute((Void)null);
  }
  */

  /**
   * This method is invoked when the "Get Authenticated Greeting" button is clicked. See
   * activity_main.xml for the dynamic reference to this method.
   */
  public void onClickGetAuthenticatedGreeting(View unused) {
    if (!isSignedIn()) {
      Toast.makeText(this, "You must sign in for this action.", Toast.LENGTH_LONG).show();
      return;
    }

    // Use of an anonymous class is done for sample code simplicity. {@code AsyncTasks} should be
    // static-inner or top-level classes to prevent memory leak issues.
    // @see http://goo.gl/fN1fuE @26:00 for an great explanation.
    AsyncTask<Void, Void, HelloGreeting> getAuthedGreetingAndDisplay =
            new AsyncTask<Void, Void, HelloGreeting> () {
              @Override
              protected HelloGreeting doInBackground(Void... unused) {
                if (!isSignedIn()) {
                  return null;
                };

                if (!AppConstants.checkGooglePlayServicesAvailable(MainActivity.this)) {
                  return null;
                }

                // Create a Google credential since this is an authenticated request to the API.
                GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                        MainActivity.this, AppConstants.AUDIENCE);
                credential.setSelectedAccountName(mEmailAccount);

                // Retrieve service handle using credential since this is an authenticated call.
                Helloworld apiServiceHandle = AppConstants.getApiServiceHandle(credential);

                try {
                  Authed getAuthedGreetingCommand = apiServiceHandle.greetings().authed();
                  HelloGreeting greeting = getAuthedGreetingCommand.execute();
                  return greeting;
                } catch (IOException e) {
                  Log.e(LOG_TAG, "Exception during API call", e);
                }
                return null;
              }

              @Override
              protected void onPostExecute(HelloGreeting greeting) {
                if (greeting!=null) {
                  displayGreetings(greeting);
                } else {
                  Log.e(LOG_TAG, "No greetings were returned by the API.");
                }
              }
            };

    getAuthedGreetingAndDisplay.execute((Void)null);
  }

  private void displayGreetings(HelloGreeting... greetings) {
    String msg;
    if (greetings==null || greetings.length < 1) {
      msg = "Greeting was not present";
      Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    } else {
      if (DEBUG) {
        Log.d(LOG_TAG, "Displaying " + greetings.length + " greetings.");
      }

      List<HelloGreeting> greetingsList = Arrays.asList(greetings);
      listAdapter.replaceData(greetings);
    }
  }

  /**
   * This method is invoked when the "Sign In" button is clicked. See activity_main.xml for the
   * dynamic reference to this method.
   */
  public void onClickSignIn(View view) {
    TextView emailAddressTV = (TextView) view.getRootView().findViewById(id.email_address_tv);
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
        emailAddressTV.setText("Logged in as: " + accounts[0].name);
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

  }

  /**
   * Schedule the authorization check in an {@code Tasks}.
   */
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

  /**
   * Verifies OAuth2 token access for the application and Google account combination with
   * the {@code AccountManager} and the Play Services installed application. If the appropriate
   * OAuth2 access hasn't been granted (to this application) then the task may fire an
   * {@code Intent} to request that the user approve such access. If the appropriate access does
   * exist then the button that will let the user proceed to the next activity is enabled.
   */
  class AuthorizationCheckTask extends AsyncTask<String, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(String... emailAccounts) {
      Log.i(LOG_TAG, "Background task started.");

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

      if (DEBUG) {
        Log.d(LOG_TAG, "Attempting to get AuthToken for account: " + mEmailAccount);
      }

      try {
        // If the application has the appropriate access then a token will be retrieved, otherwise
        // an error will be thrown.
        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                MainActivity.this, AppConstants.AUDIENCE);
        credential.setSelectedAccountName(emailAccount);

        String accessToken = credential.getToken();

        if (DEBUG) {
          Log.d(LOG_TAG, "AccessToken retrieved");
        }

        // Success.
        return true;
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
      TextView emailAddressTV = (TextView) MainActivity.this.findViewById(id.email_address_tv);
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

  /**
   * Simple use of an ArrayAdapter but we're using a static class to ensure no references to the
   * Activity exists.
   */
  static class GreetingsDataAdapter extends ArrayAdapter {
    GreetingsDataAdapter(Application application) {
      super(application.getApplicationContext(), android.R.layout.simple_list_item_1,
              application.greetings);
    }

    void replaceData(HelloGreeting[] greetings) {
      clear();
      for (HelloGreeting greeting : greetings) {
        add(greeting);
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView view = (TextView)super.getView(position, convertView, parent);

      HelloGreeting greeting = (HelloGreeting)this.getItem(position);

      StringBuilder sb = new StringBuilder();

      Set<String> fields = greeting.keySet();
      boolean firstLoop = true;
      for (String fieldName : fields) {
        // Append next line chars to 2.. loop runs.
        if (firstLoop) {
          firstLoop = false;
        } else {
          sb.append("\n");
        }

        sb.append(fieldName)
                .append(": ")
                .append(greeting.get(fieldName));
      }

      view.setText(sb.toString());
      return view;
    }
  }
}