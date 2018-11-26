package com.rvsoft.safty;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HeterogeneousExpandableList;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.rvsoft.safty.adapter.ViewPagerItemAdapter;
import com.rvsoft.safty.helper.Constant;
import com.rvsoft.safty.helper.Helper;
import com.rvsoft.safty.helper.PermissionHelper;
import com.rvsoft.safty.interfaces.OnPermissionListener;
import com.rvsoft.safty.smsverifycatcher.OnSmsCatchListener;
import com.rvsoft.safty.smsverifycatcher.SmsVerifyCatcher;
import com.rvsoft.safty.views.PinEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    private String TAG = LoginActivity.class.getSimpleName();


    @BindView(R.id.login_view_pager)
    ViewPager viewPager;
    @BindView(R.id.edit_mobile)
    EditText editMobile;
    @BindView(R.id.txt_mobile)
    TextView txtMobile;
    @BindView(R.id.edit_otp)
    PinEditText editOTP;


    private ViewPagerItemAdapter adapter;
    private FirebaseAuth mAuth;
    private Activity mActivity;
    private SmsVerifyCatcher smsVerifyCatcher;
    private RequestQueue requestQueue;
    private PermissionHelper permission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mActivity = this;
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(mActivity);
        permission = new PermissionHelper(mActivity);

        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener() {
            @Override
            public void onSmsCatch(String code) {
                //showProgress(true);
                editOTP.setText(code);
                verifyMobile();
            }
        });
        smsVerifyCatcher.setPatseExpression("[^0-9]");
        smsVerifyCatcher.setPhoneNumberFilter(Constant.SMS_SENDER);

        initViewPager();
        askForPermission();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(mActivity,HomeActivity.class));
            finish();
        }
    }

    private void askForPermission() {
        permission.checkAndAskPermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS}, Constant.REQUESTS.ALL, new OnPermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied() {

            }
        });
    }

    private void initViewPager() {
        adapter = new ViewPagerItemAdapter(mActivity);
        adapter.addView(R.id.step_one,"Mobile Page");
        adapter.addView(R.id.step_two,"Sms Verification");
        adapter.addView(R.id.step_three,"User Details");
        viewPager.setAdapter(adapter);
    }

    @OnClick(R.id.btn_step_one)
    void login() {
        if (TextUtils.isEmpty(editMobile.getText().toString())) {
            editMobile.setError("Please Enter Valid Mobile No.");
            editMobile.requestFocus();
            return;
        }

        /*Ion.with(this)
                .load("http://rvsoft.esy.es/Android/helpo/firebase.php")
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            customAuth(result);
                        }
                    }
                });*/

        StringRequest request = new StringRequest(Request.Method.POST, Constant.API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.optBoolean("success", false)) {
                        txtMobile.setText("+91-"+editMobile.getText().toString());
                        viewPager.setCurrentItem(1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> value = new HashMap<>();
                value.put("tag","user");
                value.put("mobile",editMobile.getText().toString());
                return value;
            }
        };
        requestQueue.add(request);
    }

    @OnClick(R.id.btn_step_two)
    void verifyMobile() {
        if (TextUtils.isEmpty(editOTP.getText().toString())) {
            editOTP.setError("Please Enter OTP");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.optBoolean("success", false)) {
                        if (!obj.isNull("response")) {
                            String token = obj.getJSONObject("response").getString("fcm_token");
                            customAuth(token);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<>();
                param.put("mobile",editMobile.getText().toString());
                param.put("otp",editOTP.getText().toString());
                param.put("imei",Helper.getIMEI(mActivity));
                param.put("fcm",FirebaseInstanceId.getInstance().getToken());
                param.put("tag","verify_user");
                return param;
            }
        };
        requestQueue.add(stringRequest);
    }

    @OnClick(R.id.btn_step_three)
    void submit() {

    }



    private void customAuth(String result) {
        mAuth.signInWithCustomToken(result).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCustomToken:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Log.e(TAG,user.getUid());
                        startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                        finish();
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode,permissions,grantResults);
        permission.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }
}
