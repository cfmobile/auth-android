/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.auth;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.PasswordTokenRequest;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface AuthorizationProvider {

    public PasswordTokenRequest newPasswordTokenRequest(String username, String password);

    public RefreshTokenRequest newRefreshTokenRequest(String refreshToken);

    public AuthorizationCodeTokenRequest newTokenRequest(String authorizationCode);

    public AuthorizationCodeRequestUrl newAuthorizationUrl();

    public static class Default extends AuthorizationCodeFlow implements AuthorizationProvider {

        private static final List<String> SCOPES = Arrays.asList("offline_access", "openid");
        private static final GenericUrl TOKEN_URL = new GenericUrl(Pivotal.getTokenUrl());

        private static final Credential.AccessMethod METHOD = BearerToken.authorizationHeaderAccessMethod();
        private static final HttpExecuteInterceptor INTERCEPTOR = new ClientParametersAuthentication(Pivotal.getClientId(), Pivotal.getClientSecret());

        private static final HttpTransport TRANSPORT = new NetHttpTransport();
        private static final JsonFactory JSON_FACTORY = new JacksonFactory();

        public Default() {
            super(new Builder(METHOD, TRANSPORT, JSON_FACTORY, TOKEN_URL, INTERCEPTOR, Pivotal.getClientId(), Pivotal.getAuthorizeUrl()).setScopes(SCOPES));
        }

        @Override
        public PasswordTokenRequest newPasswordTokenRequest(final String username, final String password) {
            final GenericUrl url = new GenericUrl(getTokenServerEncodedUrl());
            final PasswordTokenRequest request = new PasswordTokenRequest(getTransport(), getJsonFactory(), url, username, password);
            request.setClientAuthentication(getClientAuthentication());
            request.setRequestInitializer(getRequestInitializer());
            request.setScopes(getScopes());
            return request;
        }

        @Override
        public RefreshTokenRequest newRefreshTokenRequest(final String refreshToken) {
            final GenericUrl url = new GenericUrl(getTokenServerEncodedUrl());
            final RefreshTokenRequest request = new RefreshTokenRequest(getTransport(), getJsonFactory(), url, refreshToken);
            request.setClientAuthentication(getClientAuthentication());
            request.setRequestInitializer(getRequestInitializer());
            request.setScopes(getScopes());
            return request;
        }

        @Override
        public AuthorizationCodeTokenRequest newTokenRequest(final String authorizationCode) {
            final AuthorizationCodeTokenRequest request = super.newTokenRequest(authorizationCode);
            request.setRedirectUri(Pivotal.getRedirectUrl());
            return request;
        }

        @Override
        public AuthorizationCodeRequestUrl newAuthorizationUrl() {
            final AuthorizationCodeRequestUrl requestUrl = super.newAuthorizationUrl();
            requestUrl.setRedirectUri(Pivotal.getRedirectUrl());
            requestUrl.setState(UUID.randomUUID().toString());
            return requestUrl;
        }
    }
}
