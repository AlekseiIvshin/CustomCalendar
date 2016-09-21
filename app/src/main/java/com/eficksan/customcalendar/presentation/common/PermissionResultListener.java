package com.eficksan.customcalendar.presentation.common;

import android.support.annotation.NonNull;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public interface PermissionResultListener {

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
