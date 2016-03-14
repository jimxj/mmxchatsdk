/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.views.helpers;

import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.max.android.UserProfile;

public class UserViewHelper {

  public static String getUserGroup(UserProfile user) {
    char letter = ' ';
    String str = UserHelper.getUserNameToCompare(user);
    if (str.length() > 0) {
      letter = str.charAt(0);
    }
    return String.valueOf(letter);
  }

  public static boolean shouldShowGroup(UserProfile user, UserProfile previousUser) {
    return null == previousUser || !getUserGroup(user).equalsIgnoreCase(getUserGroup(previousUser));
  }
}
