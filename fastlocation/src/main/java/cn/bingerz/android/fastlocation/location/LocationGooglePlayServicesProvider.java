package cn.bingerz.android.fastlocation.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * @author hanson
 */
import cn.bingerz.android.fastlocation.utils.EasyLog;

public class LocationGooglePlayServicesProvider implements LocationProvider {

    private Context mContext = null;
    private LocationParams mLocationParams;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallbackListener mLocationCallbackListener;

    private CancellationToken mCancellationToken;
    private CancellationTokenSource mCancellationTokenSource;

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                if (mLocationCallbackListener != null) {
                    mLocationCallbackListener.onLocationUpdated(location);
                }
            }
        }
    };

    @Override
    public void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid context");
        }
        this.mContext = context;
        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        this.mCancellationTokenSource = new CancellationTokenSource();
    }

    private LocationRequest getLocationRequest(LocationParams params) {
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(params.getInterval())
                .setInterval(params.getInterval())
                .setSmallestDisplacement(params.getDistance());

        switch (params.getAccuracy()) {
            case HIGH:
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            case MEDIUM:
                request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case LOW:
                request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            default:
                break;
        }
        return request;
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void request(LocationParams params, LocationCallbackListener listener) {
        if (listener == null) {
            EasyLog.w("LocationCallbackListener is null.");
        }
        mLocationCallbackListener = listener;
        mLocationParams = params;
        LocationRequest request = getLocationRequest(getLocationParams());
        getLocationClient().requestLocationUpdates(request, mLocationCallback, Looper.getMainLooper());
        EasyLog.d("Location request update. accuracy = %s", mLocationParams.getAccuracy());
    }

    private CurrentLocationRequest getCurrentLocationRequest(LocationParams params) {
        CurrentLocationRequest.Builder builder = new CurrentLocationRequest.Builder()
                .setDurationMillis(params.getAcceptableTime())
                .setMaxUpdateAgeMillis(params.getAcceptableTime())
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL);

        switch (params.getAccuracy()) {
            case HIGH:
                builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                break;
            case MEDIUM:
                builder.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case LOW:
                builder.setPriority(Priority.PRIORITY_LOW_POWER);
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
        CurrentLocationRequest request = getCurrentLocationRequest(params);
        mCancellationToken = mCancellationTokenSource.getToken();
        getLocationClient().getCurrentLocation(request, mCancellationToken)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (listener != null) {
                    listener.onLocationUpdated(location);
                }
            }
        });
        EasyLog.d("Location request single update. accuracy = %s", mLocationParams.getAccuracy());
    }

    @Override
    public void remove() {
        try {
            if (mLocationCallback != null) {
                getLocationClient().removeLocationUpdates(mLocationCallback);
            }
            if (mCancellationToken != null && mCancellationToken.isCancellationRequested()) {
                mCancellationTokenSource.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void getLastLocation(final LocationCallbackListener listener) {
        if (listener == null) {
            EasyLog.w("LocationCallbackListener is null.");
        }
        getLocationClient().getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (listener != null) {
                    listener.onLocationUpdated(location);
                }
            }
        });
    }

    private FusedLocationProviderClient getLocationClient() {
        checkRuntimeEnvironment();
        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        }
        return mFusedLocationClient;
    }

    private LocationParams getLocationParams() {
        if (mLocationParams == null) {
            mLocationParams = LocationParams.MEDIUM_ACCURACY;
        }
        return mLocationParams;
    }

    private void checkRuntimeEnvironment() {
        if (mContext == null) {
            throw new IllegalStateException("Application context is not initialized.");
        }
    }
}
