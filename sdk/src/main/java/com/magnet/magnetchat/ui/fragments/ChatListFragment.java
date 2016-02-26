package com.magnet.magnetchat.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.ui.activities.ChatActivity;
import com.magnet.magnetchat.ui.activities.ChooseUserActivity;
import com.magnet.magnetchat.ui.adapters.BaseConversationsAdapter;
import com.magnet.magnetchat.ui.adapters.HomeConversationsAdapter;
import com.magnet.mmx.client.api.ChannelDetail;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import java.util.List;

public class ChatListFragment extends BaseChannelsFragment {

    private static final String TAG = ChatListFragment.class.getSimpleName();

    private AlertDialog leaveDialog;

    LinearLayout llCreateMessage;
    ImageView ivCreateMessage;
    AppCompatTextView tvCreateMessage;
    FloatingActionButton fabCreateMessage;

    //private ChannelDetail primaryChannel;
    //private static final String PRIMARY_CHANNEL_TAG = "active";
    //private ChannelDetail secondaryChannel;

    @Override
    protected void onFragmentCreated(View containerView) {
        Log.d(TAG, "\n---------------------------------\nHomeFragment created\n---------------------------------\n");

        llCreateMessage = (LinearLayout) containerView.findViewById(R.id.llHomeCreateMsg);
        ivCreateMessage = (ImageView) containerView.findViewById(R.id.ivHomeCreateMsg);
        tvCreateMessage = (AppCompatTextView) containerView.findViewById(R.id.tvHomeCreateMsg);
        fabCreateMessage = (FloatingActionButton) containerView.findViewById(R.id.fabHomeCreateMessage);

        fabCreateMessage.setVisibility(View.VISIBLE);
        fabCreateMessage.setOnClickListener(this);

        setOnClickListeners(ivCreateMessage, tvCreateMessage);

        setHasOptionsMenu(true);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.fabHomeCreateMessage
           || v.getId() == R.id.ivHomeCreateMsg
           || v.getId() == R.id.tvHomeCreateMsg) {
                startActivity(ChooseUserActivity.getIntentToCreateChannel(getActivity()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menuHomeCreateConversation:
//                startActivity(ChooseUserActivity.getIntentToCreateChannel());
//                break;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override protected List<Conversation> getAllConversations() {
        return ChannelCacheManager.getInstance().getConversations();
    }

    @Override
    protected void showAllConversations() {
        super.showAllConversations();
    }

    @Override
    protected BaseConversationsAdapter createAdapter(List<Conversation> conversations) {
        HomeConversationsAdapter adapter = new HomeConversationsAdapter(getActivity(), conversations, new HomeConversationsAdapter.onClickHeaderListener() {
            @Override
            public void onClickEvent() {
                //Conversation conversation = addConversation(primaryChannel);
                //Intent i = ChatActivity.getIntentWithChannel(getActivity(), conversation);
                //if (null != i) {
                //    startActivity(i);
                //}
            }

            @Override
            public void onClickAskMagnet() {

            }
        });
        adapter.setOnConversationLongClick(new BaseConversationsAdapter.OnConversationLongClick() {
            @Override
            public void onLongClick(Conversation conversation) {
                showLeaveDialog(conversation);
            }
        });
        //if (primaryChannel != null) {
        //    adapter.setEventConversationEnabled(true);
        //}
        return adapter;
    }

    @Override
    protected void onConversationListIsEmpty(boolean isEmpty) {
        if (isEmpty) {
            llCreateMessage.setVisibility(View.VISIBLE);
        } else {
            llCreateMessage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSelectConversation(Conversation conversation) {
        startActivity(ChatActivity.getIntentWithChannel(getActivity(), conversation));
    }

    @Override
    protected void onReceiveMessage(MMXMessage mmxMessage) {
        if (mmxMessage != null && mmxMessage.getChannel() != null) {
            //MMXChannel channel = mmxMessage.getChannel();
            //if (!UserHelper.isMagnetSupportMember() && channel.getName().equalsIgnoreCase(ChannelHelper.ASK_MAGNET)) {
            //    askMagnetView.setUnreadMessage(true);
            //}
        }
        if (llCreateMessage.getVisibility() == View.VISIBLE) {
            onConversationListIsEmpty(false);
        }
    }

    @Override
    public void onPause() {
        if (leaveDialog != null && leaveDialog.isShowing()) {
            leaveDialog.dismiss();
        }
        super.onPause();
    }


    private void showLeaveDialog(final Conversation conversation) {
        if (leaveDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    leaveDialog.dismiss();
                }
            });
            leaveDialog = builder.create();
            leaveDialog.setMessage("Are you sure that you want to leave conversation");
        }
        leaveDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Leave", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //setProgressBarVisibility(View.VISIBLE);
                ChannelHelper.unsubscribeFromChannel(conversation, new ChannelHelper.OnLeaveChannelListener() {
                    @Override
                    public void onSuccess() {
                        //setProgressBarVisibility(View.GONE);
                        ChannelCacheManager.getInstance().removeConversation(conversation.getChannel().getName());
                        showAllConversations();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        //setProgressBarVisibility(View.GONE);
                    }
                });
                leaveDialog.dismiss();
            }
        });
        leaveDialog.show();
    }

}
