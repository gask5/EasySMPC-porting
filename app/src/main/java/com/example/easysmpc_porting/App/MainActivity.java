package com.example.easysmpc_porting.App;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.easysmpc_porting.R;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easysmpc.dataimport.ImportFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tu_darmstadt.cbs.emailsmpc.Study;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "EasySMPC";
    @SuppressLint("StaticFieldLeak")
    private static MainActivity instance;
    @SuppressLint("StaticFieldLeak")
    private HashMap<String, String> dataMap = new HashMap<>();
    private ArrayAdapter<String> adapter;
    private List<String> listItems = new ArrayList<>();
    private String selectedKey = null;


    private static final String PREF_NAME = "ResultsPref";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);

        // Load saved user inputs
        loadUserInputs();

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        EditText keyInput = findViewById(R.id.keyInput);
        EditText valueInput = findViewById(R.id.valueInput);
        Button addButton = findViewById(R.id.addButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        ListView listView = findViewById(R.id.hashmapListView);
        Button participateButton = findViewById(R.id.participateButton);
        Button createSessionButton = findViewById(R.id.createSessionButton);

        EditText serverUrlInput = findViewById(R.id.serverUrlInput);

        EditText studyNameInput = findViewById(R.id.studyNameInput);
        EditText participantNameInput = findViewById(R.id.participantNameInput);
        EditText emailReceivingInput = findViewById(R.id.emailReceivingInput);
        EditText passwordReceivingInput = findViewById(R.id.passwordReceivingInput);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            String key = keyInput.getText().toString();
            String value = valueInput.getText().toString();
            if (!key.isEmpty() && !value.isEmpty()) {
                if (selectedKey != null && selectedKey.equals(key)) {
                    dataMap.put(key, value);  // Edit existing entry
                    selectedKey = null;  // Reset selected key
                } else {
                    dataMap.put(key, value);  // Add new entry
                }
                keyInput.setText("");
                valueInput.setText("");
                updateListView();
                Toast.makeText(MainActivity.this, "Added/Edited: " + key + " -> " + value, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Key and Value cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (selectedKey != null) {
                dataMap.remove(selectedKey);
                selectedKey = null;
                updateListView();
                Toast.makeText(MainActivity.this, "Deleted: " + selectedKey, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "No item selected", Toast.LENGTH_SHORT).show();
            }
        });


        participateButton.setOnClickListener(v -> {
            // Get inputs from fields
            String serverUrl = serverUrlInput.getText().toString();
            String studyName = studyNameInput.getText().toString();
            String participantName = participantNameInput.getText().toString();
            String emailReceiving = emailReceivingInput.getText().toString();
            String passwordReceiving = passwordReceivingInput.getText().toString();

            // Validate inputs
            if (serverUrl.isEmpty() || studyName.isEmpty() || participantName.isEmpty() ||
                    emailReceiving.isEmpty() || passwordReceiving.isEmpty()) {
                Toast.makeText(MainActivity.this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dataMap.isEmpty()) {
                Toast.makeText(MainActivity.this, "At least one entry must be added to the data map", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable interactive elements
            disableInteractiveElements();

            // Save user inputs
            saveUserInputs();

            // Start EasySMPC
            startEasySMPC(serverUrl, "participate", studyName, participantName, dataMap,
                    emailReceiving, passwordReceiving);
        });

        // TODO
        createSessionButton.setOnClickListener(v -> {
            String serverUrl = serverUrlInput.getText().toString();
            String studyName = studyNameInput.getText().toString();
            String participantName = participantNameInput.getText().toString();
            String emailReceiving = emailReceivingInput.getText().toString();
            String passwordReceiving = passwordReceivingInput.getText().toString();

            startEasySMPC("EASYBACKEND", "participate", studyName, participantName, dataMap,
                    emailReceiving, passwordReceiving);
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            String[] parts = item.split(": ");
            if (parts.length == 2) {
                keyInput.setText(parts[0]);
                valueInput.setText(parts[1]);
                selectedKey = parts[0];
            }
        });
    }

    private void disableInteractiveElements() {
        EditText keyInput = findViewById(R.id.keyInput);
        EditText valueInput = findViewById(R.id.valueInput);
        Button addButton = findViewById(R.id.addButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button participateButton = findViewById(R.id.participateButton);
        Button createSessionButton = findViewById(R.id.createSessionButton);
        ListView listView = findViewById(R.id.hashmapListView);
        EditText serverUrlInput = findViewById(R.id.serverUrlInput);
        EditText studyNameInput = findViewById(R.id.studyNameInput);
        EditText participantNameInput = findViewById(R.id.participantNameInput);
        EditText emailReceivingInput = findViewById(R.id.emailReceivingInput);
        EditText passwordReceivingInput = findViewById(R.id.passwordReceivingInput);

        keyInput.setEnabled(false);
        valueInput.setEnabled(false);
        addButton.setEnabled(false);
        deleteButton.setEnabled(false);
        participateButton.setEnabled(false);
        createSessionButton.setEnabled(false);
        listView.setEnabled(false);
        serverUrlInput.setEnabled(false);
        studyNameInput.setEnabled(false);
        participantNameInput.setEnabled(false);
        emailReceivingInput.setEnabled(false);
        passwordReceivingInput.setEnabled(false);
    }

    private void updateListView() {
        listItems.clear();
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            listItems.add(entry.getKey() + ": " + entry.getValue());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Clear existing data
        dataMap.clear();
        listItems.clear();

        // Retrieve the updated results from SharedPreferences
        int count = sharedPreferences.getInt("resultCount", 0);

        // Load the updated results into dataMap and listItems
        for (int i = 0; i < count; i++) {
            String name = sharedPreferences.getString("result_" + i + "_name", null);
            String value = sharedPreferences.getString("result_" + i + "_value", null);
            if (name != null && value != null) {
                dataMap.put(name, value);
                listItems.add(name + ": " + value);
            }
        }

        // Update the ListView
        adapter.notifyDataSetChanged();


        // Log the change for debugging
        Log.d(TAG, "SharedPreferences key " + key + " changed, updated results loaded.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    // Method to save user inputs
    private void saveUserInputs() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserInputs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save inputs
        editor.putString("serverUrl", ((EditText) findViewById(R.id.serverUrlInput)).getText().toString());
        editor.putString("studyName", ((EditText) findViewById(R.id.studyNameInput)).getText().toString());
        editor.putString("participantName", ((EditText) findViewById(R.id.participantNameInput)).getText().toString());
        editor.putString("emailReceiving", ((EditText) findViewById(R.id.emailReceivingInput)).getText().toString());
        editor.putString("passwordReceiving", ((EditText) findViewById(R.id.passwordReceivingInput)).getText().toString());
        // Apply changes
        editor.apply();
    }

    private void loadUserInputs() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserInputs", MODE_PRIVATE);

        // Load inputs
        String serverUrl = sharedPreferences.getString("serverUrl", "");
        String studyName = sharedPreferences.getString("studyName", "");
        String participantName = sharedPreferences.getString("participantName", "");
        String emailReceiving = sharedPreferences.getString("emailReceiving", "");
        String passwordReceiving = sharedPreferences.getString("passwordReceiving", "");

        // Set inputs to EditTexts
        ((EditText) findViewById(R.id.serverUrlInput)).setText(serverUrl);
        ((EditText) findViewById(R.id.studyNameInput)).setText(studyName);
        ((EditText) findViewById(R.id.participantNameInput)).setText(participantName);
        ((EditText) findViewById(R.id.emailReceivingInput)).setText(emailReceiving);
        ((EditText) findViewById(R.id.passwordReceivingInput)).setText(passwordReceiving);
    }

    public static Context getAppContext() {
        return instance.getBaseContext();
    }

    private void startEasySMPC(String serverUrl,
                               String action,
                               String studyName,
                               String participantName,
                               HashMap<String, String> dataInput,
                               String emailReceiving,
                               String passwordReceiving) {
        // Prefer IPv6
        System.getProperties().setProperty("java.net.preferIPv6Addresses", "true");
        // Set headless mode (see https://poi.apache.org/components/spreadsheet/quick-guide.html#Autofit)
        System.getProperties().setProperty("java.awt.headless", "true");

        try {
            // Check action
            if (!("create".equals(action) || "participate".equals(action) || "resume".equals(action))) {
                throw new IllegalArgumentException("Action must be one of: create, participate, resume");
            }

            // Perform action
            if ("participate".equals(action)) {
                Toast.makeText(getApplicationContext(), "Participating", Toast.LENGTH_SHORT).show();
                proceedParticipate(serverUrl, studyName, participantName, dataInput, false, false, 0, emailReceiving);
            } else if ("resume".equals(action)) {
                String resumeFile = null;
                String passwordSending = null;
                proceedResume(resumeFile, passwordReceiving, passwordSending);
            } else {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "An error occurred: Unknown action");
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "An error occurred: " + e.getMessage(), e);
        }
    }

    private void proceedResume(String resumeFile, String passwordReceiving, String passwordSending) {
    }

    /**
     * Proceed processing in participating mode.
     *
     * @param studyName       Name of the study
     * @param participantName Name of the participant
     * @param dataInput       Data variables
     * @param dataColumn      Indicates column-oriented data
     * @param hasHeader       Indicates if data has a header to skip
     * @param skipColumns     Number of columns to skip
     * @param emailReceiving  Email address for receiving
     */
    private void proceedParticipate(String serverUrl,
                                    String studyName,
                                    String participantName,
                                    HashMap<String, String> dataInput,
                                    boolean dataColumn,
                                    boolean hasHeader,
                                    int skipColumns,
                                    String emailReceiving
    ) {
        try {
            String password = "test";
            String authServerUrl = null; // Optional, set to null if not used
            String authRealm = "easybackend"; // Optional, set to null if not used
            String authClientId = "easy-client"; // Optional, set to null if not used
            String authClientSecret = null; // Optional, set to null if not used
            String proxyUrl = null; // Optional, set to null if not used
            int mailboxCheckInterval = 60; // in seconds, adjust as needed
            int sendTimeout = 30; // in seconds, adjust as needed
            int maxMessageSize = 10; // in MB, adjust as needed
            // Prepare connection settings parser based on connection type
            ConnectionSettingsParser connectionSettingsParser;
            connectionSettingsParser = new ConnectionSettingsParserEasyBackend(
                    serverUrl,
                    password,
                    authServerUrl,
                    authRealm,
                    authClientId,
                    authClientSecret,
                    proxyUrl,
                    mailboxCheckInterval,
                    sendTimeout,
                    maxMessageSize
            );

            Toast.makeText(getApplicationContext(), "Proceeding", Toast.LENGTH_SHORT).show();

            // Create participating user
            UserProcessParticipating participatingUser = new UserProcessParticipating(studyName,
                    new Participant(participantName, emailReceiving),
                    dataInput,
                    connectionSettingsParser.getConnectionSettings(emailReceiving));

            // Wait for participant to be initialized
            while (participatingUser.getModel() == null || participatingUser.getModel().getState() == Study.StudyState.NONE) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "An error occurred: " + e.getMessage(), e);
        }
    }

    public void loadResults() {
        // Clear the existing data
        dataMap.clear();
        listItems.clear();

        // Retrieve the results from SharedPreferences
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences("ResultsPref", Context.MODE_PRIVATE);
        int count = sharedPreferences.getInt("resultCount", 0);

        // Load results into dataMap and listItems
        for (int i = 0; i < count; i++) {
            String name = sharedPreferences.getString("result_" + i + "_name", null);
            String value = sharedPreferences.getString("result_" + i + "_value", null);
            if (name != null && value != null) {
                dataMap.put(name, value);
                listItems.add(name + ": " + value);
            }
        }

        // Notify adapter about data changes
        adapter.notifyDataSetChanged();

        // Optional: Provide feedback to the user
        Toast.makeText(instance, "Results loaded", Toast.LENGTH_SHORT).show();
    }


}
