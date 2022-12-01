package com.huawei.hmsmapkitandlocation.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hmsmapkitandlocation.R;

public class CustomInfoWindowAdapter implements HuaweiMap.InfoWindowAdapter {

    private View mWindow;
    private String strTitle;
    private String strSnippet;

    public CustomInfoWindowAdapter(Context context, String strTitle, String strSnippet) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
        this.strTitle = strTitle;
        this.strSnippet = strSnippet;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView tvTitle = mWindow.findViewById(R.id.txtv_titlee);
        TextView tvSnippet = mWindow.findViewById(R.id.txtv_snippett);

        tvTitle.setText(this.strTitle);
        tvSnippet.setText(this.strSnippet);

        return mWindow;
    }
}
