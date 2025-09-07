package Utility;

/**
 * A utility class to hold application-wide constants.
 * By declaring the class as 'final', we prevent it from being extended, which
 * is a good practice for utility classes that are not designed for inheritance.
 */
public final class AppConstants {

    /**
     * A private constructor prevents this utility class from being instantiated.
     * All members are static, so you should never need to create an object of this class.
     * This enforces the utility pattern.
     */
    private AppConstants() {
        // This class should not be instantiated.
    }

    public static final String USER_FIRST_NAME_KEY = "user_first_name";
    public static final String USER_MIDDLE_NAME_KEY = "user_middle_name";
    public static final String USER_LAST_NAME_KEY = "user_last_name";
    public static final String USER_EMAIL_KEY = "user_email";
    public static final String USER_PHONE_KEY = "user_phone";
    public static final String USER_PHONE_OR_EMAIL_KEY = "user_phone_or_email";
    public static final String USER_PASSWORD_KEY = "user_password";
    public static final String USER_CONFIRM_PASSWORD_KEY = "user_confirm_password";
}