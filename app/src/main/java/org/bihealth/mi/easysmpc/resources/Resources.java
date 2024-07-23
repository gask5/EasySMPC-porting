/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easysmpc.resources;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Accessor class for messages and settings used by the UI
 *
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class Resources {

    public static final int ROW_GAP = 2;
    /**
     * Row gap large
     */
    public static final int ROW_GAP_LARGE = 5;
    /**
     * X-size of small dialog
     */
    public static final int SIZE_DIALOG_SMALL_X = 450;
    /**
     * Y-size of small dialog
     */
    public static final int SIZE_DIALOG_SMALL_Y = 185;
    /**
     * String indicating start of exchange string
     */
    public static final String MESSAGE_START_TAG = "BEGIN_PAYLOAD";                               //$NON-NLS-1$
    /**
     * String indicating end of exchange string
     */
    public static final String MESSAGE_END_TAG = "END_PAYLOAD";                                 //$NON-NLS-1$
    /**
     * Char length for exchange before line break
     */
    public static final int MESSAGE_LINE_WIDTH = 150;
    /**
     * Preferred height for the progress container
     */
    public static final int PROGRESS_PREFERRED_HEIGHT = 50;
    /**
     * Ending for project files
     */
    public static final String FILE_ENDING = "smpc";                                                                 //$NON-NLS-1$
    /**
     * About dialog size x
     */
    public static final int SIZE_DIALOG_X = 500;
    /**
     * About dialog size y
     */
    public static final int SIZE_DIALOG_Y = 300;
    /**
     * Size of loading animation
     */
    public static final int SIZE_LOADING_ANIMATION = 15;
    /**
     * Size of checkmark clipart x
     */
    public static final int SIZE_CHECKMARK_X = 15;
    /**
     * Size of checkmark clipart y
     */
    public static final int SIZE_CHECKMARK_Y = 12;
    /**
     * Interval schedule for tasks in background
     */
    public static final long INTERVAL_SCHEDULER_MILLISECONDS = 200;
    /**
     * File ending for CSV-files
     */
    public static final String FILE_ENDING_CSV = "csv";                                                                  //$NON-NLS-1$
    /**
     * File ending for Excel-2007-files
     */
    public static final String FILE_ENDING_EXCEL_XLSX = "xlsx";                                                                 //$NON-NLS-1$
    /**
     * File ending for Excel-97-files
     */
    public static final String FILE_ENDING_EXCEL_XLS = "xls";                                                                  //$NON-NLS-1$
    /**
     * Maximal rows considered
     */
    public static final int MAX_COUNT_ROWS = 250000;
    /**
     * Maximal columns considered
     */
    public static final int MAX_COUNT_COLUMNS = 250000;
    /**
     * Delimiters considered for CSV files
     */
    public static final char[] DELIMITERS = {';', ',', '|', '\t'};                                                // $NON-NLS-1$
    /**
     * Maximum number of lines to be loaded for preview purposes for CSV file
     * detection.
     */
    public static final int PREVIEW_MAX_LINES = 25;
    /**
     * Maximum number of chars to be loaded for detecting separators for CSV
     * file detection.
     */
    public static final int DETECT_MAX_CHARS = 100000;
    /**
     * Interval to check mail box automatically in milliseconds
     */
    public static final int INTERVAL_CHECK_MAILBOX_DEFAULT = 30000;
    /**
     * Round for initial e-mails
     */
    public static final String ROUND_0 = "_round0";
    /**
     * Step 1 identifier
     */
    public static final String ROUND_1 = "_round1";
    /**
     * Step 2 identifier
     */
    public static final String ROUND_2 = "_round2";
    /**
     * Interval to wait for sending e-mails in milliseconds
     */
    public static final int TIMEOUT_SEND_EMAILS_DEFAULT = 60000;
    /**
     * Fetch size for messages with IMAP
     */
    public static final int FETCH_SIZE_IMAP = 1048576;
    /**
     * Number of threads in thread pool
     */
    public static final int SIZE_THREADPOOL = 5;
    /**
     * Fractional bits for decimal values
     */
    public static final int FRACTIONAL_BITS = 32;
    /**
     * Aggregation delimiter
     */
    public static final String AGGREGATION_DELIMITER = "X";
    /**
     * Default message size for e-mails
     */
    public static final int EMAIL_MAX_MESSAGE_SIZE_DEFAULT = 10 * 1024 * 1024;
    /**
     * CLI stop processing string
     */
    public static final String STOP_CLI_PROCESS_STRING = "stop";
    /**
     * Retries when sending with HTTP for Easybackend
     */
    public static final int RETRY_EASYBACKEND_NUMBER_RETRY = 5;
    /**
     * Wait time between Retries for Easybackend
     */
    public static final int RETRY_EASYBACKEND_WAIT_TIME_RETRY = 5000;
    /**
     * Timeout for Easybackend requests
     */
    public static final int TIMEOUT_EASYBACKEND = 30000;
    /**
     * Auth realm default
     */
    public static final String AUTH_REALM_DEFAULT = "easybackend";
    /**
     * Auth client id
     */
    public static final String AUTH_CLIENTID_DEFAULT = "easy-client";
    /**
     * Interval to check easybackend automatically in milliseconds
     */
    public static final int INTERVAL_CHECK_EASYBACKEND_DEFAULT = 10000;
    /**
     * Bundle name
     */
    private static final String BUNDLE_NAME = "org.bihealth.mi.easysmpc.resources.messages"; //$NON-NLS-1$
    /**
     * The charset used to read the license text
     */
    private final static Charset CHARSET = StandardCharsets.UTF_8;
    /**
     * Available languages
     */
    private static final Locale[] AVAILABLE_LANGUAGES = {Locale.ENGLISH, Locale.GERMAN};
    /**
     * Bundle
     */
    private static ResourceBundle resource_bundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * No instantiation
     */
    private Resources() {
        // Empty by design
    }

    /**
     * Icon
     *
     * @return
     * @throws IOException
     */

    /**
     * Returns all available languages
     *
     * @return
     */
    public static Locale[] getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }

    /**
     * Get locale of resource bundle
     *
     * @return
     */
    public static Locale getResourceBundleLocale() {
        return resource_bundle.getLocale();
    }

    /**
     * Set locale of resource bundle
     *
     * @return
     */
    public static void setResourceBundleLocale(Locale locale) {
        Locale.setDefault(Locale.ENGLISH);
        resource_bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    /**
     * Returns a message
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        try {
            return resource_bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
