package com.example.vheineck.myfacgamesample;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChallengeActivityFragment extends Fragment {

    private static final String TAG  = "ChallengeActivityFrag";

    ProfilePictureView imgProfile;
    ProfilePictureView imgFriendProfile;
    TextView txtProfileName;
    TextView txtFriendProfileName;
    String myProfileId;
    String friendProfileId;

    public ChallengeActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);

        imgProfile = (ProfilePictureView)view.findViewById(R.id.imgProfile);
        imgFriendProfile = (ProfilePictureView)view.findViewById(R.id.imgFriendProfile);
        txtProfileName = (TextView)view.findViewById(R.id.txtProfileName);
        txtFriendProfileName = (TextView)view.findViewById(R.id.txtFriendProfileName);

        myProfileId = getActivity().getIntent().getExtras().getString("MY_PROFILE_ID");
        friendProfileId = getActivity().getIntent().getExtras().getString("FRIEND_PROFILE_ID");
        String profileName = getActivity().getIntent().getExtras().getString("MY_PROFILE_NAME");

        Log.d(TAG, "myProfileId: " + myProfileId);
        Log.d(TAG, "friendProfileId: " + friendProfileId);

        imgProfile.setProfileId(myProfileId);
        imgFriendProfile.setProfileId(friendProfileId);
        txtProfileName.setText(profileName);

        GraphRequest request = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), friendProfileId, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {

                Log.d(TAG, "response: " +response.toString());

                FacebookRequestError error = response.getError();
                if (error != null) {
                    Log.d(TAG, "ERROR: " + error.getErrorMessage());
                }

                try {

                    JSONObject json = response.getJSONObject();
                    String name = json.getString("name");

                    txtFriendProfileName.setText(name);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
        request.setParameters(parameters);
        request.executeAsync();

        return view;
    }
}
