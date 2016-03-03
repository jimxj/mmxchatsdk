package com.magnet.magnetchat.mvp.presenters;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.mvp.api.ChooseUserContract;
import com.magnet.magnetchat.ui.activities.ChatActivity;
import com.magnet.magnetchat.util.Logger;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dlernatovich on 3/2/16.
 */
public class ChooseUserPresenterImpl implements ChooseUserContract.Presenter {

    private final String SEARCH_QUERY = "firstName:%s* OR lastName:%s*";
    private final ChooseUserContract.View mView;
    private Conversation mConversation;
    private ChooseUserContract.ChooseMode mAddmingMode;
    private WeakReference<Activity> mActivityRef;
    private List<User> mInitUsers;

    public ChooseUserPresenterImpl(ChooseUserContract.View view, Activity activity) {
        this(view, activity, null);
    }

    public ChooseUserPresenterImpl(ChooseUserContract.View view, Activity activity, String channelName) {
        this.mView = view;
        this.mActivityRef = new WeakReference<>(activity);
        if(null != channelName) {
            mConversation = ChannelCacheManager.getInstance().getConversationByName(channelName);
            mAddmingMode = ChooseUserContract.ChooseMode.MODE_ADD_USER;
        } else {
            mAddmingMode = ChooseUserContract.ChooseMode.MODE_NEW_CHAT;
        }

        onLoadUsers(true);
    }

    @Override public void onLoadUsers(boolean forceUpdate) {
        if(forceUpdate) {
            searchUsers("");
        } else {
            mView.updateList(mInitUsers);
        }
    }

    /**
     * Method which provide the searching of the user by query
     *
     * @param query current query
     */
    @Override
    public void searchUsers(@NonNull String query) {
        mView.setProgressIndicator(true);
        User.search(String.format(SEARCH_QUERY, query, query), 100, 0, "lastName:asc",
            userSearchCallback);
    }

    @Override
    public void onUsersSelected(@NonNull List<UserProfile> selectedUsers) {
        if (selectedUsers.size() > 0) {
            switch (mAddmingMode) {
                case MODE_ADD_USER:
                    onAddUsersToChat(selectedUsers);
                    break;
                case MODE_NEW_CHAT:
                    onNewChat(selectedUsers);
                    break;
            }
        } else {
            Utils.showMessage("No contact was selected");
        }
    }

    public void onAddUsersToChat(@NonNull List<UserProfile> selectedUsers) {
        mView.setProgressIndicator(true);
        ChannelHelper.addUserToConversation(mConversation, selectedUsers, addUserChannelListener);
    }

    public void onNewChat(@NonNull List<UserProfile> selectedUsers) {
        if(null != mActivityRef.get()) {
            mActivityRef.get().startActivity(ChatActivity.getIntentForNewChannel(mActivityRef.get(), selectedUsers));
        }
    }

    /**
     * Callback which provide to user search
     */
    private final ApiCallback<List<User>> userSearchCallback = new ApiCallback<List<User>>() {
        @Override
        public void success(List<User> users) {
            users.remove(User.getCurrentUser());
            if (mConversation != null) {
                List<Integer> indexes = new ArrayList<>();
                for(int i = 0; i < users.size(); i++) {
                    if(null != mConversation.getSupplier(users.get(i).getUserIdentifier())) {
                        indexes.add(i);
                    }
                }
                for(Integer i : indexes) {
                    users.remove(i.intValue());
                }
            }

            if(null == mInitUsers) {
                mInitUsers = new ArrayList<>(users);
            }

            mView.setProgressIndicator(false);
            Logger.debug("find users", "success");
            mView.updateList(users);
        }

        @Override
        public void failure(ApiError apiError) {
            mView.setProgressIndicator(false);
            Utils.showMessage("Can't find users");
            Logger.error("find users", apiError);
        }
    };

    /**
     * Listener which provide to listening of the action when users add to channel
     */
    private final ChannelHelper.OnAddUserListener addUserChannelListener = new ChannelHelper.OnAddUserListener() {
        @Override
        public void onSuccessAdded() {
            mView.setProgressIndicator(false);
            mView.finishSelection();
        }

        @Override
        public void onUserSetExists(String channelSetName) {

        }

        @Override
        public void onWasAlreadyAdded() {
            mView.setProgressIndicator(false);
            Utils.showMessage("Contact was already added");
            mView.finishSelection();
        }

        @Override
        public void onFailure(Throwable throwable) {
            mView.setProgressIndicator(false);
            Utils.showMessage("Can't add contact to channel");
        }
    };
}
