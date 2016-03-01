package com.magnet.magnetchat.mvp.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.mvp.views.ChatListFragmentView;
import com.magnet.magnetchat.util.Logger;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dlernatovich on 3/1/16.
 */
public class ChatListFragmentPresenterImpl implements ChatListFragmentPresenter {

    private boolean isLoadingWhenCreating = false;
    private ChatListFragmentView view;

    /**
     * Constructor
     *
     * @param view
     */
    public ChatListFragmentPresenterImpl(ChatListFragmentView view) {
        this.view = view;
    }

    /**
     * Method which provide to getting of the reading channels
     */
    @Override
    public void getConversations() {
        ChannelHelper.readConversations(readChannelInfoListener);
    }

    /**
     * Method which provide the action when activity or fragment call onResume
     * (WARNING: Should be inside the onCreate method)
     */
    @Override
    public void onResume() {
        if (!isLoadingWhenCreating && ChannelCacheManager.getInstance().isConversationListUpdated()) {
            showAllConversations();
            ChannelCacheManager.getInstance().resetConversationListUpdated();
        }
        MMX.registerListener(eventListener);
        view.getActivity().registerReceiver(onAddedConversation, new IntentFilter(ChannelHelper.ACTION_ADDED_CONVERSATION));
    }

    /**
     * Method which provide the action when activity or fragment call onPause
     * (WARNING: Should be inside the onPause method)
     */
    @Override
    public void onPause() {
        MMX.unregisterListener(eventListener);
        view.getActivity().unregisterReceiver(onAddedConversation);
        view.dismissLeaveDialog();
    }

    /**
     * Method which provide to show of the messages by query
     *
     * @param query search query
     */
    @Override
    public void searchMessage(String query) {
        final List<Conversation> searchResult = new ArrayList<>();
        for (Conversation conversation : getAllConversations()) {
            for (UserProfile userProfile : conversation.getSuppliersList()) {
                if (userProfile.getDisplayName() != null && userProfile.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                    searchResult.add(conversation);
                    break;
                }
            }
        }
        if (searchResult.isEmpty()) {
            Utils.showMessage(view.getActivity(), "Nothing found");
        }
        view.showList(searchResult);
    }

    /**
     * Method which provide to getting of the list of the all conversations
     *
     * @return list of all conversations
     */
    @Override
    public List<Conversation> getAllConversations() {
        return ChannelCacheManager.getInstance().getConversations();
    }

    /**
     * Method which provide to showing of the all conversations
     */
    @Override
    public void showAllConversations() {
        view.showList(getAllConversations());
    }

    /**
     * Callback which provide the watch dog notification for the MMX events
     */
    private final MMX.EventListener eventListener = new MMX.EventListener() {
        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            Logger.debug(view.getTAG(), "onMessageReceived");
            showAllConversations();
            return false;
        }

        @Override
        public boolean onMessageAcknowledgementReceived(User from, String messageId) {
            Logger.debug(view.getTAG(), "onMessageAcknowledgementReceived");
            view.updateList();
            return false;
        }

        @Override
        public boolean onInviteReceived(MMXChannel.MMXInvite invite) {
            Logger.debug(view.getTAG(), "onInviteReceived");
            view.updateList();
            return false;
        }

        @Override
        public boolean onInviteResponseReceived(MMXChannel.MMXInviteResponse inviteResponse) {
            Logger.debug(view.getTAG(), "onInviteResponseReceived");
            view.updateList();
            return false;
        }

        @Override
        public boolean onMessageSendError(String messageId, MMXMessage.FailureCode code, String text) {
            Logger.debug("onMessageSendError");
            view.updateList();
            return false;
        }
    };

    /**
     * Callbacks which provide the notification for the BroadcastReceiver
     */
    private final BroadcastReceiver onAddedConversation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showAllConversations();
        }
    };

    /**
     * Callback which provide the listening of the watch dog notification when user try to read the channel information
     */
    private ChannelHelper.OnReadChannelInfoListener readChannelInfoListener = new ChannelHelper.OnReadChannelInfoListener() {
        @Override
        public void onSuccessFinish(Conversation lastConversation) {
            finishGetChannels();
            showAllConversations();
            if (view.getConversations() == null || view.getConversations().size() == 0) {
                //onConversationListIsEmpty(true);
                Log.w("read channels", "No conversation is available");
            } else {
                //onConversationListIsEmpty(false);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            finishGetChannels();
        }

        private void finishGetChannels() {
            isLoadingWhenCreating = false;
            view.switchSwipeContainer(true);
        }
    };
}
