/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.auth;

import android.accounts.AccountAuthenticatorActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/* package */ class PackageHelper {

    public static Class<?> getLoginActivityClass(final Context context) {
        try {
            final Class<?> klass = findLoginActivityClass(context);
            if (klass != null) return klass;
        } catch (final Exception e) {
            Logger.ex(e);
        }

        throw new IllegalStateException("No subclass of AccountAuthenticatorActivity found in your AndroidManifest.xml");
    }

    private static Class<?> findLoginActivityClass(final Context context) throws Exception {
        final PackageManager manager = context.getPackageManager();
        final PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        final ActivityInfo[] activities = info.activities;
        return findLoginActivityClass(activities);
    }

    private static Class<?> findLoginActivityClass(final ActivityInfo[] activities) throws Exception {
        if (activities != null) {
            for (int i =0; i < activities.length; i++) {
                final Class<?> klass = Class.forName(activities[i].name);
                if (AccountAuthenticatorActivity.class.isAssignableFrom(klass)) {
                    return klass;
                }
            }
        }
        return null;
    }

    // TODO

//    private static Class<?> findLoginActivityClass(final ActivityInfo[] activities) throws Exception {
//        if (activities != null) {
//            final Set<Class<?>> klasses = new HashSet<Class<?>>();
//            for (int i =0; i < activities.length; i++) {
//                final Class<?> klass = Class.forName(activities[i].name);
//                if (AccountAuthenticatorActivity.class.isAssignableFrom(klass)) {
//                    klasses.add(klass);
//                }
//            }
//            if (klasses.size() > 1) {
//                klasses.remove(LoginPasswordActivity.class);
//                klasses.remove(LoginAuthCodeActivity.class);
//            }
//        }
//        return null;
//    }
}
