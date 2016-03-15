/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.views.helpers;

import android.util.Log;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.model.Chat;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.max.android.util.StringUtil;

public class ChatViewHelper {
  private static final String TAG = "ChatViewHelper";

  /**
   * Get last message for conversation
   *
   * @param chat
   * @return empty line, if conversation has not any massage
   */
  public static String getLastMessage(Chat chat) {
    String lastMessage = chat.getLastMessageSummary();
    if(StringUtil.isEmpty(lastMessage)) {
      lastMessage = "No message";
    }
    return lastMessage;
  }


  public static String getTitle(Chat chat) {
    String title = null;
    if(!chat.getSubscribers().isEmpty()) {
      title = UserHelper.getDisplayNames(chat.getSubscribers());
    } else {
      title = UserHelper.getDisplayName(User.getCurrentUser());
    }
    if(StringUtil.isEmpty(title)) {
      Log.e(TAG, "----------title is empty for Chat : " + chat);
    }

    return title;
  }


  public static UserProfile getSingleRecipient(Chat chat) {
    UserProfile result = null;
    if(0 == chat.getSubscribers().size()) {
      result = User.getCurrentUser();
    } else if(1 == chat.getSubscribers().size()) {
      result = chat.getSubscribers().get(0);
    }

    return result;
  }
}
