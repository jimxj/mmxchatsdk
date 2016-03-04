/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.core.managers;

import com.magnet.magnetchat.callbacks.NewMessageProcessListener;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.util.Logger;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelCacheManager {
    private static final String TAG = "ChannelCacheManager";

    private static ChannelCacheManager _instance;

    private List<MMXChannel> allSubscriptions;

    /**
     * Key is channel name
     */
    private Map<String, Conversation> conversations;
    /**
     * Key is channel owner id
     */
    private Map<String, Message> messagesToApproveDeliver;

    private AtomicBoolean isConversationListUpdated = new AtomicBoolean(false);

    private final Comparator<Conversation> conversationComparator = new Comparator<Conversation>() {
        @Override
        public int compare(Conversation lhs, Conversation rhs) {
            return 0 - lhs.getLastActiveTime().compareTo(rhs.getLastActiveTime());
        }
    };

    private ChannelCacheManager() {
        conversations = new HashMap<>();
    }

    public static ChannelCacheManager getInstance() {
        if (null == _instance) {
            _instance = new ChannelCacheManager();
        }

        return _instance;
    }

    public void setAllSubscriptions(List<MMXChannel> allSubscriptions) {
        if(null != this.allSubscriptions) {
            this.allSubscriptions.clear();
            conversations.clear();
        }
        this.allSubscriptions = allSubscriptions;
    }

    public List<MMXChannel> getAllSubscriptions() {
        return allSubscriptions;
    }

    public List<MMXChannel> getSubscriptions(int offset, int limit) {
        if (null != allSubscriptions) {
            int size = allSubscriptions.size();

            if (limit > 0) {
                if (offset >= 0 && offset < size) {
                    return (offset + limit) > size ? allSubscriptions.subList(offset, size)
                            : allSubscriptions.subList(offset, offset + limit);
                }
            } else {
                return allSubscriptions;
            }
        }

        return Collections.EMPTY_LIST;
    }

    public List<Conversation> getConversations() {
        ArrayList<Conversation> list = new ArrayList<>(conversations.values());
        Collections.sort(list, conversationComparator);
        return list;
    }

    public Map<String, Message> getMessagesToApproveDeliver() {
        if (messagesToApproveDeliver == null) {
            messagesToApproveDeliver = new HashMap<>();
        }
        return messagesToApproveDeliver;
    }

    public void addConversation(Conversation conversation) {
        if (null != conversation) {
            Conversation existingConversation = getConversationByName(conversation.getChannel().getName());
            if (existingConversation == null) {
                if(!allSubscriptions.contains(conversation.getChannel())) {
                    if (allSubscriptions.size() >= conversations.size()) {
                        allSubscriptions.add(conversations.size(), conversation.getChannel());
                    }
                }

                conversations.put(conversation.getChannel().getName(), conversation);

                //TODO : handling new message
                //conversation.setHasUnreadMessage(true);
                //conversation.setLastActiveTime(new Date());
            } else {
                boolean newMessageAdded = existingConversation.mergeFrom(conversation);
                if (newMessageAdded) {
                    existingConversation.setHasUnreadMessage(true);
                    existingConversation.setLastActiveTime(new Date());
                }
            }

            isConversationListUpdated.set(true);
        }
    }

    public void removeConversation(String channelName) {
        if (conversations != null && conversations.containsKey(channelName)) {
            conversations.remove(channelName);
            isConversationListUpdated.set(true);
        }
    }

    public boolean isConversationListUpdated() {
        return isConversationListUpdated.get();
    }

    public void resetConversationListUpdated() {
        isConversationListUpdated.set(false);
    }

    public void setConversationListUpdated() {
        isConversationListUpdated.set(true);
    }

    public void approveMessage(String messageId) {
        Message message = getMessagesToApproveDeliver().get(messageId);
        if (message != null) {
            message.setIsDelivered(true);
            messagesToApproveDeliver.remove(messageId);
        }
    }

    public Conversation getConversationByName(String name) {
        if (name == null) {
            return null;
        }
        return conversations.get(name.toLowerCase());
    }

    public void resetConversations() {
        conversations.clear();
        isConversationListUpdated.set(true);
    }

    public void handleIncomingMessage(final MMXMessage mmxMessage, final NewMessageProcessListener listener) {
        Logger.debug(TAG, "handle incoming  new message : " + mmxMessage);
        MMXChannel channel = mmxMessage.getChannel();
        if (channel != null) {
            final String channelName = channel.getName();
            Conversation conversation = ChannelCacheManager.getInstance().getConversationByName(channelName);
            final Message message = Message.createMessageFrom(mmxMessage);
            if (conversation != null) {
                conversation.addMessage(message, true);
                if (null != listener) {
                    listener.onProcessSuccess(conversation, message, false);
                }
            } else {
                ChannelHelper.getChannelDetails(mmxMessage.getChannel(), null, new ChannelHelper.OnReadChannelDetailListener() {
                    @Override
                    public void onSuccessFinish(Conversation conversation) {
                        addConversation(conversation);
                        conversation.addMessage(message, true);

                        if (null != listener) {
                            listener.onProcessSuccess(conversation, message, true);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Logger.error(TAG, "Failed to load channel details for channel : " + channelName);

                        if (null != listener) {
                            listener.onProcessFailure(throwable);
                        }
                    }
                });
            }
        }
    }

}
