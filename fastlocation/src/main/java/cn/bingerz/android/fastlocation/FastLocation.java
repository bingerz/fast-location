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

public class FastLocation {

    private static final int MSG_REQUEST_TIMEOUT = 0x11;

    private static final int TIMEOUT_REQUEST_LOCATION = 15 * 1000;

    private Context mContext;
    private LocationProvider mLocationProvider;
    private LocationParams mLocationParams;

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
                if (mLocationParams != null && mLocationParams == LocationParams.HIGH_ACCURACY) {
                    mLocationParams = LocationParams.MEDIUM_ACCURACY;
                    if (requestLocationUpdates(mLocationParams)) {
                        delaySendRequestTimeout(TIMEOUT_REQUEST_LOCATION);
                        return;
                    }
                }
                finishResult(null);
                break;
            default:
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

    public LocationParams getLocationParams() {
        if (mLocationParams == null) {
            mLocationParams = LocationParams.MEDIUM_ACCURACY;
        }
        return mLocationParams;
    }

    private boolean requestLocationUpdates(LocationParams params) {
        try {
            PermissionUtils.checkLocationGranted(mContext);
            if (isRequesting) {
                EasyLog.w("Request location update is busy");
                return false;
            }
            getLocationProvider().request(new LocationCallbackListener() {
                @Override
                public void onLocationUpdated(Location location) {
                    printf(location);
                    finishResult(location);
                    requestTimeoutMsgInit();
                    isRequesting = false;
                }
            }, params);
            isRequesting = true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void removeLocationUpdates() {
        try {
            PermissionUtils.checkLocationGranted(mContext);
            if (getLocationProvider() != null) {
                getLocationProvider().remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRequesting = false;
    }

    private Location correctLocationTime(Location location) {
        long currentTimeMillis = System.currentTimeMillis();
        if (location != null) {
            long diff = currentTimeMillis - location.getTime();
            if (diff > 7 * 24 * 60 * 60 * 1000) {
                EasyLog.d("Location result need correct time");
                location.setTime(currentTimeMillis);
            }
        }
        return location;
    }

    private void finishResult(Location location) {
        finishResultListener(correctLocationTime(location));
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
        synchronized (FastLocation.class) {
            if (locationResultListener != null && !findResultListener(locationResultListener)) {
                mLocationResultListeners.add(locationResultListener);
            }
        }
    }

    private void finishResultListener(Location location) {
        synchronized (FastLocation.class) {
            if (mLocationResultListeners != null && !mLocationResultListeners.isEmpty()) {
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

    public boolean isRequesting() {
        return isRequesting;
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
        if (requestLocationUpdates(mLocationParams)) {
            delaySendRequestTimeout(TIMEOUT_REQUEST_LOCATION);
        }
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
