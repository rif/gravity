package com.zedmedia.gravity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AccountManagerActivity extends Activity {

	AccountManager mAccountManager;
	AccountManagerFuture<Bundle> c;
	String token;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gravity);

		mAccountManager = AccountManager.get(this);
		Account[] acc = mAccountManager.getAccounts();
		System.out.println("1111: " + acc.length);
		for (int i = 1; i < acc.length; i++) {
			System.out.println("Account name==" + acc[i].name);
			System.out.println("Account Type==" + acc[i].type);
		}
		AuthenticatorDescription[] ad = mAccountManager.getAuthenticatorTypes();
		System.out.println("2222: " + ad.length);
		for (int i = 1; i < ad.length; i++) {
			System.out.println("AuthenticatorDescription==" + ad[i].type);
		}

		tokenForTwitter();
		tokenForFacebook();
		finish();
	}

	private void tokenForFacebook() {
		Account[] accts = mAccountManager
				.getAccountsByType("com.facebook.auth.login");
		int i = 0;
		System.out.println("FB: " + accts.length);
		if (accts.length > 0) {
			System.out.println("here");
			Account acct = accts[0];
			c = mAccountManager.getAuthToken(acct, "com.facebook.auth.token",
					null, this, new AccountManagerCallback<Bundle>() {

						@Override
						public void run(AccountManagerFuture<Bundle> arg0) {
							try {
								Bundle b = arg0.getResult();
								System.out.println("Facebook THIS AUHTOKEN: "
										+ b.getString(AccountManager.KEY_AUTHTOKEN));
								Intent launch = (Intent) b
										.get(AccountManager.KEY_INTENT);
								if (launch != null) {
									startActivityForResult(launch, 0);
									return;
								}
							} catch (Exception e) {
								System.out.println("EXCEPTION@AUTHTOKEN");
							}
						}
					}, null);

			c = mAccountManager.getAuthToken(acct,
					"com.facebook.auth.token.secret" /*
													 * what goes here
													 */, null, this,
					new AccountManagerCallback<Bundle>() {

						@Override
						public void run(AccountManagerFuture<Bundle> arg0) {
							try {
								Bundle b = arg0.getResult();
								System.out.println("Facebook THIS AUHTOKEN: "
										+ b.getString(AccountManager.KEY_AUTHTOKEN));
								Intent launch = (Intent) b
										.get(AccountManager.KEY_INTENT);
								if (launch != null) {
									startActivityForResult(launch, 0);
									return;
								}
							} catch (Exception e) {
								System.out.println("EXCEPTION@AUTHTOKEN");
							}
						}
					}, null);

			// mHandler.sendMessageDelayed(mHandler.obtainMessage(CALL), 0);

			i++;
		}

	}

	public void tokenForTwitter() {
		Account[] accts = mAccountManager
				.getAccountsByType("com.twitter.android.auth.login");
		int i = 0;
		System.out.println("TW: " + accts.length);
		if (accts.length > 0) {
			System.out.println("here");
			Account acct = accts[0];
			c = mAccountManager.getAuthToken(acct,
					"com.twitter.android.oauth.token" /* what goes here */, null,
					this, new AccountManagerCallback<Bundle>() {

						@Override
						public void run(AccountManagerFuture<Bundle> arg0) {
							try {
								Bundle b = arg0.getResult();
								System.out.println("twitter THIS AUHTOKEN: "
										+ b.getString(AccountManager.KEY_AUTHTOKEN));
								Intent launch = (Intent) b
										.get(AccountManager.KEY_INTENT);
								if (launch != null) {
									startActivityForResult(launch, 0);
									return;
								}
							} catch (Exception e) {
								System.out.println("EXCEPTION@AUTHTOKEN");
							}
						}
					}, null);

			c = mAccountManager.getAuthToken(acct,
					"com.twitter.android.oauth.token.secret" /*
															 * what goes here
															 */, null, this,
					new AccountManagerCallback<Bundle>() {

						@Override
						public void run(AccountManagerFuture<Bundle> arg0) {
							try {
								Bundle b = arg0.getResult();
								System.out.println("twitter THIS AUHTOKEN: "
										+ b.getString(AccountManager.KEY_AUTHTOKEN));
								Intent launch = (Intent) b
										.get(AccountManager.KEY_INTENT);
								if (launch != null) {
									startActivityForResult(launch, 0);
									return;
								}
							} catch (Exception e) {
								System.out.println("EXCEPTION@AUTHTOKEN");
							}
						}
					}, null);

			// mHandler.sendMessageDelayed(mHandler.obtainMessage(CALL), 0);

			i++;
		}

	}

}