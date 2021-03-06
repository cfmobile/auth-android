/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.auth;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.test.AndroidTestCase;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;

import org.mockito.Mockito;

import java.util.UUID;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AuthCodeTokenLoaderTest extends AndroidTestCase {

    private static final String AUTH_CODE = UUID.randomUUID().toString();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        RemoteAuthenticatorHolder.init(null);
    }

    public void testLoadInBackgroundSucceedsWithTokenResponse() throws Exception {
        final Context context = Mockito.mock(Context.class);
        final TokenResponse response = Mockito.mock(TokenResponse.class);
        final RemoteAuthenticator authenticator = Mockito.mock(RemoteAuthenticator.class);
        final AuthorizationCodeTokenRequest request = Mockito.mock(AuthorizationCodeTokenRequest.class);
        final AuthCodeTokenLoader loader = new AuthCodeTokenLoader(context, AUTH_CODE);

        RemoteAuthenticatorHolder.init(authenticator);

        Mockito.when(authenticator.newAuthorizationCodeTokenRequest(AUTH_CODE)).thenReturn(request);
        Mockito.when(request.execute()).thenReturn(response);

        assertEquals(response, loader.loadInBackground());

        Mockito.verify(authenticator).newAuthorizationCodeTokenRequest(AUTH_CODE);
        Mockito.verify(request).execute();
    }

    public void testLoadInBackgroundFailsWithErrorResponse() throws Exception {
        final Context context = Mockito.mock(Context.class);
        final RemoteAuthenticator authenticator = Mockito.mock(RemoteAuthenticator.class);
        final AuthorizationCodeTokenRequest request = Mockito.mock(AuthorizationCodeTokenRequest.class);
        final AuthCodeTokenLoader loader = new AuthCodeTokenLoader(context, AUTH_CODE);

        RemoteAuthenticatorHolder.init(authenticator);

        Mockito.when(authenticator.newAuthorizationCodeTokenRequest(AUTH_CODE)).thenReturn(request);
        Mockito.doThrow(new RuntimeException()).when(request).execute();

        final TokenResponse response = loader.loadInBackground();

        assertTrue(response instanceof TokenLoader.ErrorResponse);

        Mockito.verify(authenticator).newAuthorizationCodeTokenRequest(AUTH_CODE);
        Mockito.verify(request).execute();
    }
}
