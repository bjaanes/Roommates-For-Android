package com.realkode.roomates.NotLoggedIn.Login;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Button;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.realkode.roomates.Helpers.FacebookProfilePictureDownloader;
import com.realkode.roomates.Helpers.ToastMaker;
import com.realkode.roomates.ParseSubclassses.User;
import com.realkode.roomates.R;

import java.util.Arrays;
import java.util.List;

public class FacebookLogin {
    Context context;
    Button facebookButton;
    LoginActivity loginActivity;

    public FacebookLogin(Context context, Button facebookButton, LoginActivity loginActivity) {
        this.context = context;
        this.facebookButton = facebookButton;
        this.loginActivity = loginActivity;
    }

    protected void startFacebookLogin() {
        final List<String> permissions = getFacebookPermissions();

        disableFacebookButton();

        final ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(R.string.logging_in), context.getString(R.string.please_wait), true);

        ParseFacebookUtils.logIn(permissions, loginActivity, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                progressDialog.dismiss();

                if (e == null && parseUser != null) {
                    User user = (User) parseUser;
                    if (user.isNew()) {
                        User.refreshChannels();
                        updateUserData();
                    } else {
                        // Facebook login
                        User.refreshChannels();
                        loginActivity.startMainActivity();
                    }
                } else {
                    ToastMaker.makeLongToast(context.getString(R.string.something_went_wrong), context);
                }
            }
        });
    }

    private List<String> getFacebookPermissions() {
        return Arrays.asList("basic_info", "email");
    }

    private void disableFacebookButton() {
        facebookButton.setClickable(false);
        facebookButton.setEnabled(false);
    }

    // Making the User object from the facebook-login.
    private void updateUserData() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                String facebookID = user.getId();

                // The URL for facebook profilepicture with the facebook user ID.
                final String profilePictureUrl = "http://graph.facebook.com/" + facebookID + "/picture?type=large";
                facebookButton.setClickable(false);
                facebookButton.setEnabled(false);

                final ParseUser currentUser = ParseUser.getCurrentUser();

                currentUser.setEmail((String) user.getProperty("email"));
                currentUser.setUsername((String) user.getProperty("email"));
                currentUser.put("displayName", user.getFirstName() + " " + user.getLastName());
                currentUser.saveInBackground(new UserSaveCallback(profilePictureUrl));
            }
        });
        request.executeAsync();

    }

    private class UserSaveCallback extends SaveCallback {
        private final String profilePictureURL;

        public UserSaveCallback(String profilePictureURL) {
            this.profilePictureURL = profilePictureURL;
        }

        @Override
        public void done(ParseException parseException) {
            if (parseException == null) {
                new FacebookProfilePictureDownloader().execute(profilePictureURL);
                facebookButton.setClickable(true);
                facebookButton.setEnabled(true);
                loginActivity.startMainActivity();
            } else {
                // Something went wrong, bail out.
                ParseUser.getCurrentUser().deleteEventually();
                ParseUser.logOut();
                facebookButton.setClickable(true);
                facebookButton.setEnabled(true);
            }

        }
    }
}