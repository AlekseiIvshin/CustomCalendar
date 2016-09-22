package com.eficksan.customcalendar.presentation.common;

import android.support.annotation.NonNull;

import java.util.LinkedList;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */

public class PermissionsRequestListenerDelegate {

    private LinkedList<PermissionResultListener> mPermissionResultListeners;

    public PermissionsRequestListenerDelegate() {
        mPermissionResultListeners = new LinkedList<>();
    }

    public void addListener(PermissionResultListener resultListener) {
        mPermissionResultListeners.add(resultListener);
    }

    public void removeListener(PermissionResultListener resultListener) {
        mPermissionResultListeners.remove(resultListener);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (PermissionResultListener resultListener : mPermissionResultListeners) {
            resultListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
