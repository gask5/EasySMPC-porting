package org.bihealth.mi.easybus;

import android.util.Log;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * A participant (either sender or receiver)
 *
 * @author Felix Wirth
 */
public class Participant implements Serializable {

    /**
     * SVUID
     */
    private static final long serialVersionUID = 4218866719460664961L;

    /**
     * Regex to check for a correct mail address
     */
    private static final Pattern CHECK_EMAIL_REGEX = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
    /**
     * Name
     */
    private final String name;
    /**
     * E-mail address
     */
    private final String emailAddress;

    /**
     * Creates a new instance
     *
     * @throws BusException
     */
    public Participant(String name, String emailAddress) throws BusException {
        if (!isEmailValid(emailAddress)) {
            Log.e("Error", "User name is not a valid e-mail address");
            throw new BusException("User name is not a valid e-mail address");
        }
        this.name = name;
        this.emailAddress = emailAddress;
    }

    /**
     * Check if an e-mail address is valid
     *
     * @return e-mail is valid
     */
    public static boolean isEmailValid(String emailAddress) {
        return CHECK_EMAIL_REGEX.matcher(emailAddress).matches();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Participant other = (Participant) obj;
        if (emailAddress == null) {
            if (other.emailAddress != null) return false;
        } else if (!emailAddress.equals(other.emailAddress)) return false;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

    /**
     * Returns the e-mail address
     *
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}