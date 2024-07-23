package org.bihealth.mi.easybus;

/**
 * Exception to handle errors in the bus
 *
 * @author Felix Wirth
 */
public class BusException extends Exception {

    /**
     * SVID
     */
    private static final long serialVersionUID = 5462783962126095816L;

    /**
     * Creates a new instance
     *
     * @param message
     */
    public BusException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param message
     * @param cause
     */
    public BusException(String message, Throwable cause) {
        super(message, cause);
    }
}