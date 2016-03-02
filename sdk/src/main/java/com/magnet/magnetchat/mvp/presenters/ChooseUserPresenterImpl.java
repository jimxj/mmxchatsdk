package com.magnet.magnetchat.mvp.presenters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.mvp.api.ChooseUserContract;
import com.magnet.magnetchat.util.Logger;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;

import java.util.List;

/**
 * Created by dlernatovich on 3/2/16.
 */
public class ChooseUserPresenterImpl implements ChooseUserContract.Presenter {

    private final String SEARCH_QUERY = "firstName:%s* OR lastName:%s*";
    private final ChooseUserContract.View view;
    private Conversation conversation;

    public ChooseUserPresenterImpl(ChooseUserContract.View view) {
        this.view = view;
    }

    /**
     * Method which provide the action when Activity of Fragment call onResume method
     * (WARNING: Should locate in the onResume method)
     */
    @Override
    public void onResume() {

    }

    /**
     * Method which provide the searching of the user by query
     *
     * @param query current query
     */
    @Override
    public void searchUsers(@NonNull String query) {
        view.switchSearchUserProgress(true);
        User.search(String.format(SEARCH_QUERY, query, query), 100, 0, "lastName:asc", apiCallback);
    }

    /**
     * Method which provide to setting of the current conversation
     *
     * @param conversation
     */
    @Override
    public void setConversation(@Nullable Conversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public void addUserToChannel(@NonNull List<UserProfile> userList) {
        view.switchSearchUserProgress(true);
        ChannelHelper.addUserToConversation(conversation, userList, addUserChannelListener);
    }

    /**
     * Callback which provide to user search
     */
    private final ApiCallback<List<User>> apiCallback = new ApiCallback<List<User>>() {
        @Override
        public void success(List<User> users) {
            users.remove(User.getCurrentUser());
            if (conversation != null) {
                for (UserProfile user : conversation.getSuppliersList()) {
                    users.remove(user);
                }
            }
            view.switchSearchUserProgress(false);
            Logger.debug("find users", "success");
            view.updateList(users);
        }

        @Override
        public void failure(ApiError apiError) {
            view.switchSearchUserProgress(false);
            view.showInformationMessage("Can't find users");
            Logger.error("find users", apiError);
        }
    };

    /**
     * Listener which provide to listening of the action when users add to channel
     */
    private final ChannelHelper.OnAddUserListener addUserChannelListener = new ChannelHelper.OnAddUserListener() {
        @Override
        public void onSuccessAdded() {
            view.switchSearchUserProgress(false);
            view.closeActivity();
        }

        @Override
        public void onUserSetExists(String channelSetName) {
            view.switchSearchUserProgress(false);
            Conversation anotherConversation = ChannelCacheManager.getInstance().getConversationByName(channelSetName);
            view.startAnotherConversation(anotherConversation);
        }

        @Override
        public void onWasAlreadyAdded() {
            view.switchSearchUserProgress(false);
            view.showInformationMessage("User was already added");
            view.closeActivity();
        }

        @Override
        public void onFailure(Throwable throwable) {
            view.switchSearchUserProgress(false);
            view.showInformationMessage("Can't add user to channel");
        }
    };
}
