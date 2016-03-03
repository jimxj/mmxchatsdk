package com.magnet.magnetchat.mvp.presenters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.magnet.magnetchat.callbacks.NewMessageProcessListener;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.mvp.api.ChatListContract;
import com.magnet.magnetchat.util.Logger;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.Max;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.ChannelDetail;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dlernatovich on 3/1/16.
 */
public class ChatListPresenterImpl implements ChatListContract.Presenter {
    protected static final String TAG = "ChatListPresenter";

    protected List<Conversation> mConversations = new ArrayList<>();
    private boolean isLoadingWhenCreating = false;
    protected ChatListContract.View mView;

    /**
     * Constructor
     *
     * @param view
     */
    public ChatListPresenterImpl(ChatListContract.View view) {
        this.mView = view;
    }

    /**
     * Method which provide to getting of the reading channels
     */
    @Override
    public void onLoadConversations(int offset, int limit) {
        ChannelHelper.getSubscriptionDetails(offset, limit, channelsListener);
    }

    @Override public void onConversationUpdate(Conversation conversation, boolean isNew) {
        mView.showConversationUpdate(conversation, isNew);
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

        if (null != Max.getApplicationContext()) {
            Max.getApplicationContext().registerReceiver(onAddedConversation, new IntentFilter(ChannelHelper.ACTION_ADDED_CONVERSATION));
        }
    }

    /**
     * Method which provide the action when activity or fragment call onPause
     * (WARNING: Should be inside the onPause method)
     */
    @Override
    public void onPause() {
        MMX.unregisterListener(eventListener);

        if (null != Max.getApplicationContext()) {
            Max.getApplicationContext().unregisterReceiver(onAddedConversation);
        }
        mView.dismissLeaveDialog();
    }

    /**
     * Method which provide to show of the messages by query
     *
     * @param query search query
     */
    @Override
    public void onSearchConversation(String query) {
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
            Utils.showMessage(Max.getApplicationContext(), "Nothing found");
        }
        mView.showList(searchResult);
    }

    @Override public void onResetSearch() {
        showAllConversations();
    }

    /**
     * Method which provide the action when user click on the conversation channel
     *
     * @param conversation channel
     */
    @Override
    public void onConversationClick(Conversation conversation) {
        mView.showChatDetails(conversation);
    }

    /**
     * Method which provide the action when user do long click for the conversation
     *
     * @param conversation channel
     */
    @Override
    public void onConversationLongClick(Conversation conversation) {

    }

    /**
     * Method which provide to getting of the list of the all conversations
     *
     * @return list of all conversations
     */
    @Override
    public List<Conversation> getAllConversations() {
        return mConversations;
    }

    /**
     * Method which provide to showing of the all conversations
     */
    private void showAllConversations() {
        mView.showList(mConversations);
    }

    /**
     * Callback which provide the watch dog notification for the MMX events
     */
    private final MMX.EventListener eventListener = new MMX.EventListener() {
        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            Logger.debug(TAG, "onMessageReceived");
            ChannelCacheManager.getInstance().handleIncomingMessage(mmxMessage, new NewMessageProcessListener() {
                @Override public void onProcessSuccess(Conversation conversation, Message message,
                    boolean isNewChat) {
                    onConversationUpdate(conversation, isNewChat);
                }

                @Override public void onProcessFailure(Throwable throwable) {
                    Logger.error(TAG, "onProcessFailure", throwable);
                }
            });
            return false;
        }

        @Override
        public boolean onMessageAcknowledgementReceived(User from, String messageId) {
            Logger.debug(TAG, "onMessageAcknowledgementReceived");
            //mView.updateList();
            return false;
        }

        @Override
        public boolean onInviteReceived(MMXChannel.MMXInvite invite) {
            Logger.debug(TAG, "onInviteReceived");
            //mView.updateList();
            return false;
        }

        @Override
        public boolean onInviteResponseReceived(MMXChannel.MMXInviteResponse inviteResponse) {
            Logger.debug(TAG, "onInviteResponseReceived");
            //mView.updateList();
            return false;
        }

        @Override
        public boolean onMessageSendError(String messageId, MMXMessage.FailureCode code, String text) {
            Logger.debug("onMessageSendError");
            //mView.updateList();
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
     * Listener which provide the listening of the watch dog notification for channel creation
     */
    private final MMXChannel.OnFinishedListener<List<ChannelDetail>> channelsListener = new MMXChannel.OnFinishedListener<List<ChannelDetail>>() {
        @Override
        public void onSuccess(List<ChannelDetail> channelDetails) {
            if (null != channelDetails) {
                for (ChannelDetail cd : channelDetails) {
                    Conversation c = new Conversation(cd);
                    ChannelCacheManager.getInstance().addConversation(c);
                }
            }

            mConversations.clear();
            mConversations.addAll(ChannelCacheManager.getInstance().getConversations());

            showAllConversations();

            finishGetChannels();
        }

        @Override
        public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
            handleError(failureCode.toString(), throwable);
            finishGetChannels();
        }

        private void finishGetChannels() {
            isLoadingWhenCreating = false;
            mView.setProgressIndicator(false);
        }

        private void handleError(String message, Throwable throwable) {
            Logger.error(TAG, "Can't get conversations due to "
                    + message
                    + ", throwable : \n"
                    + throwable);
        }
    };

}
