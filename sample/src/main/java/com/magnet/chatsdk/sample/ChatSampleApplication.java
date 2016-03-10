/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.chatsdk.sample;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.magnet.magnetchat.ChatSDK;
import com.magnet.max.android.Max;
import com.magnet.max.android.config.MaxAndroidPropertiesConfig;

public class ChatSampleApplication extends MultiDexApplication {

  private static ChatSampleApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();

    //Initialization of the MagnetMax
    Max.init(this, new MaxAndroidPropertiesConfig(this, com.magnet.chatsdk.sample.R.raw.magnetmax));

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
    ChatSDK.init(this);
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
