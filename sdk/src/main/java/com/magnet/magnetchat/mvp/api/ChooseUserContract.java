package com.magnet.magnetchat.mvp.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.magnet.magnetchat.model.Conversation;
import com.magnet.max.android.UserProfile;

import java.util.List;

/**
 * Created by dlernatovich on 3/2/16.
 */
public interface ChooseUserContract {

    interface View {

        /**
         * Method which provide the show message
         *
         * @param message current message
         */
        void showInformationMessage(String message);

        /**
         * Method which provide to switching of the search user progress
         *
         * @param isNeedShow
         */
        void switchSearchUserProgress(boolean isNeedShow);

        /**
         * Method which provide the list updating from the list of users object
         *
         * @param users users list
         */
        void updateList(@NonNull List<? extends UserProfile> users);

        /**
         * Method which provide the closing of the Activity
         */
        void closeActivity();

        /**
         * Method which provide to start of the another conversation
         *
         * @param anotherConversation conversation object
         */
        void startAnotherConversation(@Nullable Conversation anotherConversation);

    }

    interface Presenter {

        /**
         * Method which provide the action when Activity of Fragment call onResume method
         * (WARNING: Should locate in the onResume method)
         */
        void onResume();

        /**
         * Method which provide the searching of the user by query
         *
         * @param query current query
         */
        void searchUsers(@NonNull String query);

        /**
         * Method which provide to setting of the current conversation
         *
         * @param conversation
         */
        void setConversation(@Nullable Conversation conversation);

        /**
         * Method which provide to add the users to the channel
         *
         * @param userList user list
         */
        void addUserToChannel(@NonNull final List<UserProfile> userList);

    }

}
