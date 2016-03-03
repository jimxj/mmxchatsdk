/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.mvp.api;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.max.android.UserProfile;

import java.util.List;

public interface ChatContract {

    interface View {

        /**
         * Show or hide the progress bar
         *
         * @param active
         */
        void setProgressIndicator(boolean active);

        /**
         * Method which provide to show the messages
         *
         * @param messages messages list
         */
        void showMessages(List<Message> messages);

        /**
         * Method which provide to show the recipients
         *
         * @param recipients recipients list
         */
        void showRecipients(List<UserProfile> recipients);

        /**
         * Method whihc provide to show of the new message
         *
         * @param message new message
         */
        void showNewMessage(Message message);

        /**
         * Method which provide to show of the image picker
         */
        void showImagePicker();

        /**
         * Method which provide to clearing of the input field
         */
        void clearInput();

        /**
         * Method which provide the enabling of the send button
         *
         * @param enabled is need enable
         */
        void setSendEnabled(boolean enabled);

        //void showLocationPicker();

        /**
         * Method whihc provide to show of the location
         *
         * @param message message
         */
        void showLocation(Message message);

        /**
         * Method which provide to show of the image message
         *
         * @param message image message
         */
        void showImage(Message message);

        /**
         * Method which provide the getting of the activity
         *
         * @return current activity
         */
        @NonNull
        Activity getActivity();
    }

    interface Presenter {

        /**
         * Method which provide the action when Activity/Fragment call onResume method
         * (WARNING: Should be call in the onCreate method)
         */
        void onResume();

        /**
         * Method which provide the action when Activity/Fragment call onPause method
         * (WARNING: Should be call in the onPause method)
         */
        void onPause();


        void onLoadMessages(boolean forceUpdate);

        void onLoadRecipients(boolean forceUpdate);

        void onNewMessage(Message message);

        void onReadMessage();

        void onSendText(String text);

        void onSendImages(Uri[] uris);

        void onSendLocation(Location location);

        void onMessageClick(Message message);

        void onMessageLongClick(Message message);

        void onChatDetails();

        Conversation getCurrentConversation();

    }

}
