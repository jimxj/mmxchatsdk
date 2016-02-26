package com.magnet.magnetchat.ui.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.callbacks.BaseActivityCallback;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.ui.fragments.BaseFragment;
import com.magnet.magnetchat.ui.fragments.ChatListFragment;
import com.magnet.max.android.User;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListActivity extends BaseActivity implements BaseActivityCallback {
    private static final String TAG = ChatListActivity.class.getSimpleName();

    Toolbar toolbar;

    private LinearLayout llUserProfile;
    private CircleImageView ivUserAvatar;
    private TextView tvUserName;

    private int unreadSupport = 0;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_chat_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,
            "\n---------------------------------\nHomeActivity created\n---------------------------------\n");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(User.getCurrentUser().getDisplayName());

        setFragment();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (User.getCurrentUser() != null) {
            tvUserName.setText(User.getCurrentUser().getDisplayName());
            if (UserHelper.isMagnetSupportMember()) {
                MMX.registerListener(homeMessageReceiver);
            }

            if (null != User.getCurrentUser().getAvatarUrl()) {
                Glide.with(this)
                        .load(User.getCurrentUser().getAvatarUrl())
                        .placeholder(R.mipmap.ic_user)
                                //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .centerCrop()
                        .into(ivUserAvatar);
            }
        } else {
            Log.w(TAG, "CurrentUser is null, logout");
            //TODO :
            //UserHelper.logout(logoutListener);
        }
    }

    @Override
    protected void onPause() {
        MMX.unregisterListener(homeMessageReceiver);
        super.onPause();
    }

    /**
     * method which provide the setting of the current fragment co container view
     *
     */
    private void setFragment() {
        BaseFragment baseFragment = new ChatListFragment();
        baseFragment.setBaseActivityCallback(this);
        replace(baseFragment, R.id.container, "chats");
    }

    @Override
    public void onReceiveFragmentEvent(Event event) {

    }

    /**
     * Receiver which check if drawer button should show indicator, that support section has unread message
     */
    private MMX.EventListener homeMessageReceiver = new MMX.EventListener() {
        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            if (mmxMessage != null && mmxMessage.getChannel() != null) {
                MMXChannel channel = mmxMessage.getChannel();
                unreadSupport++;
            }
            return false;
        }
    };

    @Override public void onClick(View v) {

    }
}
