/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;

public class Authenticator extends AbstractAccountAuthenticator {

	private final Context mContext;

	public Authenticator(final Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType, final String authTokenType, final String[] requiredFeatures, final Bundle options) throws NetworkErrorException {
		return newAccountBundle(mContext, response);
	}

	@Override
	public Bundle confirmCredentials(final AccountAuthenticatorResponse response, final Account account, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
	}

	@Override
	public Bundle editProperties(final AccountAuthenticatorResponse response, final String accountType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account, final String authTokenType, final Bundle options) throws NetworkErrorException {
		return newAuthTokenBundle(mContext, response, account, authTokenType);
	}

	@Override
	public String getAuthTokenLabel(final String authTokenType) {
        return authTokenType;
	}

	@Override
	public Bundle hasFeatures(final AccountAuthenticatorResponse response, final Account account, final String[] features) throws NetworkErrorException {
		return newResultBundle(false);
	}

	@Override
	public Bundle updateCredentials(final AccountAuthenticatorResponse response, final Account account, final String authTokenType, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
	}
	
	
	// =============================================
	
	
	private static Bundle newAccountBundle(final Context context, final AccountAuthenticatorResponse response) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, newLoginIntent(context, response));
		return bundle;
	}

    private static Intent newLoginIntent(final Context context, final AccountAuthenticatorResponse response) {
        final Intent intent = new Intent(context, Helper.getLoginActivityClass(context));
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    private static Bundle newAuthTokenBundle(final Context context, final AccountAuthenticatorResponse response, final Account account, final String authTokenType) throws NetworkErrorException {
		final AccountManager manager = AccountManager.get(context);
        final Token.Existing existingToken = new Token.Existing(manager, account);

        final String accessToken = existingToken.getAccessToken();

        Logger.v("newAuthTokenBundle accessToken: " + accessToken);

		if (!TextUtils.isEmpty(accessToken) && !existingToken.isExpired()) {
			return newAuthTokenBundle(account, accessToken);
		}

        Logger.v("newAuthTokenBundle auth token " + (TextUtils.isEmpty(accessToken) ? "empty." : "expired."));

        final String refreshToken = existingToken.getRefreshToken();

        Logger.v("newAuthTokenBundle refreshToken: " + refreshToken);

        if (!TextUtils.isEmpty(refreshToken)) {
            try {
                final AuthorizationProvider provider = new AuthorizationProvider();
                final RefreshTokenRequest request = provider.newRefreshTokenRequest(refreshToken);
                final TokenResponse resp = request.execute();

                Logger.v("newAuthTokenBundle new accessToken: " + resp.getAccessToken());

                return newAuthTokenBundle(account, resp.getAccessToken());
            } catch (final Exception e) {
                Logger.ex(e);
            }
        }

        return newAccountBundle(context, response);
	}

	public static Bundle newAuthTokenBundle(final Account account, final String authToken) {
		final Bundle result = new Bundle();
		result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
		result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
		result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
		return result;
	}

    private static Bundle newResultBundle(final boolean result) {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, result);
        return bundle;
    }

    private static final class Helper {

        public static Class<?> getLoginActivityClass(final Context context) {
            try {
                final Class<?> klass = findLoginActivityClass(context);
                if (klass != null) return klass;
            } catch (Exception e) {
                Logger.ex(e);
            }

            return LoginActivity.class;
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
    }
}