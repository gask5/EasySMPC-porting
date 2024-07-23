package org.bihealth.mi.easybus.implementations.http.easybackend;

import android.content.Context;
import android.util.Log;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.BusMessageFragment;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.MessageManager;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PerformanceListener;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.http.HTTPAuthentication;
import org.bihealth.mi.easybus.implementations.http.HTTPException;
import org.bihealth.mi.easybus.implementations.http.HTTPRequest;
import org.bihealth.mi.easybus.implementations.http.HTTPRequest.HTTPRequestType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Bus implementation with easybackend
 */
public class BusEasyBackend extends Bus {

    private static final String TAG = "BusEasyBackend";
    private static final String PATH_SEND_MESSAGE_PATTERN = "api/easybackend/send/%s/%s";
    private static final String PATH_GET_MESSAGES_PATTERN = "api/easybackend/receive/%s";
    private static final String PATH_DELETE_MESSAGE_PATTERN = "api/easybackend/message/%s";
    private static final String PATH_PURGE_PATTERN = "api/easybackend/message";

    private final HTTPAuthentication auth;
    private final URI server;
    private final Participant self;
    private final ExecutorService executorService;
    private final MessageManager messageManager;
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final long sleepMillis;
    private boolean stop = false;
    private String token = null;
    private PerformanceListener listener = null;
    private Context context;

    public BusEasyBackend(int sizeThreadpool, long millis, ConnectionSettingsEasyBackend settings, Participant self, int maxMessageSize) {
        super(sizeThreadpool);
        this.auth = new HTTPAuthentication(settings);
        this.self = self;
        this.listener = settings.getListener();
        this.sleepMillis = millis;
        try {
            this.server = settings.getAPIServer().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("API server URI is incorrect");
        }
        this.messageManager = new MessageManager(maxMessageSize);
        this.executorService = Executors.newFixedThreadPool(sizeThreadpool);

        startReceivingThread();
    }

    private void startReceivingThread() {
        executorService.submit(() -> {
            while (!stop) {
                try {
                    receive();
                } catch (BusException | InterruptedException e) {
                    Log.e(TAG, "Error receiving messages", e);
                }
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public boolean isAlive() {
        return !executorService.isShutdown();
    }

    @Override
    public void stop() {
        stop = true;
        executorService.shutdownNow();
    }

    @Override
    protected Void sendInternal(BusMessage message) throws Exception {
        int size = 0;
        for (BusMessage m : messageManager.splitMessage(message)) {
            size += send(message.getReceiver(), message.getScope(), m);
        }
        if (listener != null) {
            listener.messageSent(size);
        }
        return null;
    }

    private int send(Participant receiver, Scope scope, Object message) throws BusException {
        String body;
        try {
            body = serializeObject(message);
        } catch (IOException e) {
            throw new BusException("Unable to serialize message", e);
        }
        int size = body.getBytes(StandardCharsets.UTF_8).length;

        Exception exception = null;
        try {
            new HTTPRequest(server, String.format(PATH_SEND_MESSAGE_PATTERN, scope.getName(), receiver.getEmailAddress()), HTTPRequestType.POST, getToken(), body).execute();
        } catch (HTTPException e) {
            if (e.getStatusCode() == 401) {
                renewToken();
                try {
                    new HTTPRequest(server, String.format(PATH_SEND_MESSAGE_PATTERN, scope.getName(), receiver.getEmailAddress()), HTTPRequestType.POST, getToken(), body).execute();
                    exception = null;
                } catch (Exception e1) {
                    exception = e1;
                }
            } else {
                exception = e;
            }
        }
        if (exception != null) {
            throw new BusException("Error while executing HTTP request to send message!", exception);
        }
        return size;
    }

    @Override
    public void purge(MessageFilter filter) throws BusException, InterruptedException {
        Exception exception = null;
        try {
            new HTTPRequest(server, PATH_PURGE_PATTERN, HTTPRequestType.DELETE, getToken(), null).execute();
        } catch (HTTPException e) {
            if (e.getStatusCode() == 401) {
                renewToken();
                try {
                    new HTTPRequest(server, PATH_PURGE_PATTERN, HTTPRequestType.DELETE, getToken(), null).execute();
                    exception = null;
                } catch (Exception e1) {
                    exception = e1;
                }
            } else {
                exception = e;
            }
        }
        if (exception != null) {
            throw new BusException("Error purging messages!", exception);
        }
    }

    protected void receive() throws BusException, InterruptedException {
        Log.d(TAG, "Started receiving");

        for (String scope : getScopesForParticipant(self)) {
            String resultString = null;
            Iterator<JsonNode> messages;
            Exception exception = null;

            try {
                resultString = new HTTPRequest(server, String.format(PATH_GET_MESSAGES_PATTERN, scope), HTTPRequestType.GET, getToken(), null).execute();
            } catch (HTTPException e) {
                if (e.getStatusCode() == 401) {
                    renewToken();
                    try {
                        resultString = new HTTPRequest(server, String.format(PATH_GET_MESSAGES_PATTERN, scope), HTTPRequestType.GET, getToken(), null).execute();
                        exception = null;
                    } catch (Exception e1) {
                        exception = e1;
                    }
                }
                exception = e;
            }

            if (exception != null) {
                Log.e(TAG, "Unable to get messages for " + self.getEmailAddress(), exception);
                continue;
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            try {
                messages = mapper.reader().readTree(resultString).elements();
            } catch (JsonProcessingException e) {
                Log.e(TAG, "Error deserializing sync string!", e);
                continue;
            }

            while (messages.hasNext()) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                JsonNode messagesNode = messages.next();
                final BusMessage message;
                try {
                    message = recreateMessage(messagesNode);
                } catch (BusException e) {
                    Log.e(TAG, "Unable to recreate message!", e);
                    continue;
                }

                BusMessage messageComplete = messageManager.mergeMessage(message);
                if (messageComplete != null) {
                    receiveInternal(messageComplete);
                    if (listener != null) {
                        listener.messageReceived(0);  // TODO: Determine and use correct size of received message
                    }
                }
            }
        }
    }

    private BusMessage recreateMessage(JsonNode messagesNode) throws BusException {
        if (messagesNode.path("id") == null || messagesNode.path("id").isMissingNode() || messagesNode.path("content") == null || messagesNode.path("content").isMissingNode()) {
            throw new BusException("Node contains insufficient data fields!");
        }

        final BigInteger id = messagesNode.path("id").bigIntegerValue();
        BusMessageFragment o;
        try {
            o = (BusMessageFragment) deserializeMessage(messagesNode.path("content").textValue());
        } catch (ClassNotFoundException | IOException e) {
            throw new BusException("Unable to deserialize message", e);
        }

        if (o instanceof BusMessageFragment) {
            return new BusMessageFragment((BusMessageFragment) o) {
                private static final long serialVersionUID = -2294147052362533378L;

                @Override
                public void delete() throws BusException {
                    deleteMessage(id);
                }

                @Override
                public void expunge() throws BusException {
                }
            };
        } else {
            return new BusMessage((BusMessage) o) {
                private static final long serialVersionUID = -2294147098332533758L;

                @Override
                public void delete() throws BusException {
                    deleteMessage(id);
                }

                @Override
                public void expunge() throws BusException {
                }
            };
        }
    }

    private void deleteMessage(BigInteger id) throws BusException {
        Exception exception = null;
        try {
            new HTTPRequest(server, String.format(PATH_DELETE_MESSAGE_PATTERN, id), HTTPRequestType.DELETE, getToken(), null).execute();
        } catch (HTTPException e) {
            if (e.getStatusCode() == 401) {
                renewToken();
                try {
                    new HTTPRequest(server, String.format(PATH_DELETE_MESSAGE_PATTERN, id), HTTPRequestType.DELETE, getToken(), null).execute();
                    exception = null;
                } catch (Exception e1) {
                    exception = e1;
                }
            } else {
                exception = e;
            }
        }
        if (exception != null) {
            throw new BusException("Unable to delete message", exception);
        }
    }

    private String getToken() throws BusException {
        if (token == null) {
            renewToken();
        }
        return token;
    }

    private void renewToken() throws BusException {
            token = auth.authenticate();
    }

    static Object deserializeMessage(String message) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(message));
             GZIPInputStream gzis = new GZIPInputStream(bais);
             ObjectInputStream ois = new ObjectInputStream(gzis)) {
            return ois.readObject();
        }
    }

    private String serializeObject(Object message) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzos = new GZIPOutputStream(baos);
             ObjectOutputStream oos = new ObjectOutputStream(gzos)) {
            oos.writeObject(message);
            oos.flush();
            gzos.finish();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}
