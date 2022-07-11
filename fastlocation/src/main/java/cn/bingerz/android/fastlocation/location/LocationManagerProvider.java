package cn.bingerz.android.fastlocation.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import java.util.function.Consumer;

import cn.bingerz.android.fastlocation.utils.EasyLog;
import cn.bingerz.android.fastlocation.utils.PermissionUtils;

/**
 * @author hanson
 */
public class LocationManagerProvider implements LocationProvider {

    private Context mContext = null;
    private LocationParams mLocationParams;
    private LocationManager mLocationManager;
    private CancellationSignal mCancellationSignal;
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

    private Consumer<Location> mLocationConsumer = new Consumer<Location>() {
        @Override
        public void accept(Location location) {
            if (location == null) {
                EasyLog.e("LocationConsumer callback location is null.");
                return;
            }
            if (mLocationCallbackListener != null) {
                mLocationCallbackListener.onLocationUpdated(location);
            }
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
    public void request(LocationParams params, final LocationCallbackListener listener) {
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


    @RequiresApi(api = Build.VERSION_CODES.S)
    private LocationRequest getLocationRequest(boolean isSingle, LocationParams params) {
        LocationRequest.Builder builder = new LocationRequest.Builder(params.getInterval())
                .setMaxUpdates(isSingle ? 1 : Integer.MAX_VALUE)
                .setDurationMillis(params.getAcceptableTime())
                .setMaxUpdateDelayMillis(params.getAcceptableTime())
                .setMinUpdateDistanceMeters(params.getDistance())
                .setMinUpdateIntervalMillis(params.getInterval());

        switch (params.getAccuracy()) {
            case HIGH:
                builder.setQuality(LocationRequest.QUALITY_HIGH_ACCURACY);
                break;
            case MEDIUM:
                builder.setQuality(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY);
                break;
            case LOW:
                builder.setQuality(LocationRequest.QUALITY_LOW_POWER);
                break;
            default:
                break;
        }
        return builder.build();
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void requestSingle(LocationParams params, LocationCallbackListener listener) {
        if (listener == null) {
            EasyLog.w("LocationCallbackListener is null.");
        }
        mLocationCallbackListener = listener;
        mLocationParams = params;
        Criteria criteria = getCriteria(getLocationParams());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String provider = getProvider(params);
            LocationRequest request = getLocationRequest(true, params);
            mCancellationSignal = new CancellationSignal();
            getLocationManager().getCurrentLocation(provider, request, mCancellationSignal,
                                                    mContext.getMainExecutor(), mLocationConsumer);
        } else {
            getLocationManager().requestSingleUpdate(criteria, mLocationListener, Looper.getMainLooper());
        }
        EasyLog.d("Location request single update. accuracy = %s", mLocationParams.getAccuracy());
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void remove() {
        try {
            LocationManager locationManager = getLocationManager();
            if (locationManager != null) {
                locationManager.removeUpdates(mLocationListener);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                    mCancellationSignal.cancel();
                }
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
        Location gpsLocation = getLastLocationByProvider(LocationManager.GPS_PROVIDER);
        Location networkLocation = getLastLocationByProvider(LocationManager.NETWORK_PROVIDER);

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
    private Location getLastLocationByProvider(String provider) {
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

    private String getProvider(LocationParams params) {
        boolean isHighAccuracy = params != null && params.getAccuracy() == LocationAccuracy.HIGH;
        return isHighAccuracy ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
    }

    private void checkRuntimeEnvironment() {
        if (mContext == null) {
            throw new IllegalStateException("Application context is not initialized.");
        }
    }
}
