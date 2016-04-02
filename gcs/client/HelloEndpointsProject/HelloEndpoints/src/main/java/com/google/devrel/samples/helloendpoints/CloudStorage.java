package com.google.devrel.samples.helloendpoints;

/**
 * Created by Tom on 4/2/2016.
 */
import android.app.*;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;


/**
 * Simple wrapper around the Google Cloud Storage API
 */
public class CloudStorage {

    private static final String LOG_TAG = "MainActivity";

    private static Storage storage;

    private static final String PROJECT_ID_PROPERTY = "thomasmhardy";
    private static final String APPLICATION_NAME_PROPERTY = "thomasmhardy";
    private static final String ACCOUNT_ID_PROPERTY = "helloendpointsexample@thomasmhardy.iam.gserviceaccount.com";
    private static final String PRIVATE_KEY_PATH_PROPERTY = "thomasmhardy-ebc515c808a6.p12";

    /**
     * Uploads a file to a bucket. Filename and content type will be based on
     * the original file.
     *
     * @param bucketName Bucket where file will be uploaded
     * @param filePath   Absolute path of the file to upload
     * @throws Exception
     */
    public static void uploadFile(String bucketName, String filePath, Context context)
            throws Exception {
        Log.e(LOG_TAG, "wtf-up");

        Storage storage = getStorage(context);

        Log.e(LOG_TAG, "wtf-up2");

        StorageObject object = new StorageObject();
        object.setBucket(bucketName);

        File file = new File(filePath);
        Log.e(LOG_TAG, "wtf-up3: " + file.exists() + file.length() + filePath);

        InputStream stream = new FileInputStream(file);
        try {
            Log.e(LOG_TAG, "wtf-up4");
            String contentType = URLConnection
                    //.guessContentTypeFromStream(stream);
                    .guessContentTypeFromName(file.getName());
            if(contentType==null) contentType = "application/octet-stream";
            Log.e(LOG_TAG, "wtf-up5: " + contentType);
            InputStreamContent content = new InputStreamContent(contentType,
                    stream);
            content.setLength(file.length());
            Log.e(LOG_TAG, "wtf-up6: " + content.getLength() + bucketName);
            Storage.Objects.Insert insert = storage.objects().insert(
                    bucketName, null, content);
            Log.e(LOG_TAG, "wtf-up7: " + insert.getLastStatusMessage()+ insert.isEmpty());
            insert.setName(file.getName());
            Log.e(LOG_TAG, "wtf-up8: " + insert.getLastStatusMessage()+ insert.isEmpty());
            insert.execute();
            Log.e(LOG_TAG, "wtf-up9: " + insert.getLastStatusMessage()+ insert.isEmpty());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error gcs: " + e.getMessage());
        } finally {
            Log.e(LOG_TAG, "wtf-up10: ");
            stream.close();
            Log.e(LOG_TAG, "wtf-up11");
        }
        Log.e(LOG_TAG, "wtf-up12");
    }

    public static void downloadFile(String bucketName, String fileName, String destinationDirectory, Context context) throws Exception {

        File directory = new File(destinationDirectory);
        if (!directory.isDirectory()) {
            throw new Exception("Provided destinationDirectory path is not a directory");
        }
        File file = new File(directory.getAbsolutePath() + "/" + fileName);

        Storage storage = getStorage(context);

        Storage.Objects.Get get = storage.objects().get(bucketName, fileName);
        FileOutputStream stream = new FileOutputStream(file);
        try {
            get.executeAndDownloadTo(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Deletes a file within a bucket
     *
     * @param bucketName Name of bucket that contains the file
     * @param fileName   The file to delete
     * @throws Exception
     */
    public static void deleteFile(String bucketName, String fileName, Context context)
            throws Exception {

        Storage storage = getStorage(context);

        storage.objects().delete(bucketName, fileName).execute();
    }

    /**
     * Creates a bucket
     *
     * @param bucketName Name of bucket to create
     * @throws Exception
     */
    public static void createBucket(String bucketName, Context context) throws Exception {

        Storage storage = getStorage(context);

        Bucket bucket = new Bucket();
        bucket.setName(bucketName);

        storage.buckets().insert(PROJECT_ID_PROPERTY, bucket).execute();
    }

    /**
     * Deletes a bucket
     *
     * @param bucketName Name of bucket to delete
     * @throws Exception
     */
    public static void deleteBucket(String bucketName, Context context) throws Exception {

        Storage storage = getStorage(context);

        storage.buckets().delete(bucketName).execute();
    }

    /**
     * Lists the objects in a bucket
     *
     * @param bucketName bucket name to list
     * @return Array of object names
     * @throws Exception
     */
    public static List<String> listBucket(String bucketName, Context context) throws Exception {

        Storage storage = getStorage(context);

        List<String> list = new ArrayList<String>();

        List<StorageObject> objects = storage.objects().list(bucketName).execute().getItems();
        if (objects != null) {
            for (StorageObject o : objects) {
                list.add(o.getName());
            }
        }

        return list;
    }

    /**
     * List the buckets with the project
     * (Project is configured in properties)
     *
     * @return
     * @throws Exception
     */
    public static List<String> listBuckets(Context context) throws Exception {

        Storage storage = getStorage(context);

        List<String> list = new ArrayList<String>();

        List<Bucket> buckets = storage.buckets().list(PROJECT_ID_PROPERTY).execute().getItems();
        if (buckets != null) {
            for (Bucket b : buckets) {
                list.add(b.getName());
            }
        }

        return list;
    }

    private static Storage getStorage(Context context) throws Exception {
        Log.e(LOG_TAG, "wtf-store");
        if (storage == null) {

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List<String> scopes = new ArrayList<String>();
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

            Log.e(LOG_TAG, "wtf-store2");

            Credential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(ACCOUNT_ID_PROPERTY)
					/*
          .setServiceAccountPrivateKeyFromP12File(
							new File(getProperties().getProperty(
									PRIVATE_KEY_PATH_PROPERTY)))
          */
                    .setServiceAccountPrivateKeyFromP12File(getTempPkc12File(context))
                    //.setServiceAccountPrivateKeyFromP12File(
                    //        new File("/src/main/assets/thomasmhardy-ebc515c808a6.p12"))
                    .setServiceAccountScopes(scopes).build();

            Log.e(LOG_TAG, "wtf-store3");
            storage = new Storage.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName(APPLICATION_NAME_PROPERTY)
                    .build();
        }


        return storage;
    }

    private static File getTempPkc12File(Context context) throws Exception {
        // xxx.p12 export from google API console

        Log.e(LOG_TAG, "wtf-12");

        InputStream pkc12Stream = context.getAssets().open(PRIVATE_KEY_PATH_PROPERTY);
        File tempPkc12File = File.createTempFile("temp_pkc12_file", "p12");
        OutputStream tempFileStream = new FileOutputStream(tempPkc12File);


        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = pkc12Stream.read(bytes)) != -1) {
            tempFileStream.write(bytes, 0, read);
        }

        assert pkc12Stream != null;
        pkc12Stream.close();

        return tempPkc12File;

    }
}