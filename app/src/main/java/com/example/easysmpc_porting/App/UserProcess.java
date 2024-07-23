/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.easysmpc_porting.App;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.BinResult;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Study;
import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easybus.implementations.http.easybackend.BusEasyBackend;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easysmpc.resources.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * A user in an EasySMPC process
 *
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class UserProcess implements MessageListener {

    private ExecutorService executorService;

    /** Logger */
    /**
     * The study model
     */
    private Study model = new Study();
    /**
     * connection settings
     */
    private ConnectionSettings connectionSettings;
    /**
     * Error flag
     */
    private boolean stop = false;

    /** Self participant data */
    private Participant self;

    private Context context;
    /**
     * Creates a new instance
     *
     * @param connectionSettings
     */
    protected UserProcess(ConnectionSettings connectionSettings) {
        // Store
        this.connectionSettings = connectionSettings;
        this.context = MainActivity.getAppContext();
        toast("Started");
    }


    /**
     * Create a new instance from an existing model
     *
     * @param model
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     */
    public UserProcess(Study model, Context context) throws ClassNotFoundException, IllegalArgumentException, IOException {
        this(model.getConnectionSettings());
        this.context = MainActivity.getAppContext();
        // Store
        this.model = model;

        // TODO check if it's better the let Android handle the threads.
        // Spawns the common steps in an own thread
        //        new Thread(new Runnable() {
        //            public void run() {
        //                performCommonSteps();
        //            }
        //        }).start();
        performCommonSteps();
    }

    /**
     * Creates a key board listener thread to allow for stop processing
     */

    /**
     * Stops the process
     */
    protected void shutdown() {
        // Set stop flag
        this.stop = true;

        // Stop bus
        try {
            this.model.getBus().stop();
        } catch (BusException e1) {
            // Ignore
        }

        // Save latest state
        save();

        // Last log entry
    }


    /**
     * Gets the model
     *
     * @return the model
     */
    public Study getModel() {
        return model;
    }

    /**
     * Sets the model
     */
    protected void setModel(Study model) {
        this.model = model;
    }

    protected void setSelfData(Participant self){
        this.self = self;
    }

    /**
     * Is the process finished?
     *
     * @return
     */
    public boolean isProcessFinished() {
        return getModel().getState() == StudyState.FINISHED;
    }

    @Override
    public void receive(String message) {
        String messageStripped = message;

        // Check if valid
        if (isMessageShareResultValid(messageStripped)) {
            try {
                // Set message
                model.setShareFromMessage(Message.deserializeMessage(messageStripped));

                // Save
                save();
            } catch (IllegalStateException | IllegalArgumentException | NoSuchAlgorithmException |
                     ClassNotFoundException | IOException e) {
                Log.e("Unable to digest message", e.toString());
            }
        }
    }

    @Override
    public void receiveError(Exception e) {
        Log.e("Error receiveing messages", e.toString());
        Log.i("Receiveing will be retried", e.toString());
    }

    /**
     * Are shares complete to proceed?
     *
     * @return
     */
    private boolean areSharesComplete() {
        for (Bin b : this.model.getBins()) {
            if (!b.isComplete()) return false;
        }
        return true;
    }

    /**
     * Check whether message is valid
     *
     * @param text
     * @return
     */
    private boolean isMessageShareResultValid(String text) {
        // Check not null or empty
        if (model == null || text == null || text.trim().isEmpty()) {
            return false;
        }

        // Check message
        try {
            return model.isMessageShareResultValid(Message.deserializeMessage(text));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Starts receiving a message by means of messages bus
     *
     * @param roundIdentifier
     * @throws IllegalArgumentException
     * @throws BusException
     * @throws InterruptedException
     */
    private void receiveMessages(String roundIdentifier) throws IllegalArgumentException, BusException, InterruptedException {
        getModel().getBus(getModel().getConnectionSettings().getCheckInterval(), false).receive(new Scope(getModel().getName() + roundIdentifier),
                new Participant(getModel().getParticipantFromId(getModel().getOwnId()).name,
                        getModel().getParticipantFromId(getModel().getOwnId()).emailAddress),
                this);

        // Wait for all shares
        while (!areSharesComplete()) {

            // Check for error while receiving and throw exception
            if (this.stop) {
                throw new InterruptedException("Process stopped");
            }

            // Proceed if shares complete
            if (!getModel().isBusAlive()) {
                Log.e("Bus not available", "Bus is not alive anymore!");
            }

            // Wait
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e("Sleep of bus interrupted", e.toString());
            }
        }
    }

    /**
     * Sends a message by means of bus
     *
     * @param roundIdentifier
     * @throws InterruptedException
     */
    private void sendMessages(String roundIdentifier) throws InterruptedException {

        // Prepare
        FutureTask<Void> future = null;

        // Loop over participants
        for (int index = 0; index < getModel().getNumParticipants(); index++) {


            // Only proceed if not own user
            if (index != getModel().getOwnId()) {

                // Check for error while receiving and throw exception
                if (this.stop) {
                    throw new InterruptedException("Process stopped");
                }

                // Check if message has been sent already
                if (getModel().getUnsentMessageFor(index) == null) {
                    continue;
                }

                try {
                    // Retrieve bus and send message

                    future = getModel().getBus(getModel().getConnectionSettings().getCheckInterval(), false).send(Message.serializeMessage(getModel().getUnsentMessageFor(index)),
                            new Scope(getModel().getName() + (getModel().getState() == StudyState.INITIAL_SENDING ? Resources.ROUND_0 : roundIdentifier)),
                            new Participant(getModel().getParticipants()[index].name,
                                    getModel().getParticipants()[index].emailAddress));

                    // Wait for result with a timeout time
                    future.get(getModel().getConnectionSettings().getSendTimeout(), TimeUnit.MILLISECONDS);

                    // Mark message as sent
                    model.markMessageSent(index);

                    // Save
                    save();
                } catch (Exception e) {
                    future.cancel(true);
                    Log.e("Unable to send message", e.toString());
                    throw new IllegalStateException("Unable to send message!", e);
                }
            }
        }
    }

    // Method to show toast messages
    public void toast(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(this.context, text, Toast.LENGTH_LONG).show());
    }

    /**
     * Proceeds the SMPC steps which are the same for participating and creating user
     */
    protected void performCommonSteps() {
        Log.i("Processing started", "Common Steps started!");
        try {

            // Register keyboard listener to stop
            //registerKeyboardListenerThread();

            // Sends the messages for the first round and proceeds the model
            if ((model.getState() == StudyState.INITIAL_SENDING || model.getState() == StudyState.SENDING_SHARE) && !this.stop) {
                sendMessages(Resources.ROUND_1);
                this.model.toRecievingShares();
                Log.i("Round1end", String.format("1. round sending finished for study %s", getModel().getName()));
                toast(String.format("1. round sending finished for study %s", getModel().getName()));

            }

            // Receives the messages for the first round and proceeds the model
            if (model.getState() == StudyState.RECIEVING_SHARE && !this.stop) {
                Log.i("INFO", String.format("1. round receiving started for study %s", getModel().getName()));
                toast(String.format("1. round receiving started for study %s", getModel().getName()));
                receiveMessages(Resources.ROUND_1);
                this.model.toSendingResult();
                Log.i("INFO", String.format("1. round receiving finished for study %s", getModel().getName()));
                toast(String.format("1. round receiving finished for study %s", getModel().getName()));

            }

            // Sends the messages for the second round and proceeds the model
            if (getModel().getState() == StudyState.SENDING_RESULT && !this.stop) {
                Log.i("INFO", String.format("2. round sending started for study %s", getModel().getName()));
                toast(String.format("2. round sending started for study %s", getModel().getName()));

                sendMessages(Resources.ROUND_2);
                this.model.toRecievingResult();
                Log.i("INFO", String.format("2. round sending finished for study %s", getModel().getName()));
                toast(String.format("2. round sending finished for study %s", getModel().getName()));
            }


            // Receives the messages for the second round, stops the bus and finalizes the model
            if (getModel().getState() == StudyState.RECIEVING_RESULT && !this.stop) {
                Log.i("INFO", String.format("2. round receiving started for study %s", getModel().getName()));
                toast(String.format("2. round receiving started for study %s", getModel().getName()));
                receiveMessages(Resources.ROUND_2);
                getModel().stopBus();
                this.model.toFinished();
                Log.i("INFO", String.format("2. round receiving finished for study %s", getModel().getName()));
                toast(String.format("2. round receiving finished for study %s", getModel().getName()));

            }

            // Calculate & write result, delete file model
            if (getModel().getState() == StudyState.FINISHED) {
                Log.i("INFO", "Start calculating and writing result");
                toast("Start calculating results");
                exportResult();
            }

            // Log finished
            Log.i("INFO", String.format("Process completed sucessfully. Please see result file %s", createResultFileName()));
            toast("Process completed succefully.");


        } catch (IllegalStateException | IllegalArgumentException | IOException | BusException e) {
            // Log and shutdown
            Log.e("Unable to process common process steps", e.toString());
            shutdown();
        } catch (InterruptedException e) {
            // Log and shutdown
            Log.i("INFO", "Execution stopped");
            shutdown();
        }
    }

    /**
     * Export result to file
     */
    private void exportResult() {
        // Load data into list
        List<List<String>> list = new ArrayList<>();
        for (BinResult result : getModel().getAllResults()) {
            list.add(new ArrayList<String>(Arrays.asList(result.name, String.valueOf(result.value))));
            Log.i("Results",result.name + " " + String.valueOf(result.value));
        }

        // Export
        // Create a string builder for the text content
        StringBuilder data = new StringBuilder();
        for (List<String> item : list) {
            data.append(item.get(0)).append(" ").append(item.get(1)).append("\n");
        }

        // Save to a text file in internal storage
        String filename = "results.txt";
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(data.toString().getBytes());
        } catch (IOException e) {
            Log.e("SaveFile", "Error writing to file", e);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("ResultsPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("resultCount", list.size());

        for (int i = 0; i < list.size(); i++) {
            editor.putString("result_" + i + "_name", list.get(i).get(0));
            editor.putString("result_" + i + "_value", list.get(i).get(1));
        }

        editor.apply();
    }

    /**
     * Create the file name for the result file
     *
     * @return
     */
    private String createResultFileName() {
        return "result_" + getModel().getName() + "_" + DateTimeFormatter.ofPattern("yyyy.MM.dd HH.mm.ss").format(LocalDateTime.now()) + ".xlsx";
    }

    /**
     * Get connectionIMAPSettings
     *
     * @return
     */
    protected ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    /**
     * Tries to save the current state and logs in case of an error
     *
     * @return
     */
    protected void save() {

        // Ensure filename
        if (model.getFilename() == null) {
            model.setFilename(new File(getModel().getName() + "." + Resources.FILE_ENDING));
        }

        // Try saving
        try {
            this.model.saveProgram();
        } catch (IllegalStateException | IOException e) {
            Log.e("Unable to save interim state. Program execution is proceeded but state will be lost if the programm stops", e.toString());
        }
    }

    /**
     * Get an interim bus with 1000 milliseconds check interval
     *
     * @return
     */
    public Bus getInterimBus() {
        // Is e-mails bus?
        if (this.getConnectionSettings() instanceof ConnectionSettingsIMAP) {

            try {
                return new BusEmail(new ConnectionIMAP((ConnectionSettingsIMAP) this.getConnectionSettings(),
                        false), 1000);
            } catch (BusException e) {
                Log.e("Unable to get interim bus!", e.toString());
                throw new IllegalStateException("Unable to get interim bus!");
            }
        }

        // Is EasyBackend bus?
        try {
            // Set test values if this.self is null
            String name = (this.self != null && this.self.getName() != null) ? this.self.getName() : "Test Name";
            String email = (this.self != null && this.self.getEmailAddress() != null) ? this.self.getEmailAddress() : "test@example.com";

            if (this.getConnectionSettings() instanceof ConnectionSettingsEasyBackend) {
                return new BusEasyBackend(Resources.SIZE_THREADPOOL,
                        getConnectionSettings().getCheckInterval(),
                        ((ConnectionSettingsEasyBackend) getConnectionSettings()),
                        new Participant(name, email),
                        getConnectionSettings().getMaxMessageSize());
            }
        } catch (BusException e) {
            Log.e("Unable to get interim bus!", e.toString());
            throw new IllegalStateException("Unable to get interim bus!");
        }

        // Nothing found
        throw new IllegalStateException("Unable to determine bus type");
    }

    /**
     * Deletes all pre-existing messages which are related to the bus
     *
     * @param filter
     * @throws BusException
     * @throws InterruptedException
     */
    protected void purgeMessages(MessageFilter filter) throws BusException, InterruptedException {
        Bus bus = getInterimBus();
        bus.purge(filter);
        bus.stop();
    }
}