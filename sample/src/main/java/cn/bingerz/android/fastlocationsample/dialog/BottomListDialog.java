package cn.bingerz.android.fastlocationsample.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import cn.bingerz.android.fastlocationsample.R;

/**
 * @author hanson
 */
public class BottomListDialog extends DialogFragmentCompat {

    public static final String TAG = "bottom_sheet_dialog";

    public BottomListTypeListener mBottomListTypeListener;

    public BottomListDismissListener mBottomListDismissListener;

    public BottomListDialog() {
        setStyle(STYLE_NO_FRAME, R.style.MyDialogTheme);
    }

    public BottomListDialog setBundle(Bundle bundle) {
        setArguments(bundle);
        return this;
    }

    public BottomListDialog setOnTypeSelectedListener(BottomListTypeListener listener) {
        this.mBottomListTypeListener = listener;
        return this;
    }

    public BottomListDialog setOnDismissListener(BottomListDismissListener listener) {
        this.mBottomListDismissListener = listener;
        return this;
    }

    public void show(FragmentManager manager) {
        show(manager, TAG);
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogWindowLayout();
    }

    private void setDialogWindowLayout() {
        Window window = getDialogWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.MyDialogAnimation);
            WindowManager.LayoutParams wl = window.getAttributes();
            wl.gravity = Gravity.BOTTOM;
        }
        return dialog;
    }

    public interface BottomListTypeListener {
        void onTypeResult(String type, Bundle data);
    }

    public interface BottomListDismissListener {
        void onDismiss();
    }
}
