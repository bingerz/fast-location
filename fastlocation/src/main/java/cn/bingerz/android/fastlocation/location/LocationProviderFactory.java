package cn.bingerz.android.fastlocation.location;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import cn.bingerz.android.fastlocation.utils.Utils;

/**
 * @author hanson
 */
public class LocationProviderFactory {

    public static LocationProvider getLocationProvider(Context context) {
        LocationProvider locationProvider;
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (!Utils.isChina(context) && result == ConnectionResult.SUCCESS) {
            locationProvider = new LocationGooglePlayServicesProvider();
        } else {
            locationProvider = new LocationManagerProvider();
        }
        locationProvider.init(context);
        return locationProvider;
    }
}
