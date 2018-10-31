package com.rvsoft.safty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rvsoft.safty.app.App;
import com.rvsoft.safty.app.BaseActivity;
import com.rvsoft.safty.geofire.GeoFire;
import com.rvsoft.safty.geofire.GeoLocation;
import com.rvsoft.safty.geofire.GeoQuery;
import com.rvsoft.safty.geofire.GeoQueryDataEventListener;
import com.rvsoft.safty.helper.Constant;
import com.rvsoft.safty.helper.Helper;
import com.rvsoft.safty.helper.PermissionHelper;
import com.rvsoft.safty.interfaces.OnPermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rvsoft.safty.helper.Constant.REQUESTS.ALL;
import static com.rvsoft.safty.helper.Constant.REQUESTS.ENABLE_GPS;
import static com.rvsoft.safty.helper.Constant.REQUESTS.LOCATION;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback{

    private static final long DURATION_TRANSITION_MS = 300;
    private PermissionHelper permission;
    private Activity mActivity;
    private GoogleMap map;
    private Location location;
    private Window window;
    private RequestQueue requestQueue;

    @BindView(R.id.upper_content)
    View upperContent;
    @BindView(R.id.bottom_content)
    View bottomContent;
    @BindView(R.id.nearby_police)
    ImageButton nearPolice;
    @BindView(R.id.locationUpdate)
    TextView helpo;


    private Interpolator contentInInterpolator;
    private Interpolator contentOutInterpolator;
    private boolean isOpen = false;

    private DatabaseReference ref;
    private GeoFire geoFire;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().hide();
            window = getWindow();
        }

        mActivity = this;
        permission = new PermissionHelper(mActivity);
        requestQueue = Volley.newRequestQueue(mActivity);
        FirebaseApp.initializeApp(this);
        ref = FirebaseDatabase.getInstance().getReference("/location");
        geoFire = new GeoFire(ref);

        nearPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeContentVisibility();
            }
        });

        helpo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNearbyPolice();
            }
        });
        //askForPermission();
        askForGPSPermission();



        //initMap();
    }



    private void askForGPSPermission() {
        try {
            permission.checkAndAskPermission(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION, new OnPermissionListener() {
                @Override
                public void onPermissionGranted() {
                    initMap();
                }

                @Override
                public void onPermissionDenied() {
                    new FancyAlertDialog.Builder(mActivity)
                            .setimageResource(R.drawable.ic_gps)
                            .setTextSubTitle("\"HelpO\" Would like to Use Your Location!")
                            .setBody("HelpO finds helps for you exact nearby you\nChoose Allow so the app can find your location")
                            .setPositiveButtonText("Grant Access")
                            .setNegativeButtonText("Cancel")
                            .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog) {
                                    askForGPSPermission();
                                }
                            })
                            .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog) {
                                    finish();
                                }
                            }).build().show();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initMap() {
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mapFragment==null){
            mapFragment = new SupportMapFragment();
            fm.beginTransaction().replace(R.id.map_container,mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    private void askForPermission() {
        try {
            String[] permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CALL_PHONE
            };
            permission.checkAndAskPermission(permissions, ALL, new OnPermissionListener() {
                @Override
                public void onPermissionGranted() {

                }

                @Override
                public void onPermissionDenied() {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void changeContentVisibility(){
        int targetTranslation = 0;
        int contTranslation = 0;
        Interpolator interpolator = null;

        if (!isOpen){
            targetTranslation = bottomContent.getHeight();
            contTranslation = upperContent.getHeight();
            interpolator = contentOutInterpolator;
            isOpen = true;

            if (map!=null){
                map.animateCamera(CameraUpdateFactory.zoomTo(13));
            }

        }else {
            targetTranslation = 0;
            contTranslation = 0;
            interpolator = contentInInterpolator;
            isOpen = false;
            if (map!=null){
                map.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        }
        if (getSupportActionBar()!=null){
            if (isOpen)
                getSupportActionBar().show();
            else
                getSupportActionBar().hide();
        }
        bottomContent.animate().cancel();
        bottomContent.animate()
                .translationY(targetTranslation)
                .setInterpolator(interpolator)
                .setDuration(DURATION_TRANSITION_MS)
                .start();
        upperContent.animate().cancel();
        upperContent.animate()
                .translationY(-(contTranslation))
                .setInterpolator(interpolator)
                .setDuration(DURATION_TRANSITION_MS)
                .start();
    }

    @SuppressLint("MissingPermission")
    private void getNearbyPolice() {
        Awareness.getSnapshotClient(mActivity).getLocation().addOnCompleteListener(new OnCompleteListener<LocationResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationResponse> task) {
                if (task.isSuccessful()){
                    location = task.getResult().getLocation();
                    final GeoLocation geoLocation = new GeoLocation(location.getLatitude(),location.getLongitude());
                    final GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation,0.6);
                    GeoQueryDataEventListener listner = new GeoQueryDataEventListener() {
                        @Override
                        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                        }

                        @Override
                        public void onDataExited(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                        }

                        @Override
                        public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    };
                    geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                        @Override
                        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                            Toast.makeText(mActivity, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                            Log.d("TAG",dataSnapshot.getKey());
                            geoQuery.removeAllListeners();
                        }

                        @Override
                        public void onDataExited(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                        }

                        @Override
                        public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id){
            case R.id.action_settings:
                break;
            case android.R.id.home:
                changeContentVisibility();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.google_light_map));
        map.setMyLocationEnabled(true);
        detectLocation();

    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Awareness.getSnapshotClient(mActivity).getLocation().addOnCompleteListener(new OnCompleteListener<LocationResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationResponse> task) {
                if (task.isSuccessful()){
                    location = task.getResult().getLocation();
                    if (location!=null){
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),11));
                        getNearbyPoliceStations();
                    }
                }
            }
        });
    }

    private void getNearbyPoliceStations() {
        String url = MessageFormat.format(Constant.POLICE,location.getLatitude(),location.getLongitude());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("RES",response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.optString("status","").equalsIgnoreCase("OK")){
                        JSONArray array = obj.getJSONArray("results");
                        for (int i=0;i<array.length();i++){
                            JSONObject result = array.getJSONObject(i);
                            JSONObject geo = result.getJSONObject("geometry");
                            JSONObject loc = geo.getJSONObject("location");
                            String name = result.optString("name","");
                            String address = result.optString("vicinity","");
                            double lat = loc.getDouble("lat");
                            double lng = loc.getDouble("lng");
                            if (map!=null){
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat,lng))
                                        .anchor(0.5f, 0.5f)
                                        .title(name)
                                        .snippet(address)
                                        .icon(BitmapDescriptorFactory.fromBitmap(Helper.getPoliceStationMarker(mActivity)))
                                );
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission.onRequestPermissionsResult(requestCode,permissions,grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
