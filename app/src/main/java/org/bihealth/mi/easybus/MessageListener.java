package org.bihealth.mi.easybus;

/**
 * An interface allowing the receiving of a message
 *
 * @author Felix Wirth
 */
public interface MessageListener {

    /**
     * Needs to be implemented in order to receive a message
     */
    void receive(String message);

    /**
     * Needs to be implemented in order to process an error
     */
    void receiveError(Exception exception);
}
