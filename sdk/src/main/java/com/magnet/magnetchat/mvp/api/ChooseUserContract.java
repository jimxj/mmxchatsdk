package com.magnet.magnetchat.mvp.api;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.magnet.max.android.UserProfile;

import java.util.List;

/**
 * Created by dlernatovich on 3/2/16.
 */
public interface ChooseUserContract {

    enum ChooseMode {MODE_NEW_CHAT, MODE_ADD_USER}

    interface View {

        /**
         * Method which provide to switching of the search user progress
         *
         * @param active
         */
        void setProgressIndicator(boolean active);

        /**
         * Method which provide the list updating from the list of users object
         *
         * @param users users list
         */
        void updateList(@NonNull List<? extends UserProfile> users);

        /**
         * Method which provide the closing of the Activity
         */
        void finishSelection();

        /**
         * Method which provide the getting of the activity
         *
         * @return current activity
         */
        Activity getActivity();

    }

    interface Presenter {

        /**
         * Method which provide to getting of the reading channels
         */
        void onLoadUsers(boolean forceUpdate);

        /**
         * Method which provide the searching of the user by query
         *
         * @param query current query
         */
        void searchUsers(@NonNull String query);

        /**
         * Method which provide the user selection
         *
         * @param userList user list
         */
        void onUsersSelected(@NonNull final List<UserProfile> userList);

        /**
         * Method which provide to adding of the user to the chat
         *
         * @param selectedUsers selected users
         */
        void onAddUsersToChat(@NonNull List<UserProfile> selectedUsers);

        /**
         * Method which provide the creating of the new chat
         *
         * @param selectedUsers selected users
         */
        void onNewChat(@NonNull List<UserProfile> selectedUsers);
    }

}
