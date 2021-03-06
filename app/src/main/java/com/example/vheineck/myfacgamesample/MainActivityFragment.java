package com.example.vheineck.myfacgamesample;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.GameRequestDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import bolts.AppLinks;

import static java.util.Arrays.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "MainActivityFragment";

    CallbackManager callbackManager;
    LoginButton mLoginButton;
    ProfilePictureView imgProfile;
    TextView txtProfileName;
    Button btnChallenge;
    ProfileTracker profileTracker;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    GameRequestDialog gameRequestDialog;
    String myProfileId;
    String myProfileName;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        imgProfile = (ProfilePictureView)view.findViewById(R.id.imgProfile);
        txtProfileName = (TextView)view.findViewById(R.id.txtProfileName);
        btnChallenge = (Button)view.findViewById(R.id.btnChallenge);
        btnChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestInvite();

            }
        });

        mLoginButton = (LoginButton) view.findViewById(R.id.login_button);
        mLoginButton.setReadPermissions("user_friends");
        // If using in a fragment
        mLoginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.d(TAG, "Facebook Success");
                Log.d(TAG, "Access token: " + loginResult.getAccessToken());
                Log.d(TAG, "Recently granted permissions: " + loginResult.getRecentlyGrantedPermissions().toString());
                Log.d(TAG, "Recently denied permissions: " + loginResult.getRecentlyDeniedPermissions().toString());

                if (loginResult.getRecentlyGrantedPermissions().contains("publish_actions") == false) {
                    LoginManager.getInstance().logInWithPublishPermissions(MainActivityFragment.this, Arrays.asList("publish_actions"));
                }

                RequestProfileData();

            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "Cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(TAG, "ERROR: " + exception.toString());
            }
        });

        RequestProfileData();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code

                Log.d(TAG, "ProfileTracker called");

                myProfileId = currentProfile.getId();
                myProfileName = currentProfile.getName();

            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                Log.d(TAG, "AccessToken called");

            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(getActivity().getApplicationContext(), getActivity().getIntent());
        if (targetUrl != null) {
            Log.i(TAG, "App Link Target URL: " + targetUrl.toString());
        }

        gameRequestDialog = new GameRequestDialog(this);
        gameRequestDialog.registerCallback(callbackManager,
                new FacebookCallback<GameRequestDialog.Result>() {
                    public void onSuccess(GameRequestDialog.Result result) {
                        String id = result.getRequestId();
                        

                        Log.d(TAG, "request_id: " + id);
                        Log.d(TAG, "friend_ids: " + result.getRequestRecipients().toString());

                        String friendProfileId = result.getRequestRecipients().get(0);

                        Log.d(TAG, "myProfileId: " + myProfileId);
                        Log.d(TAG, "myProfileName: " + myProfileName);
                        Log.d(TAG, "friendProfileId: " + friendProfileId);

                        Intent i = new Intent(getActivity(), ChallengeActivity.class);
                        i.putExtra("MY_PROFILE_ID", myProfileId);
                        i.putExtra("MY_PROFILE_NAME", myProfileName);
                        i.putExtra("FRIEND_PROFILE_ID", friendProfileId);

                        getActivity().startActivity(i);

                    }

                    public void onCancel() {
                        Log.d(TAG, "Game request cancelled: ");
                    }

                    public void onError(FacebookException error) {
                        Log.d(TAG, "Game request error: " + error.getLocalizedMessage());
                    }
                }
        );

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
        accessTokenTracker.startTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    public void RequestProfileData(){

        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object,GraphResponse response) {

                JSONObject json = response.getJSONObject();
                try {
                    if(json != null){

                        String name = json.getString("name");
                        String id = json.getString("id");

                        Log.d(TAG, "facebook id: " + id);

                        myProfileId = id;
                        myProfileName = name;

                        imgProfile.setProfileId(id);
                        txtProfileName.setText(name);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void requestInvite() {

        //inviteByAppInvite();
        invitebyGameRequest();

    }

    public void invitebyGameRequest() {

        GameRequestContent content = new GameRequestContent.Builder()
                .setMessage("Estou lhe desafiando!!!")
                .setTitle("Estou lhe desafiando!!!")
                .build();
        gameRequestDialog.show(content);

    }

    public void inviteByAppInvite() {

        String appLinkUrl = "https://fb.me/1569453850035766";
        String previewImageUrl = "http://musingsofacurvylady.com/wp-content/uploads/2014/10/homer-simpson.jpg";
        final String TAG = "fbv4";

        if (AccessToken.getCurrentAccessToken() == null) {
            // start login...
        } else {
            //FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
            //CallbackManager callbackManager = CallbackManager.Factory.create();

            FacebookCallback<AppInviteDialog.Result> facebookCallback = new FacebookCallback<AppInviteDialog.Result>() {
                @Override
                public void onSuccess(AppInviteDialog.Result result) {

                    Bundle bundle = result.getData();

                    Log.i(TAG, "InviteCallback - SUCCESS!" + bundle);

                    Log.i(TAG, "InviteCallback - bundle: " + bundle2string(bundle));

                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "InviteCallback - CANCEL!");
                }

                @Override
                public void onError(FacebookException e) {
                    Log.e(TAG, "InviteCallback - ERROR! " + e.getMessage());
                }
            };

            AppInviteDialog appInviteDialog = new AppInviteDialog(MainActivityFragment.this);
            if (appInviteDialog.canShow()) {
                AppInviteContent.Builder content = new AppInviteContent.Builder();
                content.setApplinkUrl(appLinkUrl);
                content.setPreviewImageUrl(previewImageUrl);
                AppInviteContent appInviteContent = content.build();
                appInviteDialog.registerCallback(callbackManager, facebookCallback);
                appInviteDialog.show(MainActivityFragment.this, appInviteContent);
            }
        }
    }

    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

}
