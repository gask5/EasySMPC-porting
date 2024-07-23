package org.bihealth.mi.easybus;

import org.bihealth.mi.easysmpc.resources.Resources;

import java.io.Serializable;

/**
 * Generic settings
 *
 * @author Felix Wirth
 */
public abstract class ConnectionSettings implements Serializable {

    /**
     * SVUID
     */
    private static final long serialVersionUID = -3887172032343688839L;
    /**
     * Password store
     */
    private transient PasswordStore passwordStore;

    /**
     * Returns the identifier
     *
     * @return
     */
    public abstract String getIdentifier();

    /**
     * Returns whether this connection is valid
     *
     * @param usePasswordProvider
     * @return
     */
    public abstract boolean isValid(boolean usePasswordProvider);

    /**
     * Return the check interval
     *
     * @return
     */
    public abstract int getCheckInterval();

    /**
     * Get send timeout
     *
     * @return
     */
    public abstract int getSendTimeout();

    /**
     * @return the passwordStore
     */
    public PasswordStore getPasswordStore() {
        return passwordStore;
    }

    /**
     * @param passwordStore the passwordStore to set
     */
    public ConnectionSettings setPasswordStore(PasswordStore passwordStore) {
        this.passwordStore = passwordStore;
        return this;
    }

    /**
     * @return the maxMessageSize
     */
    public abstract int getMaxMessageSize();

    /**
     * Returns the exchange mode
     *
     * @return
     */
    public abstract ExchangeMode getExchangeMode();

    /**
     * Connection types
     */
    public enum ExchangeMode {
        /**
         * Enum values
         */
        MANUAL, EMAIL, EASYBACKEND;

        public String toString() {
            return Resources.getString(String.format("ExchangeMode.%s", this.name()));
        }
    }
}