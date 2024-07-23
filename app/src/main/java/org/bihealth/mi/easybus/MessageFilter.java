package org.bihealth.mi.easybus;

/**
 * Interface to filter relevant messages
 *
 * @author Felix Wirth
 */
public interface MessageFilter {

    /**
     * Checks whether a message is relevant
     *
     * @param messageDescription
     * @return
     */
    boolean accepts(String messageDescription);

}
