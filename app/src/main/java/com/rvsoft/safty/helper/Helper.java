package com.rvsoft.safty.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import com.rvsoft.safty.R;

import static android.view.View.MeasureSpec.UNSPECIFIED;

public class Helper {
    public static Bitmap getPoliceStationMarker(Activity activity){
        View view = LayoutInflater.from(activity).inflate(R.layout.marker_police_station,null);
        view.measure(UNSPECIFIED,UNSPECIFIED);
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = view.getBackground();
        if (drawable!=null)
            drawable.draw(canvas);
        view.draw(canvas);
        return bitmap;
    }
}
