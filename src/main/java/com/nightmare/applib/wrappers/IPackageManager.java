package com.nightmare.applib.wrappers;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.IInterface;

import com.nightmare.applib.utils.L;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class IPackageManager {
    public final IInterface manager;
    private Method getPackageInfoMethod;
    private Method queryIntentActivitiesMethod;
    private Method getAllPackagesMethod;
    private Method getPermissionInfoMethod;

    public IPackageManager(IInterface manager) {
        this.manager = manager;
    }

    private Method getGetPackageInfoMethod() throws NoSuchMethodException {
        if (getPackageInfoMethod == null) {
            Class<?> cls = manager.getClass();
            try {
                getPackageInfoMethod = cls.getDeclaredMethod("getPackageInfo", String.class, long.class, int.class);
            } catch (Exception ignored) {
                getPackageInfoMethod = cls.getDeclaredMethod("getPackageInfo", String.class, int.class, int.class);
            }
        }
        return getPackageInfoMethod;
    }

    private Method getQueryIntentActivitiesMethod() throws NoSuchMethodException {
        if (queryIntentActivitiesMethod == null) {
            Class<?> cls = manager.getClass();
            try {
                queryIntentActivitiesMethod = cls.getMethod("queryIntentActivities", Intent.class, String.class, long.class, int.class);
            } catch (Exception ignored) {
                queryIntentActivitiesMethod = cls.getMethod("queryIntentActivities", Intent.class, String.class, int.class, int.class);
            }
        }
        return queryIntentActivitiesMethod;
    }

    private Method getGetAllPackagesMethod() throws NoSuchMethodException {
        if (getAllPackagesMethod == null) {
            Class<?> cls = manager.getClass();
            getAllPackagesMethod = cls.getMethod("getAllPackages");
        }
        return getAllPackagesMethod;
    }

    private Method getGetPermissionInfo() throws NoSuchMethodException {
        if (getPermissionInfoMethod == null) {
            Class<?> cls = manager.getClass();
            getPermissionInfoMethod = cls.getMethod("getPermissionInfo", String.class, int.class);
        }
        return getPermissionInfoMethod;
    }

    public PackageInfo getPackageInfo(String packageName, int flag, int userId) {
        try {
            return (PackageInfo) Objects.requireNonNull(getGetPackageInfoMethod()).invoke(manager, packageName, flag, userId);
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        try {
            Object object = Objects.requireNonNull(getQueryIntentActivitiesMethod()).invoke(manager, intent, resolvedType, flags, userId);
            try {
                return (List<ResolveInfo>) object;
            } catch (ClassCastException ignored) {
                return (List<ResolveInfo>) object.getClass().getMethod("getList").invoke(object);
            }
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllPackages() {
        try {
            return (List<String>) Objects.requireNonNull(getGetAllPackagesMethod()).invoke(manager);
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    public PermissionInfo getPermissionInfo(String packageName, int flag) {
        try {
            return (PermissionInfo) Objects.requireNonNull(getGetPermissionInfo()).invoke(this.manager, packageName, flag);
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }
}
