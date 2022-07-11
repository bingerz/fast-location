package cn.bingerz.android.fastlocationsample;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import cn.bingerz.android.fastlocation.FastLocation;
import cn.bingerz.android.fastlocation.LocationResultListener;
import cn.bingerz.android.fastlocation.location.LocationParams;
import cn.bingerz.android.fastlocation.utils.PermissionUtils;
import cn.bingerz.android.fastlocationsample.dialog.MapChooseBLDialog;

/**
 * @author hanson
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_LOCATION_PERMISSION = 111;

    private static final String KEY_LOCATION = "key_location";

    private static final int MSG_SEND_LOCATION = 0x10;

    private MyHandler mHandler;

    private Location mCurrentLocation;

    FastLocation mFastLocation;

    private static final class MyHandler extends Handler {

        WeakReference<MainActivity> mReference;

        public MyHandler(MainActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mReference.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEND_LOCATION:
                Bundle data = msg.getData();
                showLocation((Location) data.getParcelable(KEY_LOCATION));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyLocation();

        Button getHighAccuracy = findViewById(R.id.btn_get_high_accuracy_location);
        Button getMediumAccuracy = findViewById(R.id.btn_get_medium_accuracy_location);
        Button getLowAccuracy = findViewById(R.id.btn_get_low_accuracy_location);
        Button getLastKnow = findViewById(R.id.btn_get_last_know_location);
        Button showMap = findViewById(R.id.btn_goto_map);
        Button cancel = findViewById(R.id.btn_cancel);
        mHandler = new MyHandler(this);
        mFastLocation = new FastLocation(this);

        getHighAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(LocationParams.HIGH_ACCURACY);
            }
        });

        getMediumAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(LocationParams.MEDIUM_ACCURACY);
            }
        });

        getLowAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(LocationParams.LOW_ACCURACY);
            }
        });

        getLastKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnowLocation();
            }
        });

        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseMapDialog(mCurrentLocation);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFastLocation != null) {
                    mFastLocation.removeLocationUpdates();
                }
            }
        });
    }

    private void getLocation(LocationParams params) {
        mFastLocation.getLocation(true, params, new LocationResultListener() {
            @Override
            public void onResult(Location location) {
                if (location == null) {
                    Log.e(TAG, "location is null");
                    return;
                }
                Bundle data = new Bundle();
                data.putParcelable(KEY_LOCATION, location);
                Message msg = new Message();
                msg.what = MSG_SEND_LOCATION;
                msg.setData(data);
                mHandler.sendMessage(msg);
            }
        });
    }

    private void getLastKnowLocation() {
        try {
            FastLocation fastLocation = new FastLocation(this);
            fastLocation.getLastKnowLocation(new LocationResultListener() {
                @Override
                public void onResult(Location location) {
                    showLastKnowLocation(location);
                }
            });
        } catch (SecurityException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void showLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Invalid location.");
        }
        Log.e(TAG, "showLocation: " + location);
        mCurrentLocation = location;
        TextView tvTitle = findViewById(R.id.tv_location_title);
        TextView tvLocation = findViewById(R.id.tv_location);
        TextView tvProvider = findViewById(R.id.tv_provider);
        TextView tvAccuracy = findViewById(R.id.tv_accuracy);
        TextView tvTime = findViewById(R.id.tv_time);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        String provider = location.getProvider();
        long time = location.getTime();

        tvTitle.setText("Location:");
        tvLocation.setText(String.format("Location:<%f %f>", latitude, longitude));
        tvProvider.setText(String.format("Provider:%s", provider));
        tvAccuracy.setText(String.format("Accuracy:%f", accuracy));

        DateFormat locationFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        tvTime.setText(String.format("Time:%s", locationFormat.format(time)));
    }

    private void showLastKnowLocation(Location location) {
        if (location == null) {
            Toast.makeText(this, "Location is null.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e(TAG, "showLastKnowLocation: " + location);
        mCurrentLocation = location;
        TextView tvTitle = findViewById(R.id.tv_last_know_location_title);
        TextView tvLocation = findViewById(R.id.tv_last_know_location);
        TextView tvProvider = findViewById(R.id.tv_last_know_provider);
        TextView tvAccuracy = findViewById(R.id.tv_last_know_accuracy);
        TextView tvTime = findViewById(R.id.tv_last_know_time);

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        String provider = location.getProvider();
        long time = location.getTime();

        tvTitle.setText("LastKnowLocation:");
        tvLocation.setText(String.format("Location:<%f %f>", latitude, longitude));
        tvProvider.setText(String.format("Provider:%s", provider));
        tvAccuracy.setText(String.format("Accuracy:%f", accuracy));

        DateFormat locationFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        tvTime.setText(String.format("Time:%s", locationFormat.format(time)));
    }

    private void showChooseMapDialog(Location location) {
        if (location != null) {
            Bundle bundle = new Bundle();
            bundle.putString("title", "MyLocation");
            bundle.putDouble("latitude", location.getLatitude());
            bundle.putDouble("longitude", location.getLongitude());
            MapChooseBLDialog.newInstance(bundle).show(getSupportFragmentManager());
        }
    }

    protected boolean verifyLocation() {
        if (PermissionUtils.isGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                PermissionUtils.isGranted(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return true;
        } else {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            PermissionUtils.requestPermission(this, permissions, REQUEST_LOCATION_PERMISSION);
        }
        return false;
    }

}
