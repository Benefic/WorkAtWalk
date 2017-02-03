/*
 * Copyright abenefic (c) 2017.
 */

package ru.itmasterskaya.workatwalk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public static final String ARG_ACCOUNT_TYPE = "AccountType";
    public static final String ARG_AUTH_TYPE = "AuthTokenType";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "IsAddingNewAccount";
    public static final String ACCOUNT_TYPE = "Work at walk";
    private AccountManager mAccountManager;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUser;
    private EditText mPasswordView;
    private EditText mServerView;
    private View mProgressView;
    private View mLoginFormView;

    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUser = (EditText) findViewById(R.id.user);
        mServerView = (EditText) findViewById(R.id.server);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mLoginSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mLoginSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mAccountManager = AccountManager.get(getBaseContext());

    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUser.setError(null);
        mPasswordView.setError(null);
        mServerView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUser.getText().toString();
        String password = mPasswordView.getText().toString();
        String server = mServerView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid server, if the user entered one.
        if (!TextUtils.isEmpty(server) && !isServerValid(server)) {
            mServerView.setError(getString(R.string.error_invalid_server));
            focusView = mServerView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            mUser.setError(getString(R.string.user_field_required));
            focusView = mUser;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(user, password, server);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isServerValid(String server) {
        return server.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;
        private final String mServer;

        UserLoginTask(String user, String password, String server) {
            mUser = user;
            mPassword = password;
            mServer = server;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // TODO: register the new account here.
            String accountToken = NetworkServices.autenticationRequest(getBaseContext(), mUser, mPassword);
            if (accountToken.equals("false")) {
                return false;
            } else {
                final Intent res = new Intent();
                res.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUser);
                res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ARG_ACCOUNT_TYPE);
                res.putExtra(AccountManager.KEY_AUTHTOKEN, accountToken);
                res.putExtra(Constant.USER_PASSWORD, mPassword);
                finishLogin(res);
                return true;
            }
        }

        private void finishLogin(Intent intent) {
            String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            String accountPassword = intent.getStringExtra(Constant.USER_PASSWORD);
            final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
            if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
                String authtokenType = ACCOUNT_TYPE;
                // Creating the account on the device and setting the auth token we got
                // (Not setting the auth token will cause another call to the server to authenticate the user)
                mAccountManager.addAccountExplicitly(account, accountPassword, null);
                mAccountManager.setAuthToken(account, authtokenType, authtoken);
            } else {
                mAccountManager.setPassword(account, accountPassword);
            }
            //setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

