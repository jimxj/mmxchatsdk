package com.magnet.magnetchat.mvp.views;

import android.app.Activity;

import com.magnet.magnetchat.model.Conversation;

import java.util.List;

/**
 * Created by dlernatovich on 3/1/16.
 */
public interface ChatListFragmentView {

    /**
     * Method which provide to getting of the conversation list
     *
     * @return getting conversation list
     */
    List<Conversation> getConversations();

    /**
     * Method which provide to getting of the activity
     *
     * @return activity
     */
    Activity getActivity();

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

    /**
     * Method which provide the dismissing of the leave dialog
     */
    void dismissLeaveDialog();

    /**
     * Method which provide the switching of the swipe container
     *
     * @param isNeedHidden
     */
    void switchSwipeContainer(boolean isNeedHidden);

    /**
     * Method which provide to getting of the TAG
     *
     * @return class tag
     */
    String getTAG();
}
