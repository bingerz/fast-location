package cn.bingerz.android.fastlocation.location;

import android.content.Context;

/**
 * @author hanson
 */
public interface LocationProvider {

    void init(Context context);

    void request(LocationParams params, LocationCallbackListener listener);

    void requestSingle(LocationParams params, LocationCallbackListener listener);

    void remove();

    void getLastLocation(LocationCallbackListener listener);
}
