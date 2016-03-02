package com.magnet.magnetchat.mvp.api;

import com.magnet.magnetchat.model.Conversation;
import java.util.List;

/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
public interface ChatListContract {

  interface View {

    /**
     * Method which provide to show of the list of the conversations
     *
     * @param conversations conversation
     */
    void showList(List<Conversation> conversations);

    /**
     * Method which provide the list updating
     */
    void updateList();

    /**
     * Method which provide to show of the new chat
     */
    void showNewChat();

    /**
     * Method which provide the conversation details
     *
     * @param conversation current conversation
     */
    void showChatDetails(Conversation conversation);

    void showLeaveConfirmation(Conversation conversation);

    /**
     * Method which provide the dismissing of the leave dialog
     */
    void dismissLeaveDialog();

    /**
     * Show or hide the progress bar
     *
     * @param active
     */
    void setProgressIndicator(boolean active);
  }

  interface UserActionsListener {

    /**
     * Method which provide to getting of the reading channels
     */
    void onLoadConversations(boolean forceUpdate);

    /**
     * Method which provide to show of the messages by query
     *
     * @param query search query
     */
    void onSearchMessage(final String query);

    void onConversationClick(Conversation conversation);

    void onConversationLongClick(Conversation conversation);

    /**
     * Method which provide the action when activity or fragment call onResume
     * (WARNING: Should be inside the onCreate method)
     */
    void onResume();

    /**
     * Method which provide the action when activity or fragment call onPause
     * (WARNING: Should be inside the onPause method)
     */
    void onPause();

    /**
     * Method which provide to getting of the list of the all conversations
     *
     * @return list of all conversations
     */
    List<Conversation> getAllConversations();
  }
}
