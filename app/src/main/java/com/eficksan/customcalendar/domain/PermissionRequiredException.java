package com.eficksan.customcalendar.domain;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */
public class PermissionRequiredException extends RuntimeException {

    public final String[] requiredPermissions;

    public PermissionRequiredException(String[] requiredPermissions) {
        super("Permissions required");
        this.requiredPermissions = requiredPermissions;
    }
}
