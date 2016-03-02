/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.mvp.api;

import android.location.Location;
import android.net.Uri;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.max.android.Attachment;
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

    void showMessages(List<Message> messages);

    void showRecipients(List<UserProfile> recipients);

    void showNewMessage(Message message);

    void showImagePicker();

    void clearInput();

    void setSendEnabled(boolean enabled);

    //void showLocationPicker();

    void showLocation(Message message);

    void showImage(Message message);

    void showChatDetails(Conversation conversation);
  }

  interface UserActionsListener {

    void onLoadMessages(boolean forceUpdate);

    void onNewMessage(Message message);

    void onReadMessage();

    void onRefreshMessages();

    void onSendText(String text);

    void onSendImages(Uri[] uris);

    void onSendLocation(Location location);

    void onMessageClick(Message message);

    void onMessageLongClick(Message message);

    Conversation getCurrentConversation();

  }

}
