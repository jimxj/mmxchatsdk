/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.mvp.presenters;

import android.app.Activity;
import android.util.Log;

import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.mvp.api.ChatDetailsContract;
import com.magnet.magnetchat.ui.activities.ChooseUserActivity;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.max.android.util.StringUtil;
import com.magnet.mmx.client.api.ListResult;
import com.magnet.mmx.client.api.MMXChannel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatDetailsPresenterImpl implements ChatDetailsContract.Presenter {
    private static final String TAG = "ChatDetailsPresenter";

    private final ChatDetailsContract.View mView;
    private final MMXChannel mmCurrentChannel;
    private WeakReference<Activity> mActivityRef;

    public ChatDetailsPresenterImpl(ChatDetailsContract.View view, MMXChannel channel, Activity activity) {
        this.mView = view;
        this.mmCurrentChannel = channel;
        this.mActivityRef = new WeakReference<>(activity);
    }

    @Override
    public void onLoadRecipients(boolean forceUpdate) {
        mView.setProgressIndicator(true);
        mmCurrentChannel.getAllSubscribers(100, 0, new MMXChannel.OnFinishedListener<ListResult<User>>() {
            @Override
            public void onSuccess(ListResult<User> userListResult) {
                onComplete();

                List<UserProfile> userProfiles = new ArrayList<>(userListResult.items.size());
                for (User u : userListResult.items) {
                    if (!u.getUserIdentifier().equals(User.getCurrentUserId())) {
                        userProfiles.add(u);
                    }
                }
                Collections.sort(userProfiles, UserHelper.getUserProfileComparator());
                mView.showRecipients(userProfiles);
            }

            @Override
            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                onComplete();
                Log.e(TAG, failureCode.toString(), throwable);
                Utils.showMessage("Failed to load recipients, please try later");
            }

            private void onComplete() {
                mView.setProgressIndicator(false);
            }
        });
    }

    @Override
    public void onAddRecipients() {
        if (null != mActivityRef.get()) {
            mActivityRef.get().startActivity(ChooseUserActivity.getIntentToAddUserToChannel(mActivityRef.get(),
                    mmCurrentChannel.getName()));
            mView.finishDetails();
        }
    }

    @Override
    public boolean isOwnerChannel() {
        if (mmCurrentChannel == null) {
            return false;
        }
        if (StringUtil.isStringValueEqual(mmCurrentChannel.getOwnerId(), User.getCurrentUserId())) {
            return true;
        }
        return false;
    }
}
