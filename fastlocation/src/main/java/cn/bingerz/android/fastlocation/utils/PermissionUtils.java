package cn.bingerz.android.fastlocation.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

/**
 * @author hanson
 */
public class PermissionUtils {

    public static boolean isGranted(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity, String[] permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }

    public static boolean isGrantedLocation(Context context) {
        return context != null && isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                && isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static void requestPermission(Activity activity, int requestCode) {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermission(activity, permissions, requestCode);
    }

    public static void checkLocationGranted(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null.");
        }
        if (!PermissionUtils.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            throw new SecurityException("The app does not have ACCESS_FINE_LOCATION permission.");
        }

        if (!PermissionUtils.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            throw new SecurityException("The app does not have ACCESS_COARSE_LOCATION permission.");
        }
    }
}
