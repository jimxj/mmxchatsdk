package com.magnet.magnetchat.helpers;

import android.content.Intent;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.util.Logger;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.MaxCore;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.ChannelDetail;
import com.magnet.mmx.client.api.ChannelDetailOptions;
import com.magnet.mmx.client.api.ChannelMatchType;
import com.magnet.mmx.client.api.ListResult;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelHelper {
    private static final String TAG = ChannelHelper.class.getSimpleName();
    public static final String ACTION_ADDED_CONVERSATION = "com.magnet.magnetchat.ADDED_CONVERSATION";

    public interface OnReadChannelDetailListener {
        void onSuccessFinish(Conversation conversation);

        void onFailure(Throwable throwable);
    }

    public interface OnCreateChannelListener {
        void onSuccessCreated(MMXChannel channel);

        void onChannelExists(MMXChannel channel);

        void onFailureCreated(Throwable throwable);
    }

    public interface OnLeaveChannelListener {
        void onSuccess();

        void onFailure(Throwable throwable);
    }

    public interface OnFindChannelByUsersListener {
        void onSuccessFound(List<MMXChannel> mmxChannels);

        void onFailure(Throwable throwable);
    }

    public interface OnAddUserListener {
        void onSuccessAdded();

        void onUserSetExists(String channelSetName);

        void onWasAlreadyAdded();

        void onFailure(Throwable throwable);
    }

    public static void getAllSubscriptionDetails(final MMXChannel.OnFinishedListener<List<ChannelDetail>> listener) {
        MMXChannel.getAllSubscriptions(new MMXChannel.OnFinishedListener<List<MMXChannel>>() {
            @Override
            public void onSuccess(final List<MMXChannel> channels) {
                Logger.debug(TAG, "getAllSubscriptionDetails success : " + channels);
                //ChannelCacheManager.getInstance().resetConversations();
                if (null != channels && channels.size() > 0) {
                    getChannelDetails(channels, listener);
                } else {
                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                }
            }

            @Override
            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                Logger.error(TAG, throwable, "getAllSubscriptionDetails failed");
                if (listener != null) {
                    listener.onFailure(failureCode, throwable);
                }
            }
        });
    }


    //public static void readChannelInfo(final MMXChannel channel, final OnReadChannelInfoListener listener) {
    //    if (channel == null) {
    //        if (null != listener) {
    //            listener.onFailure(new Exception("channel shouldn't be null"));
    //        }
    //        return;
    //    }
    //    getChannelDetails(Arrays.asList(channel), listener);
    //}

    public static void getChannelDetails(final MMXChannel channel, final OnReadChannelDetailListener listener) {
        MMXChannel.getChannelDetail(Arrays.asList(channel),
            new ChannelDetailOptions.Builder().numOfMessages(50).numOfSubcribers(10).build(),
            new MMXChannel.OnFinishedListener<List<ChannelDetail>>() {
                @Override public void onSuccess(List<ChannelDetail> channelDetails) {
                    if (null != channelDetails && 1 == channelDetails.size()) {
                        Conversation conversation = new Conversation(channelDetails.get(0));
                        ChannelCacheManager.getInstance().addConversation(conversation);

                        if(null != listener) {
                            listener.onSuccessFinish(conversation);
                        }

                        //Without this new conversation isn't shown at once
                        MaxCore.getApplicationContext().sendBroadcast(new Intent(ACTION_ADDED_CONVERSATION));
                    } else {
                        handleError("empty result", new Exception("empty result"));
                    }
                }

                @Override
                public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                    handleError(failureCode.toString(), throwable);
                }

                private void handleError(String message, Throwable throwable) {
                    Logger.error(TAG, "Can't load conversation for new channel : "
                        + channel
                        + " due to "
                        + message);

                    if(null != listener) {
                        listener.onFailure(throwable);
                    }
                }
            });
    }

    public static void getChannelDetails(final List<MMXChannel> channels, final MMXChannel.OnFinishedListener<List<ChannelDetail>> listener) {
        MMXChannel.getChannelDetail(channels,
                new ChannelDetailOptions.Builder().numOfMessages(50).numOfSubcribers(10).build(),
                listener);
        //MMXChannel.getChannelDetail(channels,
        //        new ChannelDetailOptions.Builder().numOfMessages(50).numOfSubcribers(10).build(),
        //        new MMXChannel.OnFinishedListener<List<ChannelDetail>>() {
        //            @Override
        //            public void onSuccess(List<ChannelDetail> channelDetails) {
        //                Logger.debug(TAG, "getChannelDetail successfully ");
        //                Conversation lastConversation = null;
        //                for (int i = 0; i < channelDetails.size(); i++) {
        //                    final ChannelDetail channelDetail = channelDetails.get(i);
        //                    final MMXChannel channel = channelDetail.getChannel();
        //                    Conversation conversation = ChannelCacheManager.getInstance().getConversationByChannel(channel);
        //                    if (conversation == null) {
        //                        conversation = new Conversation(channelDetail);
        //                        ChannelCacheManager.getInstance().addConversation(channel.getName(), conversation);
        //                    } else {
        //                        conversation.addChannelDetailData(channelDetail);
        //                    }
        //                    lastConversation = conversation;
        //                }
        //
        //                if (listener != null) {
        //                    listener.onSuccessFinish(lastConversation);
        //                }
        //                //CurrentApplication.getInstance().sendBroadcast(new Intent(ACTION_ADDED_CONVERSATION));
        //            }
        //
        //            @Override
        //            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
        //                Logger.error(TAG, "getChannelDetail failed : ", throwable);
        //                if (listener != null) {
        //                    listener.onFailure(throwable);
        //                }
        //            }
        //        });
    }

    public static void addUserToConversation(final Conversation conversation, final List<UserProfile> userList, final OnAddUserListener listener) {
        Set<User> userSet = new HashSet<>();
        for (UserProfile user : userList) {
            if (null == conversation.getSupplier(user.getUserIdentifier())) {
                userSet.add((User) user);
            }
        }
        conversation.getChannel().addSubscribers(userSet, new MMXChannel.OnFinishedListener<List<String>>() {
            @Override
            public void onSuccess(List<String> strings) {
                Logger.debug("add user", "success");
                for (UserProfile user : userList) {
                    conversation.addSupplier(user);
                }

                if (listener != null) {
                    listener.onSuccessAdded();
                }
            }

            @Override
            public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                Logger.error("add user", throwable);
                if (listener != null) {
                    listener.onFailure(throwable);
                }
            }
        });
    }

    public static String getNameForChannel() {
        return DateHelper.getDateWithoutSpaces();
    }

    public static void createChannelForUsers(final List<String> userIds, final OnCreateChannelListener listener) {
        findChannelByUsers(userIds, new OnFindChannelByUsersListener() {
            @Override
            public void onSuccessFound(List<MMXChannel> mmxChannels) {
                if (mmxChannels.size() == 1) { // Use existing one if only one found
                    Logger.debug("channel with same subscribers exists, use it");
                    if (listener != null)
                        listener.onChannelExists(mmxChannels.get(0));
                } else {
                    Set<String> users = new HashSet<>();
                    users.addAll(userIds);
                    String summary = User.getCurrentUser().getUserName();
                    MMXChannel.create(getNameForChannel(), summary, false, MMXChannel.PublishPermission.SUBSCRIBER, users, new MMXChannel.OnFinishedListener<MMXChannel>() {
                        @Override
                        public void onSuccess(MMXChannel channel) {
                            Logger.debug("create conversation", "success");
                            if (listener != null) {
                                listener.onSuccessCreated(channel);
                            }
                        }

                        @Override
                        public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                            Logger.error("create conversation", throwable);
                            if (listener != null) {
                                listener.onFailureCreated(throwable);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (listener != null) listener.onFailureCreated(throwable);
            }
        });
    }

    public static void findChannelByUsers(List<String> userIds, final OnFindChannelByUsersListener listener) {
        User.getUsersByUserIds(userIds, new ApiCallback<List<User>>() {
            @Override
            public void success(List<User> userList) {
                MMXChannel.findChannelsBySubscribers(new HashSet<>(userList), ChannelMatchType.EXACT_MATCH, new MMXChannel.OnFinishedListener<ListResult<MMXChannel>>() {
                    @Override
                    public void onSuccess(ListResult<MMXChannel> mmxChannelListResult) {
                        if (listener != null) {
                            listener.onSuccessFound(mmxChannelListResult.items);
                        }
                    }

                    @Override
                    public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                        Logger.error("find chan by users", throwable);
                        if (listener != null) {
                            listener.onFailure(throwable);
                        }
                    }
                });
            }

            @Override
            public void failure(ApiError apiError) {
                Logger.error("find users by ids", apiError);
                if (listener != null) {
                    listener.onFailure(apiError);
                }
            }
        });
    }

    public static void receiveMessage(final MMXMessage mmxMessage) {
        Logger.debug("new message");
        MMXChannel channel = mmxMessage.getChannel();
        if (channel != null) {
            String channelName = channel.getName();
            Conversation conversation;
            conversation = ChannelCacheManager.getInstance().getConversationByName(channelName);

            checkMessageConversation(mmxMessage, conversation);
        }
    }

    private static void checkMessageConversation(final MMXMessage mmxMessage, Conversation conversation) {
        if (conversation != null) {
            conversation.addMessage(Message.createMessageFrom(mmxMessage));
        } else {
            getChannelDetails(mmxMessage.getChannel(), new OnReadChannelDetailListener() {
                @Override public void onSuccessFinish(Conversation conversation) {

                }

                @Override public void onFailure(Throwable throwable) {

                }
            });
        }
    }

    public static void unsubscribeFromChannel(final Conversation conversation, final OnLeaveChannelListener listener) {
        final MMXChannel channel = conversation.getChannel();
        if (channel != null) {
            channel.unsubscribe(new MMXChannel.OnFinishedListener<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    ChannelCacheManager.getInstance().removeConversation(channel.getName());
                    Logger.debug("unsubscribe", "success");
                    if (listener != null)
                        listener.onSuccess();
                }

                @Override
                public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                    Logger.error("unsubscribe", throwable);
                    if (listener != null)
                        listener.onFailure(throwable);
                }
            });
        }
    }

    public static void deleteChannel(final Conversation conversation, final OnLeaveChannelListener listener) {
        final MMXChannel channel = conversation.getChannel();
        if (channel != null) {
            channel.delete(new MMXChannel.OnFinishedListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    ChannelCacheManager.getInstance().removeConversation(channel.getName());
                    Logger.debug("delete", "success");
                    if (listener != null)
                        listener.onSuccess();
                }

                @Override
                public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                    Logger.error("delete", throwable);
                    if (listener != null)
                        listener.onFailure(throwable);
                }
            });
        }
    }

}
