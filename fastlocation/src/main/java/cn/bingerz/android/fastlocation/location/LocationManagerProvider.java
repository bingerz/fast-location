package cn.bingerz.android.fastlocation.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import cn.bingerz.android.fastlocation.utils.EasyLog;
import cn.bingerz.android.fastlocation.utils.PermissionUtils;

/**
 * @author hanson
 */
public class LocationManagerProvider implements LocationProvider {

    private Context mContext = null;
    private LocationParams mLocationParams;
    private LocationManager mLocationManager;
    private LocationCallbackListener mLocationCallbackListener;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                EasyLog.e("LocationListener callback location is null.");
                return;
            }
            if (mLocationCallbackListener != null) {
                mLocationCallbackListener.onLocationUpdated(location);
            }
            remove();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid context");
        }
        this.mContext = context;
    }

    private Criteria getCriteria(LocationParams params) {
        final LocationAccuracy accuracy = params.getAccuracy();
        final Criteria criteria = new Criteria();
        switch (accuracy) {
            case HIGH:
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                break;
            case MEDIUM:
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setBearingAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                break;
            case LOW:
            default:
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_LOW);
                criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_LOW);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
        return criteria;
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void request(final LocationCallbackListener listener, LocationParams params) {
        if (listener == null) {
            EasyLog.w("LocationCallbackListener is null.");
        }
        mLocationCallbackListener = listener;
        mLocationParams = params;
        long minTime = getLocationParams().getInterval();
        float minDistance = getLocationParams().getDistance();
        Criteria criteria = getCriteria(getLocationParams());
        getLocationManager().requestLocationUpdates(minTime, minDistance, criteria, mLocationListener, Looper.getMainLooper());
        EasyLog.d("Location request update. accuracy = %s", mLocationParams.getAccuracy());
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void remove() {
        try {
            LocationManager locationManager = getLocationManager();
            if (locationManager != null) {
                locationManager.removeUpdates(mLocationListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getLastLocation(LocationCallbackListener listener) {
        if (listener == null) {
            EasyLog.w("LocationCallbackListener is null.");
        }
        Location lastLocation;
        Location gpsLocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
        Location networkLocation = getLocationByProvider(LocationManager.NETWORK_PROVIDER);

        if (gpsLocation == null) {
            EasyLog.d("GPS Location is null");
            lastLocation = networkLocation;
        } else if (networkLocation == null) {
            EasyLog.d("Network Location is null");
            lastLocation = gpsLocation;
        } else if (getLocationParams().isAcceptableTime(gpsLocation)) {
            EasyLog.d("Returning current GPS Location");
            lastLocation = gpsLocation;
        } else if (getLocationParams().isAcceptableTime(networkLocation)) {
            EasyLog.d("GPS is old, Network is current, returning network");
            lastLocation = networkLocation;
        } else if (gpsLocation.getTime() > networkLocation.getTime()) {
            // both are old, return the newer of those two
            lastLocation = gpsLocation;
        } else {
            lastLocation = networkLocation;
        }
        if (listener != null) {
            listener.onLocationUpdated(lastLocation);
        }
    }

    private LocationManager getLocationManager() {
        checkRuntimeEnvironment();
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }

    private LocationParams getLocationParams() {
        if (mLocationParams == null) {
            mLocationParams = LocationParams.MEDIUM_ACCURACY;
        }
        return mLocationParams;
    }

    @SuppressWarnings({"MissingPermission"})
    private Location getLocationByProvider(String provider) {
        PermissionUtils.checkLocationGranted(mContext);

        if (!isSupportedProvider(provider)) {
            return null;
        }
        Location location = null;
        LocationManager locationManager = getLocationManager();
        if (locationManager.isProviderEnabled(provider)) {
            location = locationManager.getLastKnownLocation(provider);
        }
        return location;
    }

    private boolean isSupportedProvider(String provider) {
        checkProvider(provider);
        return provider.equals(LocationManager.GPS_PROVIDER)
                || provider.equals(LocationManager.NETWORK_PROVIDER);
    }

    private void checkProvider(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Invalid provider.");
        }
    }

    private void checkRuntimeEnvironment() {
        if (mContext == null) {
            throw new IllegalStateException("Application context is not initialized.");
        }
    }
}
