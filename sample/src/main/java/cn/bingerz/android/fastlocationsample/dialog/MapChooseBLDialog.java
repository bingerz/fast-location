package cn.bingerz.android.fastlocationsample.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bingerz.android.fastlocationsample.R;
import cn.bingerz.android.geopoint.GeoPoint;
import cn.bingerz.android.geopoint.Utils.PositionUtil;


/**
 * Created by hanbing on 15/10/20.
 */
public class MapChooseBLDialog extends BottomListDialog {

    private String mTitle;
    private double mLatitude;
    private double mLongitude;

    private List<ResolveInfo> mResolveInfos = new ArrayList<>();

    public static MapChooseBLDialog newInstance(Bundle bundle) {
        MapChooseBLDialog dialogFragment = new MapChooseBLDialog();
        dialogFragment.setBundle(bundle);
        return dialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString("title");
            mLatitude = bundle.getDouble("latitude");
            mLongitude = bundle.getDouble("longitude");
        }

        Context context = getActivity();
        if (context != null) {
            Intent action = new Intent(Intent.ACTION_VIEW, buildQueryUri(mTitle, mLatitude, mLongitude));
            mResolveInfos = context.getPackageManager().queryIntentActivities(action, PackageManager.MATCH_DEFAULT_ONLY);
            if (mResolveInfos == null || mResolveInfos.isEmpty()) {
                Toast.makeText(context, R.string.map_app_no_detected, Toast.LENGTH_SHORT).show();
                dismissAllowingStateLoss();
            } else if (mResolveInfos.size() == 1) {
                gotoActivity(mResolveInfos.get(0));
                dismissAllowingStateLoss();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_content_map_choose, null);
        RecyclerView mRecyclerView = view.findViewById(R.id.rv_list);
        Context context = getActivity();
        if (context != null) {
            ResolveInfoAdapter mResolveInfoAdapter = new ResolveInfoAdapter(context);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.setAdapter(mResolveInfoAdapter);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.height = Dp2Px(context, mResolveInfos.size() * 65);
            mRecyclerView.setLayoutParams(params);
        }
        return view;
    }

    private int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 坐标在大陆地区，需要讲地球坐标转换到火星坐标显示
     * 个别地图app除外：百度地图
     */
    private double[] convert2GCJ(String packageName, double latitude, double longitude) {
        double[] gcj = new double[] {latitude, longitude};
        if (packageName == null || packageName.isEmpty()) {
            return gcj;
        }

        if (GeoPoint.insideChina(latitude, longitude) && !packageName.contains("BaiduMap")) {
            return PositionUtil.WGS84ToGCJ02(latitude, longitude);
        }
        return gcj;
    }

    private Uri buildQueryUri(String title, double latitude, double longitude) {
        //String formatNormal = "geo:%f,%f";
        String formatQuery = "geo:0,0?q=%f,%f(%s)";
        return Uri.parse(String.format(formatQuery, latitude, longitude, title));
    }

    private void gotoActivity(ResolveInfo info) {
        gotoMapActivity(info, mTitle, mLatitude, mLongitude);
    }

    private void gotoMapActivity(ResolveInfo info, String title, double latitude, double longitude) {
        if (info == null) {
            return;
        }
        String packageName = info.activityInfo.packageName;
        double[] ll = convert2GCJ(packageName, latitude, longitude);
        latitude = ll[0];
        longitude = ll[1];

        Context context = getActivity();
        if (context != null) {
            Intent action = new Intent(Intent.ACTION_VIEW, buildQueryUri(title, latitude, longitude));
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.setAction(action.getAction());
                intent.setData(action.getData());
                startActivity(intent);
            }
        }
    }

    class ResolveInfoAdapter extends RecyclerView.Adapter<ResolveInfoAdapter.ResolveInfoViewHolder> {

        private Context mContext;

        public ResolveInfoAdapter(Context context) {
            mContext = context;
        }

        @Override
        public ResolveInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_list_map, parent, false);
            return new ResolveInfoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ResolveInfoViewHolder holder, int position) {
            ResolveInfo info = mResolveInfos.get(position);
            holder.mAppIcon.setImageDrawable(info.loadIcon(mContext.getPackageManager()));
            holder.mAppName.setText(info.loadLabel(mContext.getPackageManager()));
        }

        @Override
        public int getItemCount() {
            return mResolveInfos == null ? 0 : mResolveInfos.size();
        }

        public class ResolveInfoViewHolder extends RecyclerView.ViewHolder {
            ImageView mAppIcon;
            TextView mAppName;
            ResolveInfoViewHolder(View view) {
                super(view);
                mAppIcon = (ImageView) view.findViewById(R.id.iv_map_icon);
                mAppName = (TextView) view.findViewById(R.id.tv_map_name);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoActivity(mResolveInfos.get(getAdapterPosition()));
                        dismissAllowingStateLoss();
                    }
                });
            }
        }
    }
}
