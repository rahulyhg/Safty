package com.rvsoft.safty.interfaces;
/**
 * Created by RVishwakarma on 7/26/2018.
 */
public interface OnPermissionListener {
    /**
     * Callbase Permission Granted
     */
    void onPermissionGranted();

    /**
     * Callback Permission Denied
     */
    void onPermissionDenied();
}
