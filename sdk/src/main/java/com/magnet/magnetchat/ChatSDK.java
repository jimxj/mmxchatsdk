package com.magnet.magnetchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.util.Logger;
import com.magnet.max.android.Max;
import com.magnet.max.android.User;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;

/**
 * Created by dlernatovich on 2/8/16.
 * Class which provide to management of the MMX functional
 */
public class ChatSDK {

    public static void init(Context context) {
        MMX.registerListener(eventListener);
        MMX.registerWakeupBroadcast(context, new Intent("MMX_WAKEUP_ACTION"));
        com.magnet.mmx.client.common.Log.setLoggable(null, com.magnet.mmx.client.common.Log.VERBOSE);
    }

    public static void messageNotification(String channelName, String fromUserName) {
        PendingIntent intent = PendingIntent.getActivity(Max.getApplicationContext(), 0, new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setPackage(Max.getApplicationContext().getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(Max.getApplicationContext())
                .setAutoCancel(true)
                .setSmallIcon(Max.getApplicationContext().getApplicationInfo().icon)
                .setContentTitle("New message is available")
                .setContentInfo(fromUserName)
                .setContentIntent(intent).build();
        NotificationManager manager = (NotificationManager) Max.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(channelName, 12345, notification);
        Vibrator v = (Vibrator) Max.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    private static MMX.EventListener eventListener = new MMX.EventListener() {
        @Override
        public boolean onLoginRequired(MMX.LoginReason reason) {
            Logger.debug("login required", reason.name());
            UserHelper.checkAuthentication(null);
            return false;
        }

        @Override
        public boolean onInviteReceived(MMXChannel.MMXInvite invite) {
            Logger.debug("invite to", invite.getInviteInfo().getChannel().getName());
            return super.onInviteReceived(invite);
        }

        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            Logger.debug("onMessageReceived", mmxMessage);
            ChannelCacheManager.getInstance().handleIncomingMessage(mmxMessage, null);
            if ((mmxMessage.getSender() != null)
                    && (!mmxMessage.getSender().getUserIdentifier().equals(User.getCurrentUserId()))) {
                if (mmxMessage.getChannel() != null) {
                    messageNotification(mmxMessage.getChannel().getName(), mmxMessage.getSender().getDisplayName());
                } else {
                    messageNotification("", mmxMessage.getSender().getDisplayName());
                }
            }
            return false;
        }

        @Override
        public boolean onMessageAcknowledgementReceived(User from, String messageId) {
            ChannelCacheManager.getInstance().approveMessage(messageId);
            return false;
        }
    };
}