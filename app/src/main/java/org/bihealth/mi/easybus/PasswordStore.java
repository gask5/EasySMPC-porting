package org.bihealth.mi.easybus;

/**
 * A class storing up to two passwords
 *
 * @author Felix Wirth
 */
public class PasswordStore {
    /**
     * First password
     */
    private final String firstPassword;

    /**
     * Second password
     */
    private final String secondPassword;

    /**
     * Creates a new instance
     *
     * @param password
     */
    public PasswordStore(String password) {
        this(password, null);
    }

    /**
     * Creates a new instance. If second password is null, firstPassword will be assumed as secondPassword
     *
     * @param firstPassword
     * @param secondPassword
     */
    public PasswordStore(String firstPassword, String secondPassword) {

        // Check
        if (firstPassword == null) {
            throw new IllegalArgumentException("First password must not be null");
        }

        // Store
        this.firstPassword = firstPassword;
        this.secondPassword = secondPassword != null && !secondPassword.isBlank() ? secondPassword : firstPassword;
    }

    /**
     * @return the first password
     */
    public String getFirstPassword() {
        return firstPassword;
    }

    /**
     * @return the secondPassword
     */
    public String getSecondPassword() {
        return secondPassword;
    }
}
