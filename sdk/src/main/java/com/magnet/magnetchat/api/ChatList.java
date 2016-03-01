package com.magnet.magnetchat.api;

import com.magnet.magnetchat.model.Conversation;
import java.util.List;

/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
public interface ChatList {

  interface View {

    void setProgressIndicator(boolean active);

    void showChatList(List<Conversation> chatList);

    void showNewChat();

    void showChatDetails(Conversation conversation);
  }

  interface UserActionsListener {

    void onLoadChatList(boolean forceUpdate);

    void onAddChat();

    void onShowChatDetails();

  }
}
