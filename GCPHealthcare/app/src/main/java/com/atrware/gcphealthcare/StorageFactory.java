package com.atrware.gcphealthcare;

/**
 * Created by Tom on 4/19/2016.
 */

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

/**
 * This class manages the details of creating a Storage service, including auth.
 */
public class StorageFactory {

    private static Storage instance = null;

    public static synchronized Storage getService(GoogleCredential credential) throws IOException, GeneralSecurityException {
        if (instance == null) {
            instance = buildService(credential);
        }
        return instance;
    }

    private static Storage buildService(GoogleCredential credential) throws IOException, GeneralSecurityException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        if (credential.createScopedRequired()) {
            Collection<String> bigqueryScopes = StorageScopes.all();
            credential = credential.createScoped(bigqueryScopes);
        }

        return new Storage.Builder(transport, jsonFactory, credential)
                .setApplicationName("gcphealthcare")
                .build();
    }
}
