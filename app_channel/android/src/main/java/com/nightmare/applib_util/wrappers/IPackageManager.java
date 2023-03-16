package com.nightmare.applib_util.wrappers;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class IPackageManager {
    public final IInterface manager;
    private Method getPackageInfoMethod;
    private Method getInstalledPackagesMethod;
    private Method getPermissionInfo;

    public IPackageManager(IInterface manager) {
        this.manager = manager;
    }

    private Method getGetPackageInfoMethod() throws NoSuchMethodException {
        if (getPackageInfoMethod == null) {
            Class<?> cls = manager.getClass();
            getPackageInfoMethod = cls.getMethod("getPackageInfo", String.class, int.class, int.class);
        }
        return getPackageInfoMethod;
    }

    private Method getGetInstalledPackagesMethod() throws NoSuchMethodException {
        if (getInstalledPackagesMethod == null) {
            Class<?> cls = manager.getClass();
            getInstalledPackagesMethod = cls.getMethod("getAllPackages");
        }
        return getInstalledPackagesMethod;
    }

    private Method getGetPermissionInfo() throws NoSuchMethodException {
        if (getInstalledPackagesMethod == null) {
            Class<?> cls = manager.getClass();
            getInstalledPackagesMethod = cls.getMethod("getPermissionInfo");
        }
        return getInstalledPackagesMethod;
    }

    public PackageInfo getPackageInfo(String packageName, int flag) throws InvocationTargetException, IllegalAccessException {
        try {
            return (PackageInfo) getGetPackageInfoMethod().invoke(this.manager, new Object[]{packageName, flag, 0});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PermissionInfo getPermissionInfo(String packageName, int flag) throws InvocationTargetException, IllegalAccessException {
        try {
            return (PermissionInfo) getGetPermissionInfo().invoke(this.manager, new Object[]{packageName, flag});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Method getQueryIntentActivities() throws NoSuchMethodException {
        if (getInstalledPackagesMethod == null) {
            Class<?> cls = manager.getClass();
            getInstalledPackagesMethod = cls.getMethod("queryIntentActivities");
        }
        return getInstalledPackagesMethod;
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent,
                                            String resolvedType, int flags, int userId) {
        try {
            return (List<ResolveInfo>) getQueryIntentActivities().invoke(this.manager, new Object[]{intent, resolvedType, flags, userId});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getInstalledPackages(int flag) {
        try {
            return (List<String>) getGetInstalledPackagesMethod().invoke(this.manager, new Object[]{});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
