package com.eficksan.customcalendar.presentation.common;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public interface PermissionsRequestListener {

    void onPermissionsRequired(String[] permissions, int requestCode);

    void addListener(PermissionResultListener resultListener);

    void removeListener(PermissionResultListener resultListener);
}
