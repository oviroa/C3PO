package android.commutr.com.commutr;

import android.commutr.com.commutr.base.BaseActivity;
import android.commutr.com.commutr.model.Identity;
import android.commutr.com.commutr.utils.ClientUtility;
import android.commutr.com.commutr.utils.Installation;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import java.util.List;


/**
 * A login screen that offers login via email
 */
public class LoginActivity extends BaseActivity{

    //request queue for server calls
    private RequestQueue commuteVolley;
    //tag for Volley
    private final Object TAG = new Object();

    // UI references.
    private AutoCompleteTextView emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        if(getDataManager().retrieveUserEmail(getApplicationContext()) != null){

            logIn();
        }
        else {
            setContentView(R.layout.activity_login);
            // Set up the login form.
            emailView = (AutoCompleteTextView) findViewById(R.id.email);
            addEmailsToAutoComplete(ClientUtility.getUserEmails(this));

            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        emailView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();


        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!ClientUtility.isEmailValid(emailView.getText())) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            getDataManager().storeUserEmail(emailView.getText().toString(), getApplicationContext());


            MixpanelAPI mixpanel =
                    MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));

            mixpanel.getPeople().identify(mixpanel.getDistinctId());
            mixpanel.getPeople()
                        .set(getResources().getString(R.string.user_email),emailView.getText().toString());

            //store installation identity if network on
            if (ClientUtility.isNetworkAvailable(getApplicationContext())) {
                registerIdentity(buildIdentity());
            }

            // perform the user login attempt.
            logIn();
        }
    }

    private void logIn() {

        Intent intent = new Intent(this, CommuteActivity.class);
        startActivity(intent);
    }


    private void registerIdentity(Identity identity) {

        if(commuteVolley == null) {
            commuteVolley = Volley.newRequestQueue(getApplicationContext());
        }
        getDataManager().storeIndentity
                (
                        identity,
                        getApplicationContext(),
                        commuteVolley,
                        TAG,
                        new Response.Listener<JSONObject>() {

                            public void onResponse(JSONObject result) {

                                if(result.has("error")) {
                                    Logger.warn("IDENTITY ERROR",result.toString());
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {

                                error.printStackTrace();

                            }
                        }
                );

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        emailView.setAdapter(adapter);
    }

    private Identity buildIdentity(){

        Identity myInstallation = new Identity();
        myInstallation.setEmail((getDataManager().retrieveUserEmail(getApplicationContext())));
        myInstallation.setIdentifier(Installation.id(getApplicationContext()));

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            myInstallation.setVersion(new StringBuilder().append("A-").append(version).toString());
        } catch (PackageManager.NameNotFoundException e) {
            myInstallation.setVersion("n/a");
        }

        return myInstallation;

    }
}



