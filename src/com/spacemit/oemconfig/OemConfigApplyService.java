/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.spacemit.oemconfig;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.spacemit.policy.PolicyKeys;
import com.spacemit.policy.SpacemitPolicy;
import com.spacemit.policy.SpacemitPolicyManager;

/**
 * Reads this app's managed configuration and maps each key onto a policy call.
 * Binds the policy service, applies once connected, then unbinds and stops.
 */
public final class OemConfigApplyService extends Service {

    private static final String TAG = "SpacemitOemConfig";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final RestrictionsManager rm = getSystemService(RestrictionsManager.class);
        final Bundle config = rm != null ? rm.getApplicationRestrictions() : new Bundle();
        final SpacemitPolicyManager pm = SpacemitPolicyManager.create(this);

        boolean bound = pm.bind(new SpacemitPolicyManager.ConnectionListener() {
            @Override public void onConnected() {
                try {
                    apply(pm, config);
                } finally {
                    pm.unbind();
                    stopSelf(startId);
                }
            }
            @Override public void onDisconnected() { }
        });

        if (!bound) {
            Log.e(TAG, "Could not bind the SpacemiT policy service");
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }

    private static void apply(SpacemitPolicyManager pm, Bundle c) {
        pm.setVendorProperty(PolicyKeys.KIOSK_MODE, bit(c.getBoolean("kiosk_mode", false)));
        pm.setVendorProperty(PolicyKeys.FAKE_CAMERA, bit(c.getBoolean("fake_camera", false)));
        // ADB is a framework global setting, not a vendor property.
        pm.setDeviceSetting(SpacemitPolicy.NS_GLOBAL, PolicyKeys.ADB_ENABLED,
                bit(c.getBoolean("adb_enabled", true)));
    }

    private static String bit(boolean b) {
        return b ? "1" : "0";
    }
}
