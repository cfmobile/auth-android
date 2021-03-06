package io.pivotal.android.auth;

import android.content.Context;

/* package */ class AuthClientHolder {

    private static final Object LOCK = new Object();
    private static AuthClient sClient;

    public static void init(final AuthClient client) {
        sClient = client;
    }

    public static AuthClient get(final Context context) {
        if (sClient == null) {
            synchronized (LOCK) {
                sClient = new AuthClient.Default(context);
            }
        }
        return sClient;
    }
}