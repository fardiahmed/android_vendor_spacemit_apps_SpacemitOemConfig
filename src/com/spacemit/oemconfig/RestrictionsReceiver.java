/*
 * Copyright (C) 2026 The Android Open Source Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.spacemit.oemconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Fires when the EMM changes this app's managed configuration, and on boot.
 * It hands off to {@link OemConfigApplyService} because applying needs an
 * asynchronous bind to the policy service.
 */
public final class RestrictionsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, OemConfigApplyService.class));
    }
}
