/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.mvp.presenters;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.helpers.FileHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.mvp.api.ChatContract;
import com.magnet.magnetchat.ui.activities.ChatDetailsActivity;
import com.magnet.magnetchat.util.Logger;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.Max;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.MMXChannel;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatPresenterImpl implements ChatContract.Presenter {
  protected static final String TAG = "ChatPresenterImpl";

  protected final ChatContract.View mView;
  protected WeakReference<Activity> mActivityRef;

  protected Conversation mCurrentConversation;
  protected final List<UserProfile> mRecipients;

  public ChatPresenterImpl(ChatContract.View view, Conversation conversation, Activity activity) {
    this.mView = view;
    this.mCurrentConversation = conversation;
    this.mRecipients = conversation.getSuppliersList();
    this.mActivityRef = new WeakReference<>(activity);

    mView.showMessages(conversation.getMessages());
    mView.showRecipients(conversation.getSuppliersList());
  }

  public ChatPresenterImpl(ChatContract.View view, final ArrayList<UserProfile> recipients, Activity activity) {
    this.mView = view;
    this.mRecipients = recipients;
    this.mActivityRef = new WeakReference<>(activity);

    mView.showRecipients(mRecipients);

    List<String> userIds = new ArrayList<>(recipients.size());
    for(UserProfile up : recipients) {
      userIds.add(up.getUserIdentifier());
    }
    mView.setProgressIndicator(true);
    ChannelHelper.createChannelForUsers(userIds, new ChannelHelper.OnCreateChannelListener() {
      @Override
      public void onSuccessCreated(MMXChannel channel) {
        addNewConversation(new Conversation(channel, recipients, User.getCurrentUser()));
      }

      @Override
      public void onChannelExists(MMXChannel channel) {
        mCurrentConversation = ChannelCacheManager.getInstance().getConversationByName(channel.getName());
        if (null == mCurrentConversation) {
          ChannelHelper.getChannelDetails(channel, new ChannelHelper.OnReadChannelDetailListener() {
            @Override public void onSuccessFinish(Conversation conversation) {
              addNewConversation(conversation);
            }

            @Override public void onFailure(Throwable throwable) {
              mView.setProgressIndicator(false);
            }
          });
        } else {
          mView.setProgressIndicator(false);
        }
      }

      @Override
      public void onFailureCreated(Throwable throwable) {
        Utils.showMessage("Can't create conversation");
        mView.setProgressIndicator(false);
      }

      private void addNewConversation(Conversation conversation) {
        mCurrentConversation = conversation;
        ChannelCacheManager.getInstance().addConversation(mCurrentConversation);
        ChannelCacheManager.getInstance().setConversationListUpdated();
        mView.setProgressIndicator(false);
        mView.showMessages(mCurrentConversation.getMessages());
      }
    });
  }

  @Override public void onLoadMessages(boolean forceUpdate) {
    if(forceUpdate) {
      //TODO :
    } else {
      if (null != mCurrentConversation && mCurrentConversation.hasUnreadMessage()) {
        mView.showMessages(mCurrentConversation.getMessages());
        mCurrentConversation.setHasUnreadMessage(false);
      }
    }
  }

  @Override public void onLoadRecipients(boolean forceUpdate) {
    if(forceUpdate) {
      //TODO :
    } else {
      if(null != mCurrentConversation && mCurrentConversation.hasRecipientsUpdate()) {
        mView.showRecipients(mCurrentConversation.getSuppliersList());
        mCurrentConversation.setHasRecipientsUpdate(false);
      }
    }
  }

  @Override public void onNewMessage(Message message) {
    if(null != mCurrentConversation) {
      mCurrentConversation.addMessage(message);
      mCurrentConversation.setHasUnreadMessage(false);

      mView.showNewMessage(message);
    }
  }

  @Override public void onReadMessage() {
    if(null != mCurrentConversation) {
      mCurrentConversation.setHasUnreadMessage(false);
    }
  }

  @Override public void onSendText(String text) {
    if (mCurrentConversation != null) {
      mView.setSendEnabled(false);
      mCurrentConversation.sendTextMessage(text, sendMessageListener);
    }
  }

  @Override public void onSendImages(Uri[] uris) {
    if (mCurrentConversation != null) {
      if (uris.length > 0) {
        for (Uri uri : uris) {
          mView.setProgressIndicator(true);
          mView.setSendEnabled(false);

          String filePath = uri.toString();
          mCurrentConversation.sendPhoto(filePath,
              FileHelper.getMimeType(Max.getApplicationContext(), uri, filePath, Message.FILE_TYPE_PHOTO),
              sendMessageListener);
        }
      }
    }
  }

  @Override public void onSendLocation(Location location) {
    if (mCurrentConversation != null) {
      mView.setSendEnabled(false);
      mCurrentConversation.sendLocation(location, sendMessageListener);
    }
  }

  @Override public void onMessageClick(Message message) {
    onOpenAttachment(message);
  }

  @Override public void onMessageLongClick(Message message) {

  }

  @Override public void onChatDetials() {
    if (mCurrentConversation != null && null != mActivityRef.get()) {
      mActivityRef.get().startActivity(ChatDetailsActivity.createIntentForChannel(mActivityRef.get(), mCurrentConversation));
    }
  }

  @Override public Conversation getCurrentConversation() {
    return mCurrentConversation;
  }

  /**
   * Method which provide the opening of the attachment
   *
   * @throws Exception
   */
  private void onOpenAttachment(Message message) {
    if (message.getType() != null) {
      switch (message.getType()) {
        case Message.TYPE_MAP:
          mView.showLocation(message);
          break;
        case Message.TYPE_PHOTO:
          mView.showImage(message);
          break;
        default:
          break;
      }
    }
  }

  private Conversation.OnSendMessageListener sendMessageListener = new Conversation.OnSendMessageListener() {
    @Override
    public void onSuccessSend(Message message) {
      mView.setProgressIndicator(false);
      mView.setSendEnabled(true);

      ChannelCacheManager.getInstance().getMessagesToApproveDeliver().put(message.getMessageId(), message);
      if (message.getType() != null && message.getType().equals(Message.TYPE_TEXT)) {
        mView.clearInput();
      }
      onNewMessage(message);
    }

    @Override
    public void onFailure(Throwable throwable) {
      mView.setProgressIndicator(false);
      mView.setSendEnabled(true);
      mView.setProgressIndicator(false);
      Logger.error(TAG, "send message error", throwable);
      Utils.showMessage("Can't send message");
    }
  };
}
