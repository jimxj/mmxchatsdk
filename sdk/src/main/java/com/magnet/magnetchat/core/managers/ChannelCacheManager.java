/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.core.managers;

import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.mmx.client.api.MMXChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelCacheManager {

    private static ChannelCacheManager _instance;

    /**
     * Key is channel name
     */
    private Map<String, Conversation> conversations;
    /**
     * Key is channel owner id
     */
    private Map<String, Message> messagesToApproveDeliver;

    private AtomicBoolean isConversationListUpdated = new AtomicBoolean(false);

    private Comparator<Conversation> conversationComparator = new Comparator<Conversation>() {
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
        if(null != conversation) {
            Conversation existingConversation = getConversationByName(conversation.getChannel().getName());
            if (existingConversation == null) {
                conversations.put(conversation.getChannel().getName(), conversation);
                //TODO : handling new message
                //conversation.setHasUnreadMessage(true);
                //conversation.setLastActiveTime(new Date());
            } else {
                boolean newMessageAdded = existingConversation.mergeFrom(conversation);
                if(newMessageAdded) {
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

}
