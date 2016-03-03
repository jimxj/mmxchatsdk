package com.magnet.magnetchat.mvp.api;

import com.magnet.max.android.UserProfile;
import java.util.List;

/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
public interface ChatDetailsContract {

  interface View {
    /**
     * Show or hide the progress bar
     *
     * @param active
     */
    void setProgressIndicator(boolean active);

    void showRecipients(List<UserProfile> recipients);

    void finishDetails();
  }

  interface Presenter {

    /**
     * Method which provide to getting of the reading channels
     */
    void onLoadRecipients(boolean forceUpdate);

    void onAddRecipients();
  }
}
