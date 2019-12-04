package cn.bingerz.android.fastlocationsample.dialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Window;

import androidx.fragment.app.DialogFragment;

/**
 * Created by hanson on 2018/3/21.
 */

public class DialogFragmentCompat extends DialogFragment {

    /**
     * onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is deprecated
     * Use onAttachToContext instead
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    /**
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /**
     * Called when the fragment attaches to the context
     */
    protected void onAttachToContext(Context context) {
    }

    protected Window getDialogWindow() {
        if (getDialog() != null) {
            return getDialog().getWindow();
        }
        return null;
    }
}
