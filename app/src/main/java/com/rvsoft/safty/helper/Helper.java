package com.rvsoft.safty.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;

import com.rvsoft.safty.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.view.View.MeasureSpec.UNSPECIFIED;

public class Helper {
    public static Bitmap getPoliceStationMarker(Activity activity) {
        View view = LayoutInflater.from(activity).inflate(R.layout.marker_police_station, null);
        view.measure(UNSPECIFIED, UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return bitmap;
    }

    public static String getCurrentDateTime() {
        Calendar now = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        return dateFormat.format(now.getTime());
    }

    public static String getAuthorisation() {
        return "key=AIzaSyA68QZx4ILHk8WQVFObVHlEwnObYi7aq28";
    }

    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        return telephonyManager.getDeviceId();
    }
}
