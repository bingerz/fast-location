package cn.bingerz.android.fastlocation;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import cn.bingerz.android.fastlocation.location.LocationCallbackListener;
import cn.bingerz.android.fastlocation.location.LocationParams;
import cn.bingerz.android.fastlocation.location.LocationProvider;
import cn.bingerz.android.fastlocation.location.LocationProviderFactory;
import cn.bingerz.android.fastlocation.utils.EasyLog;
import cn.bingerz.android.fastlocation.utils.PermissionUtils;

/**
 * Created by hanson on 01/03/2018.
 */

public class FastLocation {

    private static final int MSG_REQUEST_TIMEOUT = 0x11;

    private static final int TIMEOUT_REQUEST_LOCATION = 30 * 1000;

    private Context mContext;
    private Location mLastLocation;
    private LocationProvider mLocationProvider;
    private LocationParams mLocationParams;
    private long mLastProviderTimestamp = 0;

    private boolean isRequesting = false;

    private ArrayList<LocationResultListener> mLocationResultListeners;

    private MyHandler mHandler;

    private static final class MyHandler extends Handler {

        private final WeakReference<FastLocation> mReference;

        public MyHandler(FastLocation fastLocation) {
            mReference = new WeakReference<>(fastLocation);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FastLocation fastLocation = mReference.get();
            if (fastLocation != null) {
                fastLocation.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REQUEST_TIMEOUT:
                removeLocationUpdates();
                break;
        }
    }

    private FastLocation() {
    }

    public FastLocation(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid context");
        }
        this.mContext = context;
        this.mLocationProvider = LocationProviderFactory.getLocationProvider(mContext);
        EasyLog.setExplicitTag("FastLocation");
    }

    public void setLoggable(boolean enable) {
        EasyLog.setLoggable(enable);
    }

    public void setAppendTag(boolean enable) {
        EasyLog.setAppendTag(enable);
    }

    private MyHandler getHandler() {
        if (mHandler == null) {
            mHandler = new MyHandler(this);
        }
        return mHandler;
    }

    private LocationProvider getLocationProvider() {
        if (mLocationProvider == null) {
            mLocationProvider = LocationProviderFactory.getLocationProvider(mContext);
        }
        return mLocationProvider;
    }

    private LocationParams getLocationParams() {
        if (mLocationParams == null) {
            mLocationParams = LocationParams.MEDIUM_ACCURACY;
        }
        return mLocationParams;
    }

    private boolean requestLocationUpdates(LocationParams params) {
        PermissionUtils.checkLocationGranted(mContext);
        if (isRequesting) {
            EasyLog.w("Request location update is busy");
            return false;
        }
        getLocationProvider().request(new LocationCallbackListener() {
            @Override
            public void onLocationUpdated(Location location) {
                printf(location);
                mLastProviderTimestamp = location.getTime();
                doLocationResult(location);
                requestTimeoutMsgInit();
                isRequesting = false;
            }
        }, params);
        isRequesting = true;
        return true;
    }

    public void removeLocationUpdates() throws SecurityException, IllegalArgumentException {
        PermissionUtils.checkLocationGranted(mContext);
        if (getLocationProvider() != null) {
            getLocationProvider().remove();
        }
        isRequesting = false;
    }

    private boolean isNeedFilter(Location location) {
        if (mLastLocation != null && location != null) {
            float distance = location.distanceTo(mLastLocation);
            if (distance < getLocationParams().getAcceptableAccuracy()) {
                return true;
            }
            if (location.getAccuracy() >= mLastLocation.getAccuracy()
                    && distance < location.getAccuracy()) {
                return true;
            }
            return location.getTime() <= mLastProviderTimestamp;
        }
        return false;
    }

    private void doLocationResult(Location location) {
        checkLocation(location);

        if (isNeedFilter(location)) {
            EasyLog.d("location need to filtered out, timestamp is " + location.getTime());
            finishResult(mLastLocation);
        } else {
            finishResult(location);
        }
    }

    private void finishResult(Location location) {
        checkLocation(location);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();
        String provider = location.getProvider();

        if (mLocationResultListeners != null && !mLocationResultListeners.isEmpty()) {
            String format = "Location result:<%f, %f> Accuracy:%f Time:%d Provider:%s";
            EasyLog.i(String.format(format, latitude, longitude, accuracy, time, provider));

            mLastLocation = location;
            synchronized (this) {
                Iterator<LocationResultListener> iterator = mLocationResultListeners.iterator();
                while (iterator.hasNext()) {
                    LocationResultListener listener = iterator.next();
                    if (listener != null) {
                        listener.onResult(location);
                    }
                    iterator.remove();
                }
            }
        }
    }

    private void requestTimeoutMsgInit() {
        MyHandler handler = getHandler();
        handler.removeMessages(MSG_REQUEST_TIMEOUT);
    }

    private void delaySendRequestTimeout(long delayTime) {
        MyHandler handler = getHandler();
        handler.sendMessageDelayed(handler.obtainMessage(MSG_REQUEST_TIMEOUT), delayTime);
    }

    private boolean findResultListener(LocationResultListener locationResultListener) {
        if (mLocationResultListeners == null || mLocationResultListeners.isEmpty()) {
            return false;
        }
        for (LocationResultListener listener : mLocationResultListeners) {
            if (listener.equals(locationResultListener)) {
                return true;
            }
        }
        return false;
    }

    private void insertResultListener(LocationResultListener locationResultListener) {
        if (mLocationResultListeners == null) {
            mLocationResultListeners = new ArrayList<>();
        }
        synchronized (this) {
            if (locationResultListener != null && !findResultListener(locationResultListener)) {
                mLocationResultListeners.add(locationResultListener);
            }
        }
    }

    public void getLocation(LocationResultListener listener)
            throws SecurityException, IllegalStateException, IllegalArgumentException {
        getLocation(listener, null);
    }

    public void getLocation(LocationResultListener listener, LocationParams params)
            throws SecurityException, IllegalStateException, IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("invalid locationResultListener.");
        }

        insertResultListener(listener);
        mLocationParams = params != null ? params : LocationParams.MEDIUM_ACCURACY;
        getLocationProvider().getLastLocation(new LocationCallbackListener() {
            @Override
            public void onLocationUpdated(Location location) {
                if (getLocationParams().isAcceptableTime(location)
                        && getLocationParams().isAcceptableAccuracy(location)) {
                    EasyLog.d("return best last know location.");
                    finishResult(location);
                    return;
                }
                if (requestLocationUpdates(mLocationParams)) {
                    delaySendRequestTimeout(TIMEOUT_REQUEST_LOCATION);
                }
            }
        });
    }

    public void getLastKnowLocation(final LocationResultListener listener)
            throws SecurityException, IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("invalid locationResultListener.");
        }
        getLocationProvider().getLastLocation(new LocationCallbackListener() {
            @Override
            public void onLocationUpdated(Location location) {
                if (listener != null) {
                    listener.onResult(location);
                }
            }
        });
    }

    private void checkLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Invalid location.");
        }
    }

    private void printf(Location location) {
        if (location == null) {
            return;
        }
        String format = "Location print:(%f, %f) Accuracy:%f Time:%d Provider:%s";
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();
        String provider = location.getProvider();

        EasyLog.d(String.format(format, latitude, longitude, accuracy, time, provider));
    }
}
