package cn.bingerz.android.fastlocation.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author hanson
 */
public class Utils {

    /**
     * 检测定位是否打开
     */
    public static boolean isEnabled(Context ctx) {
        return isNetworkEnabled(ctx) || (isGpsEnabled(ctx) && isNetworkEnabled(ctx));
    }

    /**
     * 检测GPS是否打开
     */
    public static boolean isGpsEnabled(Context ctx) {
        try {
            LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检测Network定位是否打开
     */
    public static boolean isNetworkEnabled(Context ctx) {
        try {
            LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the network info
     */
    private static NetworkInfo getNetworkInfo(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected());
    }
}
