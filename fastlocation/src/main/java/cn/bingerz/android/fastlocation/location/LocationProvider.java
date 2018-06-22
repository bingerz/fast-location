package cn.bingerz.android.fastlocation.location;

import android.content.Context;

public interface LocationProvider {

    void init(Context context);

    void request(LocationCallbackListener listener, LocationParams params);

    void remove();

    void getLastLocation(LocationCallbackListener listener);
}
