package com.magnet.magnetchat.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SearchView;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.api.ChatList;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.ui.activities.ChatActivity;
import com.magnet.magnetchat.ui.activities.ChooseUserActivity;
import com.magnet.magnetchat.ui.adapters.ChatsAdapter;
import com.magnet.magnetchat.ui.custom.CustomSearchView;
import com.magnet.magnetchat.ui.views.DividerItemDecoration;
import com.magnet.magnetchat.util.Logger;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends BaseFragment implements ChatList.View {

    private static String TAG = ChatListFragment.class.getSimpleName();

    RecyclerView conversationsList;
    SwipeRefreshLayout swipeContainer;

    AlertDialog leaveDialog;

    FloatingActionButton fabCreateMessage;

    private List<Conversation> conversations;
    private ChatsAdapter adapter;

    private boolean isLoadingWhenCreating = false;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    protected void onCreateFragment(View containerView) {

        conversationsList = (RecyclerView) containerView.findViewById(R.id.homeConversationsList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        conversationsList.setHasFixedSize(true);
        conversationsList.setLayoutManager(layoutManager);
        conversationsList.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.divider));

        // Setup refresh listener which triggers new data loading
        swipeContainer = (SwipeRefreshLayout) containerView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getConversations();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);

        fabCreateMessage = (FloatingActionButton) containerView.findViewById(R.id.fabHomeCreateMessage);
        fabCreateMessage.setVisibility(View.VISIBLE);
        fabCreateMessage.setOnClickListener(this);

        setHasOptionsMenu(true);

        swipeContainer.post(new Runnable() {
            @Override public void run() {
                swipeContainer.setRefreshing(true);
                getConversations();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoadingWhenCreating && ChannelCacheManager.getInstance().isConversationListUpdated()) {
            showAllConversations();
            ChannelCacheManager.getInstance().resetConversationListUpdated();
        }
        MMX.registerListener(eventListener);
        getActivity().registerReceiver(onAddedConversation, new IntentFilter(ChannelHelper.ACTION_ADDED_CONVERSATION));
    }

    @Override
    public void onPause() {
        MMX.unregisterListener(eventListener);
        getActivity().unregisterReceiver(onAddedConversation);

        if (leaveDialog != null && leaveDialog.isShowing()) {
            leaveDialog.dismiss();
        }

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final CustomSearchView search = (CustomSearchView) menu.findItem(R.id.menu_search).getActionView();
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchMessage(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        hideKeyboard();
                        showAllConversations();
                    }
                    return false;
                }
            });

            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    search.onActionViewCollapsed();
                    showAllConversations();
                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected void getConversations() {
        ChannelHelper.readConversations(readChannelInfoListener);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == R.id.fabHomeCreateMessage) {
            showNewChat();
        }
    }

    protected List<Conversation> getAllConversations() {
        return ChannelCacheManager.getInstance().getConversations();
    }

    protected void showAllConversations() {
        showList(getAllConversations());
    }

    protected void updateList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void showList(List<Conversation> conversationsToShow) {
        if(null != getActivity()) {
            if (adapter == null) {
                conversations = new ArrayList<>(conversationsToShow);
                adapter = new ChatsAdapter(getActivity(), conversations);
                adapter.setOnConversationClick(new ChatsAdapter.OnConversationClick() {
                    @Override
                    public void onClick(Conversation conversation) {
                        if (conversation != null) {
                            Log.d(TAG, "Channel " + conversation.getChannel().getName() + " is selected");
                            showChatDetails(conversation);
                        }
                    }
                });
                adapter.setOnConversationLongClick(new ChatsAdapter.OnConversationLongClick() {
                    @Override
                    public void onLongClick(Conversation conversation) {
                        showLeaveDialog(conversation);
                    }
                });
                conversationsList.setAdapter(adapter);
            } else {
                conversations.clear();
                conversations.addAll(conversationsToShow);
                adapter.notifyDataSetChanged();
            }
        } else {
            Log.w(TAG, "Fragment is detached, won't update list");
        }
    }

    protected void searchMessage(final String query) {
        final List<Conversation> searchResult = new ArrayList<>();
        for (Conversation conversation : getAllConversations()) {
            for (UserProfile userProfile : conversation.getSuppliersList()) {
                if (userProfile.getDisplayName() != null && userProfile.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                    searchResult.add(conversation);
                    break;
                }
            }
        }
        if (searchResult.isEmpty()) {
            Utils.showMessage(getActivity(), "Nothing found");
        }
        showList(searchResult);
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

    private ChannelHelper.OnReadChannelInfoListener readChannelInfoListener = new ChannelHelper.OnReadChannelInfoListener() {
        @Override
        public void onSuccessFinish(Conversation lastConversation) {
            finishGetChannels();
            showAllConversations();
            if (conversations == null || conversations.size() == 0) {
                //onConversationListIsEmpty(true);
                Log.w("read channels", "No conversation is available");
            } else {
                //onConversationListIsEmpty(false);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            finishGetChannels();
        }

        private void finishGetChannels() {
            isLoadingWhenCreating = false;
            swipeContainer.setRefreshing(false);
        }
    };

    private MMX.EventListener eventListener = new MMX.EventListener() {
        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            Logger.debug(TAG, "onMessageReceived");
            showAllConversations();
            //onReceiveMessage(mmxMessage);
            return false;
        }

        @Override
        public boolean onMessageAcknowledgementReceived(User from, String messageId) {
            Logger.debug(TAG, "onMessageAcknowledgementReceived");
            updateList();
            return false;
        }

        @Override
        public boolean onInviteReceived(MMXChannel.MMXInvite invite) {
            Logger.debug(TAG, "onInviteReceived");
            updateList();
            return false;
        }

        @Override
        public boolean onInviteResponseReceived(MMXChannel.MMXInviteResponse inviteResponse) {
            Logger.debug(TAG, "onInviteResponseReceived");
            updateList();
            return false;
        }

        @Override
        public boolean onMessageSendError(String messageId, MMXMessage.FailureCode code, String text) {
            Logger.debug("onMessageSendError");
            updateList();
            return false;
        }
    };

    private BroadcastReceiver onAddedConversation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showAllConversations();
        }
    };

    @Override public void setProgressIndicator(boolean active) {
        swipeContainer.setRefreshing(active);
    }

    @Override public void showChatList(List<Conversation> chatList) {
        showList(chatList);
    }

    @Override public void showNewChat() {
        startActivity(ChooseUserActivity.getIntentToCreateChannel(getActivity()));
    }

    @Override public void showChatDetails(Conversation conversation) {
        startActivity(ChatActivity.getIntentWithChannel(getActivity(), conversation));
    }
}
