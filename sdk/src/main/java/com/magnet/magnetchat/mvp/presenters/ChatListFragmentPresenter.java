package com.magnet.magnetchat.mvp.presenters;

import com.magnet.magnetchat.model.Conversation;

import java.util.List;

/**
 * Created by dlernatovich on 3/1/16.
 */
public interface ChatListFragmentPresenter {

    /**
     * Method which provide to getting of the reading channels
     */
    void getConversations();

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
     * Method which provide to show of the messages by query
     *
     * @param query search query
     */
    void searchMessage(final String query);

    /**
     * Method which provide to getting of the list of the all conversations
     *
     * @return list of all conversations
     */
    List<Conversation> getAllConversations();

    /**
     * Method which provide to showing of the all conversations
     */
    void showAllConversations();

}
