package com.magnet.magnetchat.model;

import android.location.Location;
import com.magnet.magnetchat.helpers.DateHelper;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.util.Logger;
import com.magnet.max.android.Attachment;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.ChannelDetail;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conversation {
    private static final String TAG = Conversation.class.getSimpleName();

    private Map<String, UserProfile> suppliers;
    private List<Message> mMessages = new ArrayList();
    private boolean hasUnreadMessage;
    private boolean hasRecipientsUpdate;
    private MMXChannel channel;
    private Date lastActiveTime;
    private UserProfile owner;

    private Comparator<UserProfile> userProfileComparator = new Comparator<UserProfile>() {
        @Override public int compare(UserProfile lhs, UserProfile rhs) {
            return 0 - lhs.getDisplayName().compareTo(rhs.getDisplayName());
        }
    };

    public interface OnSendMessageListener {
        void onSuccessSend(Message message);

        void onFailure(Throwable throwable);
    }

    public Conversation() {
    }

    public Conversation(MMXChannel channel, List<UserProfile> suppliers, UserProfile owner) {
        this.channel = channel;
        this.owner = owner;
        lastActiveTime = new Date();

        for(UserProfile up : suppliers) {
            addSupplier(up);
        }
    }

    public Conversation(ChannelDetail channelDetail) {
        this.channel = channelDetail.getChannel();

        //Logger.debug(TAG, "channel subscribers ", channelDetail.getSubscribers(), " channel ", channel.getName());
        addChannelDetailData(channelDetail);
    }

    private Map<String, UserProfile> getSuppliers() {
        if (suppliers == null) {
            suppliers = new HashMap<>();
        }
        return suppliers;
    }

    public UserProfile getSupplier(String id) {
        return getSuppliers().get(id);
    }

    public List<UserProfile> getSuppliersList() {
        ArrayList<UserProfile> list = new ArrayList<>(getSuppliers().values());
        Collections.sort(list, userProfileComparator);
        return list;
    }

    public void addSupplier(UserProfile user) {
        if (user != null && !user.equals(User.getCurrentUser())) {
            if (getSuppliers().get(user.getUserIdentifier()) == null) {
                getSuppliers().put(user.getUserIdentifier(), user);
                hasRecipientsUpdate = true;
            }
        }
    }

    public boolean hasUnreadMessage() {
        return hasUnreadMessage;
    }

    public boolean hasRecipientsUpdate() {
        return hasRecipientsUpdate;
    }

    public void setHasRecipientsUpdate(boolean hasRecipientsUpdate) {
        this.hasRecipientsUpdate = hasRecipientsUpdate;
    }

    public void setHasUnreadMessage(boolean hasUnreadMessage) {
        this.hasUnreadMessage = hasUnreadMessage;
    }

    public Date getLastActiveTime() {
        if (lastActiveTime == null && channel != null) {
            return channel.getLastTimeActive();
        }
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public MMXChannel getChannel() {
        return channel;
    }

    public void setChannel(MMXChannel channel) {
        this.channel = channel;
    }

    public UserProfile getOwner() {
        return owner;
    }

    public List<Message> getMessages() {
        return mMessages;
    }

    public List<Message> getMessages(int offset, int limit) {
        if (limit > 0) {
            int size = mMessages.size();
            if (offset >= 0 && offset < size) {
                return (offset + limit) > size ? mMessages.subList(offset, size)
                    : mMessages.subList(offset, offset + limit);
            }
        } else {
            // return a copy
            return new ArrayList<>(mMessages);
        }

        return Collections.EMPTY_LIST;
    }

    public boolean addMessage(Message message, boolean isNewMessage) {
        if (!mMessages.contains(message)) {
            mMessages.add(message);

            addSupplier(message.getMmxMessage().getSender());

            if(isNewMessage) {
                setHasUnreadMessage(true);
            }

            lastActiveTime = new Date();

            return true;
        }

        return false;
    }

    public boolean addMessages(List<MMXMessage> mmxMessages) {
        return addMessages(mmxMessages, false);
    }

    public boolean addMessages(List<MMXMessage> mmxMessages, boolean isNew) {
        boolean addedResult = false;
        if(null != mmxMessages && ! mmxMessages.isEmpty()) {
            for(MMXMessage mmxMessage : mmxMessages) {
                boolean thisAddResult = addMessage(Message.createMessageFrom(mmxMessage), isNew);
                addedResult = addedResult || thisAddResult;
            }
        }

        return addedResult;
    }

    public boolean insertMessages(List<MMXMessage> mmxMessages) {
        boolean addedResult = false;
        if(null != mmxMessages && ! mmxMessages.isEmpty()) {
            for(MMXMessage mmxMessage : mmxMessages) {
                boolean thisAddResult = insertMessage(Message.createMessageFrom(mmxMessage));
                addedResult = addedResult || thisAddResult;
            }
        }

        return addedResult;
    }

    /**
     * Adds from channelDetail messages and users which is missing in conversation object
     * @param channelDetail
     */
    public void addChannelDetailData(ChannelDetail channelDetail) {
        owner = channelDetail.getOwner();
        for (UserProfile up : channelDetail.getSubscribers()) {
            if (owner == null && up.getUserIdentifier().equals(channel.getOwnerId())) {
                owner = up;
            }
            if (!up.getUserIdentifier().equals(User.getCurrentUserId())) {
                this.addSupplier(up);
            }
        }

        //Logger.debug(TAG, "channel messages ", channelDetail.getMessages(), " channel ", channel.getName());
        this.addMessages(channelDetail.getMessages());
    }

    public boolean mergeFrom(Conversation conversation) {
        boolean newMessageAdded = false;
        if(null != conversation) {
            for (UserProfile up : conversation.getSuppliers().values()) {
                if (owner == null && up.getUserIdentifier().equals(channel.getOwnerId())) {
                    owner = up;
                }
                if (!up.getUserIdentifier().equals(User.getCurrentUserId())) {
                    this.addSupplier(up);
                }
            }

            for (Message message : conversation.getMessages()) {
                newMessageAdded = newMessageAdded || this.addMessage(message, false);
            }
        }

        return newMessageAdded;
    }

    public void sendTextMessage(final String text, final OnSendMessageListener listener) {
        if (channel != null) {
            Map<String, String> content = Message.makeContent(text);
            sendMessage(content, listener);
        } else {
            throw new Error();
        }
    }

    public void sendLocation(Location location, final OnSendMessageListener listener) {
        if (channel != null) {
            Map<String, String> content = Message.makeContent(location);
            sendMessage(content, listener);
        } else {
            throw new Error();
        }
    }

    public void sendVideo(final String filePath, final String mimeType, final OnSendMessageListener listener) {
        Logger.debug(TAG, "sending video " + filePath);
        if (channel != null) {
            File file = new File(filePath);
            Attachment attachment = new Attachment(file, mimeType, file.getName(), "From " + UserHelper.getDisplayName(User.getCurrentUser()));
            Map<String, String> content = Message.makeVideoContent();
            sendMessage(content, attachment, listener);
        } else {
            throw new Error();
        }
    }

    public void sendPhoto(final String filePath, final String mimeType, final OnSendMessageListener listener) {
        Logger.debug(TAG, "sending photo " + filePath);
        if (channel != null) {
            File file = new File(filePath);
            Attachment attachment = new Attachment(file, mimeType, file.getName(), "From " + UserHelper.getDisplayName(User.getCurrentUser()));
            Map<String, String> content = Message.makePhotoContent();
            sendMessage(content, attachment, listener);
        } else {
            throw new Error();
        }
    }

    private void sendMessage(Map<String, String> content, final OnSendMessageListener listener) {
        sendMessage(content, null, listener);
    }

    private void sendMessage(Map<String, String> content, Attachment attachment, final OnSendMessageListener listener) {
        MMXMessage.Builder builder = new MMXMessage.Builder();
        builder.channel(channel).content(content);
        if (attachment != null) {
            builder.attachments(attachment);
        }
        final Message message = Message.createMessageFrom(builder.build());
        message.setCreationDate(DateHelper.localToUtc(new Date()));
        channel.publish(message.getMmxMessage(), new MMXChannel.OnFinishedListener<String>() {
            @Override
            public void onSuccess(String s) {
                Logger.debug("send message", "success");
                addMessage(message, false);
                listener.onSuccessSend(message);
            }

            @Override
            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                listener.onFailure(throwable);
            }
        });
    }

    public String ownerId() {
        if (owner == null) {
            return null;
        }
        return owner.getUserIdentifier();
    }

    @Override
    public String toString() {
        return new StringBuilder("conversation : {\n").append("channel : ").append(channel).append("\n")
            .append("messages : ").append(Arrays.toString(getMessages().toArray())).append("\n")
            .append("suppliers : ").append(Arrays.toString(getSuppliers().values().toArray())).append("\n")
            .append("}").toString();
    }

    private boolean insertMessage(Message message) {
        if (!mMessages.contains(message)) {
            mMessages.add(0, message);

            addSupplier(message.getMmxMessage().getSender());

            return true;
        }

        return false;
    }
}
