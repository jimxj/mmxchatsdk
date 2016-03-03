/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.callbacks;

import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;

public interface NewMessageProcessListener {
  void onProcessSuccess(Conversation conversation, Message message, boolean isNewChat);
  void onProcessFailure(Throwable throwable);
}
