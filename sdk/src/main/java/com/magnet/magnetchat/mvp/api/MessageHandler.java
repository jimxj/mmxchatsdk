/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.mvp.api;

import com.magnet.magnetchat.model.Message;

public interface MessageHandler {

  void onSenderClick(Message message);
  void onMessageBodyClick(Message message);
  void onMessageBodyLongClick(Message message);

}
