package com.length6array.donationtracker2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;

/**
 * This page is long and crazy!!! Do not worry though!! (I don't even know what half this stuff is
 * either so you're good!) This is the Registration screen. Inside onCreate: text edits and spinners
 * used to gather the persons info to make a new user When the user clicks emailSignIn button, all
 * that information is checked to make sure 1. not a repeat account (it would then tell the person
 * to log on not register 2. all the info is legit (no empty fields, password & retyped password
 * match, etc) If so, the user is then taken to LocationListActivity
 */
public class Registration extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /** Keep track of the login task to ensure we can cancel it if requested. */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private EditText reTypePassword;
    private Spinner userSpinner;
    private String mText = "";

    PersonDBHandler personDBHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        personDBHandler = new PersonDBHandler(this, null, null, 1);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        reTypePassword = (EditText) findViewById(R.id.reTypePassword);
        reTypePassword.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE
                                || actionId == EditorInfo.IME_NULL) {
                            // TODO check if this is needed
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        // create account button
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("Registration", "Clicked register");
                        attemptLogin();
                    }
                });

        // go back to welcome screen
        FloatingActionButton back = (FloatingActionButton) findViewById(R.id.back);
        back.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("Registration", "Clicked back");
                        startActivity(new Intent(Registration.this, Welcome.class));
                    }
                });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        userSpinner = findViewById(R.id.personType);

        // this adapter fills in the spinner with the different types of users
        ArrayAdapter<String> adapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, Person.userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);
    }

    /**
     * Attempts to register the account specified by the login form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no actual login attempt
     * is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String retypedPassword = reTypePassword.getText().toString();
        String userType = userSpinner.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (!(password.equals(retypedPassword))) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        //Creates alert dialog for entering an authentication code for Admins and Location Employees
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Secure Authentication");
        builder.setMessage("Please enter an authentication code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        final String type = userType;
        final String finalEmail = email;
        final String finalPass = password;

        //Runnable for reading the authentication code after it's been entered
        //and the user selects "Confirm"
        final Runnable readAuthentication = new Runnable() {
            @Override
            public void run() {
                View focusView2 = null;
                boolean cancel2 = false;

                if (TextUtils.isEmpty(mText)) {
                    input.setError(getString(R.string.error_field_required));
                    focusView2 = input;
                    cancel2 = true;
                } else if (!(type.equals(mText))) {
                    input.setError("Incorrect authentication code");
                    focusView2 = mPasswordView;
                    cancel2 = true;
                }

                if (cancel2) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView2.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    showProgress(true);

                    // makes a new user, then goes to new Activity
                    mAuthTask = new UserLoginTask(finalEmail, finalPass, type);
                    mAuthTask.execute((Void) null);
                    if (mAuthTask.doInBackground()) {
                        Log.i("Registration", "Switching to main");
                        startActivity(new Intent(Registration.this, LocationListActivity.class));
                    }
                }
            }
        };

        //Action for when user presses "Confirm"
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mText = input.getText().toString();
                        dialog.dismiss();
                        readAuthentication.run();
                    }
                });

        //Action for when user presses "Cancel"
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //This will show the alert dialog to allow the user to enter the necessary
            //authentication code
            if (userType == "Admin" || userType == "Location Employee") {
                dialog.show();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);

                // makes a new user, then goes to new Activity
                mAuthTask = new UserLoginTask(email, password, userType);
                mAuthTask.execute((Void) null);
                if (mAuthTask.doInBackground()) {
                    Log.i("Registration", "Switching to main");
                    startActivity(new Intent(Registration.this, LocationListActivity.class));
                }
            }
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".com");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form. I don't really know what this does tbh but I
     * just kept it in bc it looked important
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(
                            new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                                }
                            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(
                            new AnimatorListenerAdapter() {
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

    /**
     * I think this has something do do with grabbing contacts from the users phone. I kept it in,
     * but tbh i think its also unnecessary.
     */
    // TODO check if this is really necessary
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                new String[] {ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {}

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        // Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        Registration.this,
                        android.R.layout.simple_dropdown_item_1line,
                        emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate the user. THIS IS
     * VERY IMPORTANT. What's going on: using the data the user typed in, I check if the stuff is
     * already in the system, otherwise, if the code has gotten to this point it means that all the
     * fields are valid and a new user is created!
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mUserType;

        UserLoginTask(String email, String password, String userType) {
            mEmail = email;
            mPassword = password;
            mUserType = userType;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            if (Person.credentials.containsKey(mEmail)) {
                Log.i("Registration", "Found taken email");
                mEmailView.setError(getString(R.string.email_Taken));
                return false;
            } else {
                // THIS IS THAT KEY IMPORTANT THING
                Log.i("Registration", "Making new account");
                Person p = new Person(mEmail, mPassword, mUserType);
                if (personDBHandler.addPerson(p))
                    Toast.makeText(Registration.this, "User added!", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(Registration.this, "User not added!", Toast.LENGTH_SHORT).show();
                }
                Person.credentials.put(mEmail, mPassword);
                Person.allUsers.add(p);
                return true;
            }
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
