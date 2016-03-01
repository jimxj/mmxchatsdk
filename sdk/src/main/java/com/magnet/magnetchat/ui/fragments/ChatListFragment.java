package com.magnet.magnetchat.ui.fragments;

import android.content.DialogInterface;
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
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.mvp.presenters.ChatListFragmentPresenter;
import com.magnet.magnetchat.mvp.presenters.ChatListFragmentPresenterImpl;
import com.magnet.magnetchat.mvp.views.ChatListFragmentView;
import com.magnet.magnetchat.ui.activities.ChatActivity;
import com.magnet.magnetchat.ui.activities.ChooseUserActivity;
import com.magnet.magnetchat.ui.adapters.ChatsAdapter;
import com.magnet.magnetchat.ui.custom.CustomSearchView;
import com.magnet.magnetchat.ui.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends BaseFragment implements ChatListFragmentView {

    private RecyclerView conversationsList;
    private SwipeRefreshLayout swipeContainer;

    private AlertDialog leaveDialog;

    private FloatingActionButton fabCreateMessage;

    private List<Conversation> conversations;
    private ChatsAdapter adapter;

    private ChatListFragmentPresenter presenter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    protected void onCreateFragment(View containerView) {

        presenter = new ChatListFragmentPresenterImpl(this);

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
                presenter.getConversations();
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);

        fabCreateMessage = (FloatingActionButton) containerView.findViewById(R.id.fabHomeCreateMessage);
        fabCreateMessage.setVisibility(View.VISIBLE);
        fabCreateMessage.setOnClickListener(this);

        setHasOptionsMenu(true);

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
                presenter.getConversations();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        presenter.onPause();
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
                    presenter.searchMessage(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        hideKeyboard();
                        presenter.showAllConversations();
                    }
                    return false;
                }
            });

            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    search.onActionViewCollapsed();
                    presenter.showAllConversations();
                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public List<Conversation> getConversations() {
        return conversations;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.fabHomeCreateMessage) {
            showNewChat();
        }
    }

    @Override
    public void updateList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showList(List<Conversation> conversations) {
        if (null != getActivity()) {
            if (adapter == null) {
                this.conversations = new ArrayList<>(conversations);
                adapter = new ChatsAdapter(getActivity(), this.conversations);
                adapter.setOnConversationClick(new ChatsAdapter.OnConversationClick() {
                    @Override
                    public void onClick(Conversation conversation) {
                        if (conversation != null) {
                            Log.d(getTAG(), "Channel " + conversation.getChannel().getName() + " is selected");
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
                this.conversations.clear();
                this.conversations.addAll(conversations);
                adapter.notifyDataSetChanged();
            }
        } else {
            Log.w(getTAG(), "Fragment is detached, won't update list");
        }
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
                        presenter.showAllConversations();
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

    private void setProgressIndicator(boolean active) {
        swipeContainer.setRefreshing(active);
    }

    private void showChatList(List<Conversation> chatList) {
        showList(chatList);
    }

    @Override
    public void showNewChat() {
        startActivity(ChooseUserActivity.getIntentToCreateChannel(getActivity()));
    }

    @Override
    public void showChatDetails(Conversation conversation) {
        startActivity(ChatActivity.getIntentWithChannel(getActivity(), conversation));
    }

    @Override
    public void dismissLeaveDialog() {
        if (leaveDialog != null && leaveDialog.isShowing()) {
            leaveDialog.dismiss();
        }
    }

    @Override
    public void switchSwipeContainer(boolean isNeedHidden) {
        if (swipeContainer != null) {
            swipeContainer.setRefreshing(isNeedHidden);
        }
    }

    @Override
    public String getTAG() {
        return ChatListFragment.class.getSimpleName();
    }
}
