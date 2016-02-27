/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.chatsdk.sample;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.core.managers.InternetConnectionManager;
import com.magnet.magnetchat.core.managers.MMXManager;
import com.magnet.magnetchat.core.managers.SharedPreferenceManager;
import com.magnet.magnetchat.core.managers.TypeFaceManager;

public class ChatSampleApplication extends MultiDexApplication {

  private static ChatSampleApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    onManagersInitialization();
  }

  /**
   * Method which provide the enabling of the Multidex
   *
   * @param base
   */
  public void attachBaseContext(Context base) {
    MultiDex.install(base);
    super.attachBaseContext(base);
  }

  /**
   * Method which provide the Managers initialization
   */
  private void onManagersInitialization() {
    MMXManager.getInstance(this);
    SharedPreferenceManager.getInstance(this);
    InternetConnectionManager.getInstance(this);
    ChannelCacheManager.getInstance();
  }


  public static ChatSampleApplication getInstance() {
    return instance;
  }


  /**
   * Method which provide the getting of the resource string
   *
   * @param resID resource string ID
   * @return string value from resource
   */
  public String getResourceString(int resID) {
    return getResources().getString(resID);
  }

}