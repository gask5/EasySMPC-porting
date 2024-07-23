package org.bihealth.mi.easybus;

/**
 * Listener for performance indicators
 *
 * @author Fabian Prasser
 */
public interface PerformanceListener {

    /**
     * Message received
     *
     * @param size
     */
    void messageReceived(long size);

    /**
     * Message sent
     *
     * @param size
     */
    void messageSent(long size);
}
