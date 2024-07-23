package org.bihealth.mi.easybus;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the connection to obtain initial messages
 *
 * @author Felix Wirth
 */
public abstract class InitialMessageManager {

    /**
     * Action to perform with new messages
     */
    private final Consumer<List<BusMessage>> actionUpdateMessage;
    /**
     * Action if error occurs
     */
    private final Consumer<Exception> actionError;
    /**
     * Check interval
     */
    private final int checkInterval;
    /**
     * Stop flag
     */
    private volatile boolean stop = false;

    /**
     * Creates a new instance
     *
     * @param actionUpdateMessage
     * @param actionError
     * @param checkInterval
     */
    public InitialMessageManager(Consumer<List<BusMessage>> actionUpdateMessage, Consumer<Exception> actionError, int checkInterval) {

        // Store
        this.actionUpdateMessage = actionUpdateMessage;
        this.actionError = actionError;
        this.checkInterval = checkInterval;
    }

    /**
     * Stops the manager
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * Start manager
     */
    public void start() {
        while (!stop) {
            // Update messages
            try {
                actionUpdateMessage.accept(retrieveMessages());
            } catch (IllegalStateException e) {
                this.stop = true;
                actionError.accept(e);
            }

            // Sleep
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                // Empty
            }
        }
    }

    /**
     * Retrieve messages
     *
     * @return
     */
    public abstract List<BusMessage> retrieveMessages() throws IllegalStateException;

    /**
     * @return the actionError
     */
    protected void processError(Exception e) {
        actionError.accept(e);
        throw new IllegalStateException("Unable to process messages", e);
    }
}
