/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.views.helpers;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.Glide;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.helpers.DateHelper;
import com.magnet.magnetchat.model.Message;
import com.magnet.max.android.Attachment;
import com.magnet.max.android.User;
import com.magnet.max.android.util.StringUtil;
import java.util.Date;

public class MessageViewHelper {

  private static final String TAG = "MessageViewHelper";

  public static boolean isMessageFromMe(Message message) {
    return message.getSender() == null || StringUtil.isStringValueEqual(User.getCurrentUserId(), message.getSender().getUserIdentifier());
  }

  public static String getDateString(Message message, Message previous) {
    Date date;
    Date previousDate = null;
    if (message.getCreateTime() == null) {
      date = new Date();
    } else {
      date = DateHelper.utcToLocal(message.getCreateTime());
    }
    if (previous != null) {
      previousDate = DateHelper.utcToLocal(previous.getCreateTime());
    }
    String msgDate = DateHelper.getMessageDateTime(date);
    String previousMsgDate = null;
    if (previousDate != null) {
      previousMsgDate = DateHelper.getMessageDateTime(previousDate);
    }
    if (!msgDate.equalsIgnoreCase(previousMsgDate)) {
      return msgDate;
    } else {
      return null;
    }
  }

  public static String getMessageImageUrl(Message message) {
    String result = null;
    if(message.getType() == Message.TYPE_PHOTO) {
      final Attachment attachment = message.getAttachment();
      if (attachment != null) {
        try {
          result = attachment.getDownloadUrl();
        } catch (IllegalStateException e) {
          Log.d(TAG, "Attachment is not ready2", e);
        }
      }
    } else if(message.getType() == Message.TYPE_MAP) {
      result = getMessageImageUrl(message);
    }

    return result;
  }

  private static String getLocationUrl(Message message) {
    return "http://maps.google.com/maps/api/staticmap?center=" + message.getLatitudeLongitude() + "&zoom=18&size=700x300&sensor=false&markers=color:blue%7Clabel:S%7C" + message.getLatitudeLongitude();
  }
}
