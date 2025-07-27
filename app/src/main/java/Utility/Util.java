package Utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.digihealthlocker.UserAuthenticationActivity;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class Util {
    public static void makeToast(Context context, String message, int length) {
        Toast.makeText(context, message, length).show();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmailAddress(String email) {
        if (Util.isNullOrEmpty(email)) {
            return false;
        }
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate(); // This method performs the validation
            return true;
        } catch (AddressException ex) {
            Log.e("Util", "Invalid email address: " + email + ". Error: " + ex);
        }
        return false;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.trim().replaceAll("[\\s-]", "");

        return phoneNumber.matches("^[6-9]\\d{9}$");
    }
}
