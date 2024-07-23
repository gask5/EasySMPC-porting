package com.example.easysmpc_porting.App;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;

import java.net.MalformedURLException;
import java.net.URL;

public class ConnectionSettingsParserEasyBackend extends ConnectionSettingsParser {

    private final String serverUrl;
    private final String password;
    private final String authServerUrl;
    private final String authRealm;
    private final String authClientId;
    private final String authClientSecret;
    private final String proxyUrl;
    private final int mailboxCheckInterval; // in seconds
    private final int sendTimeout; // in seconds
    private final int maxMessageSize; // in MB

    /**
     * Constructor
     */
    public ConnectionSettingsParserEasyBackend(String serverUrl,
                                               String password,
                                               String authServerUrl,
                                               String authRealm,
                                               String authClientId,
                                               String authClientSecret,
                                               String proxyUrl,
                                               int mailboxCheckInterval,
                                               int sendTimeout,
                                               int maxMessageSize) {
        this.serverUrl = serverUrl;
        this.password = password;
        this.authServerUrl = authServerUrl;
        this.authRealm = authRealm;
        this.authClientId = authClientId;
        this.authClientSecret = authClientSecret;
        this.proxyUrl = proxyUrl;
        this.mailboxCheckInterval = mailboxCheckInterval;
        this.sendTimeout = sendTimeout;
        this.maxMessageSize = maxMessageSize;
    }


    @Override
    public ConnectionSettings getConnectionSettings(String email) {
        try {
            ConnectionSettingsEasyBackend result = new ConnectionSettingsEasyBackend(email, null)
                    .setAPIServer(new URL(serverUrl))
                    .setMaxMessageSize(maxMessageSize * 1024 * 1024)
                    .setSendTimeout(sendTimeout * 1000)
                    .setCheckInterval(mailboxCheckInterval * 1000);
            result.setPasswordStore(new PasswordStore(password));

            if (authServerUrl != null) {
                result.setAuthServer(new URL(authServerUrl));
            }

            if (authRealm != null) {
                result.setRealm(authRealm);
            }

            if (authClientId != null) {
                result.setClientId(authClientId);
            }

            if (authClientSecret != null) {
                result.setClientSecret(authClientSecret);
            }

            if (proxyUrl != null) {
                result.setProxy(new URL(proxyUrl).toURI());
            }

            return result;
        } catch (MalformedURLException | IllegalArgumentException | NullPointerException |
                 IllegalStateException | java.net.URISyntaxException e) {
            throw new IllegalStateException("Unable to create connection settings object", e);
        }
    }
}
