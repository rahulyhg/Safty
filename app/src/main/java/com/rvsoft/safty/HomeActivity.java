package com.rvsoft.safty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.robinhood.ticker.TickerView;
import com.rvsoft.safty.app.BaseActivity;
import com.rvsoft.safty.geofire.GeoFire;
import com.rvsoft.safty.geofire.GeoLocation;
import com.rvsoft.safty.geofire.GeoQuery;
import com.rvsoft.safty.geofire.GeoQueryDataEventListener;
import com.rvsoft.safty.geofire.GeoQueryEventListener;
import com.rvsoft.safty.helper.Constant;
import com.rvsoft.safty.helper.Helper;
import com.rvsoft.safty.helper.PermissionHelper;
import com.rvsoft.safty.interfaces.OnPermissionListener;
import com.rvsoft.safty.model.Helpo;
import com.rvsoft.safty.model.PoliceRequest;
import com.rvsoft.safty.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.rvsoft.safty.helper.Constant.REQUESTS.ALL;
import static com.rvsoft.safty.helper.Constant.REQUESTS.LOCATION;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback{
    private static final String TAG = HomeActivity.class.getSimpleName();
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

    @BindView(R.id.no_of_police)
    TickerView noOfPolice;

    private Interpolator contentInInterpolator;
    private Interpolator contentOutInterpolator;
    private boolean isOpen = false;

    private DatabaseReference ref;
    private GeoFire geoFire;
    private int policeCount = 0;
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


        noOfPolice.setText(String.valueOf(policeCount));


        nearPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeContentVisibility();
            }
        });

        helpo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //detectLocation();
                notifyNearbyPolice();
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

                    geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                        @Override
                        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                            Toast.makeText(mActivity, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                            Log.d("TAG",dataSnapshot.getKey());
                            final String uuid = dataSnapshot.getKey();
                            policeCount++;
                            noOfPolice.setText(String.valueOf(policeCount));



                        }

                        @Override
                        public void onDataExited(DataSnapshot dataSnapshot) {
                            policeCount--;
                            noOfPolice.setText(String.valueOf(policeCount));
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

    @SuppressLint("MissingPermission")
    private void notifyNearbyPolice() {
        Awareness.getSnapshotClient(this).getLocation().addOnCompleteListener(new OnCompleteListener<LocationResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationResponse> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    location = task.getResult().getLocation();
                    if (location != null) {
                        final GeoLocation geoLocation = new GeoLocation(location.getLatitude(),location.getLongitude());
                        final GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation,0.6);
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, final GeoLocation location) {
                                Log.e(TAG,"Key Entered :"+key);
                                geoQuery.removeAllListeners();
                                if (key != null || !TextUtils.isEmpty(key)) {
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.FIREBASE_CHILD.USER);
                                    reference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);
                                            if (user != null) {
                                                Log.d(TAG, user.getUserMobile());
                                                //sentNotificationToPolice(user);
                                                addRequest(user,location);
                                            } else {
                                                Log.e(TAG,"user null");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onKeyExited(String key) {
                                Log.e(TAG,"Key Exited :"+key);
                                geoQuery.removeAllListeners();
                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {
                                Log.e(TAG,"Key Moved :"+key);
                                geoQuery.removeAllListeners();
                            }

                            @Override
                            public void onGeoQueryReady() {

                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                Log.e(TAG,"Query Error :"+error);
                            }
                        });
                    }
                }
            }
        });
    }

    private void addRequest(final User user, GeoLocation location) {
        try {

            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                final Helpo helpo = new Helpo();
                helpo.setAccepted(false);
                helpo.setUserName("Ravikant");
                helpo.setUserID("");
                helpo.setFcm(FirebaseInstanceId.getInstance().getToken());
                helpo.setLatitude(location.latitude);
                helpo.setLongitude(location.longitude);

                PoliceRequest request = new PoliceRequest();
                request.setRequestID(firebaseUser.getUid());
                request.setRequestOn("");

                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child(Constant.FIREBASE_CHILD.HELPO_REQUEST).child(user.getUserID()).child(firebaseUser.getUid()).setValue(request, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.e(TAG,"PoliceRequest Data could not be saved " + databaseError.getMessage());
                        } else {
                            reference.child(Constant.FIREBASE_CHILD.HELP_DETAIL).child(firebaseUser.getUid()).setValue(helpo, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.e(TAG,"Help Data could not be saved " + databaseError.getMessage());
                                    }else {
                                        sentNotificationToPolice(user);
                                    }
                                }
                            });
                        }
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sentNotificationToPolice(User user) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("to",user.getFcm());
            json.addProperty("collapse_key","type_a");
            JSONObject notification = new JSONObject();
            notification.put("body","Android Body");
            notification.put("title","Android Title");
            JsonElement element = new Gson().fromJson(notification.toString(),JsonElement.class);
            json.add("notification",element);

            Ion.with(mActivity)
                    .load("https://fcm.googleapis.com/fcm/send")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", Helper.getAuthorisation())
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                e.printStackTrace();
                            } else {
                                Log.d("TAG",result.toString());
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        getNearbyPolice();
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        changeContentVisibility();
    }
}
