package cn.bingerz.android.fastlocation.location;

import android.content.Context;

/**
 * @author hanson
 */
public interface LocationProvider {

    void init(Context context);

    void request(LocationCallbackListener listener, LocationParams params);

    void requestSingle(LocationCallbackListener listener, LocationParams params);

    void remove();

    void getLastLocation(LocationCallbackListener listener);
}
