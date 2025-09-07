package Utility;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedPref {

    private static final String PREFS_NAME = "encrypted_user_prefs";
    private static final String TAG = "SharedPref";

    private static SharedPref instance;
    private SharedPreferences encryptedPrefs;
    private SharedPreferences.Editor editor;

    // Private constructor to prevent direct instantiation
    private SharedPref() {}

    /**
     * Thread-safe singleton instance creation
     * Must be called once with application context during app initialization
     */
    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new SharedPref();
            instance.setupEncryptedPreferences(context.getApplicationContext());
        }
    }

    /**
     * Get singleton instance
     * @throws IllegalStateException if not initialized
     */
    public static synchronized SharedPref getInstance() {
        if (instance == null || instance.encryptedPrefs == null) {
            throw new IllegalStateException("SharedPref not initialized. Call initialize(Context) first.");
        }
        return instance;
    }

    /**
     * Setup encrypted SharedPreferences with master key from Android Keystore
     */
    private void setupEncryptedPreferences(Context context) {
        try {
            // Create master key using updated MasterKey.Builder (replaces deprecated MasterKeys)
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Create encrypted SharedPreferences instance
            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to create encrypted preferences", e);
        }
    }

    // === USER PERSONAL DATA STORAGE METHODS ===

    public void setUserFirstName(String firstName) {
        getEditor().putString("user_first_name", firstName).apply();
    }

    public String getUserFirstName() {
        return encryptedPrefs.getString("user_first_name", "");
    }

    public void setUserLastName(String lastName) {
        getEditor().putString("user_last_name", lastName).apply();
    }

    public String getUserLastName() {
        return encryptedPrefs.getString("user_last_name", "");
    }

    public void setUserMiddleName(String middleName) {
        getEditor().putString("user_middle_name", middleName).apply();
    }

    public String getUserMiddleName() {
        return encryptedPrefs.getString("user_middle_name", "");
    }

    public void setIsEmailVerified(boolean hasVerifiedGmail) {
        getEditor().putBoolean("is_verified_email", hasVerifiedGmail).apply();
    }

    public boolean getHasUserVerifiedGmail() {
        return encryptedPrefs.getBoolean("is_verified_email", false);
    }

    /**
     * Store user email securely
     */
    public void setUserEmail(String email) {
        getEditor().putString("user_email", email).apply();
    }

    public String getUserEmail() {
        return encryptedPrefs.getString("user_email", "");
    }

    /**
     * Store user phone number securely
     */
    public void setUserPhone(String phone) {
        getEditor().putString("user_phone", phone).apply();
    }

    public String getUserPhone() {
        return encryptedPrefs.getString("user_phone", "");
    }

    /**
     * Store user authentication token securely
     */
    public void setAuthToken(String token) {
        getEditor().putString("auth_token", token).apply();
    }

    public String getAuthToken() {
        return encryptedPrefs.getString("auth_token", "");
    }

    /**
     * Store user personal details as JSON
     */
    public void setUserProfile(String profileJson) {
        getEditor().putString("user_profile", profileJson).apply();
    }

    public String getUserProfile() {
        return encryptedPrefs.getString("user_profile", "{}");
    }

    // === GENERIC DATA STORAGE METHODS ===

    public void putString(String key, String value) {
        getEditor().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return encryptedPrefs.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        getEditor().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return encryptedPrefs.getInt(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return encryptedPrefs.getBoolean(key, defaultValue);
    }

    public void putLong(String key, long value) {
        getEditor().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return encryptedPrefs.getLong(key, defaultValue);
    }

    public void putFloat(String key, float value) {
        getEditor().putFloat(key, value).apply();
    }

    public float getFloat(String key, float defaultValue) {
        return encryptedPrefs.getFloat(key, defaultValue);
    }


    // === UTILITY METHODS ===

    /**
     * Check if a key exists in preferences
     */
    public boolean contains(String key) {
        return encryptedPrefs.contains(key);
    }

    /**
     * Remove specific key from preferences
     */
    public void remove(String key) {
        getEditor().remove(key).apply();
    }

    /**
     * Clear all stored preferences (use with caution)
     */
    public void clearAll() {
        getEditor().clear().apply();
    }

    /**
     * Remove all user personal data
     */
    public void clearUserData() {
        getEditor()
                .remove("user_email")
                .remove("user_phone")
                .remove("auth_token")
                .remove("user_profile")
                .apply();
    }

    /**
     * Get editor instance for batch operations
     */
    private SharedPreferences.Editor getEditor() {
        return encryptedPrefs.edit();
    }

    /**
     * Batch operation support - get editor for multiple operations before commit
     */
    public BatchEditor getBatchEditor() {
        return new BatchEditor(encryptedPrefs.edit());
    }

    /**
     * Helper class for batch operations
     */
    public static class BatchEditor {
        private final SharedPreferences.Editor editor;

        private BatchEditor(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        public BatchEditor putString(String key, String value) {
            editor.putString(key, value);
            return this;
        }

        public BatchEditor putInt(String key, int value) {
            editor.putInt(key, value);
            return this;
        }

        public BatchEditor putBoolean(String key, boolean value) {
            editor.putBoolean(key, value);
            return this;
        }

        public BatchEditor putLong(String key, long value) {
            editor.putLong(key, value);
            return this;
        }

        public BatchEditor putFloat(String key, float value) {
            editor.putFloat(key, value);
            return this;
        }

        public BatchEditor remove(String key) {
            editor.remove(key);
            return this;
        }

        /**
         * Commit all batched operations
         */
        public void commit() {
            editor.apply();
        }

        /**
         * Commit all batched operations synchronously
         */
        public boolean commitSync() {
            return editor.commit();
        }
    }
}
