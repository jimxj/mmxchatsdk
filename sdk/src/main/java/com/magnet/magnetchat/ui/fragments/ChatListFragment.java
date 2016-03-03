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
import com.magnet.magnetchat.callbacks.OnRecyclerViewItemClickListener;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.ChannelHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.mvp.api.ChatListContract;
import com.magnet.magnetchat.mvp.presenters.ChatListPresenterImpl;
import com.magnet.magnetchat.ui.activities.ChatActivity;
import com.magnet.magnetchat.ui.activities.ChooseUserActivity;
import com.magnet.magnetchat.ui.adapters.ChatsAdapter;
import com.magnet.magnetchat.ui.custom.CustomSearchView;
import com.magnet.magnetchat.ui.views.DividerItemDecoration;
import java.util.List;

public class ChatListFragment extends BaseFragment implements ChatListContract.View {
    private final static String TAG = "ChatListFragment";

    private RecyclerView conversationsList;
    private SwipeRefreshLayout swipeContainer;

    private AlertDialog leaveDialog;

    private FloatingActionButton fabCreateMessage;

    private ChatsAdapter adapter;

    private ChatListContract.Presenter presenter;

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
                presenter.onLoadConversations(true);
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent);

        fabCreateMessage = (FloatingActionButton) containerView.findViewById(R.id.fabHomeCreateMessage);
        fabCreateMessage.setVisibility(View.VISIBLE);
        fabCreateMessage.setOnClickListener(this);

        setHasOptionsMenu(true);

        presenter = new ChatListPresenterImpl(this);

        swipeContainer.post(new Runnable() {
            @Override
            public void run() {
                swipeContainer.setRefreshing(true);
                presenter.onLoadConversations(true);
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
                    presenter.onSearchMessage(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        hideKeyboard();
                        presenter.onLoadConversations(false);
                    }
                    return false;
                }
            });

            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    search.onActionViewCollapsed();
                    presenter.onLoadConversations(false);
                    return true;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.fabHomeCreateMessage) {
            createNewChat();
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
                adapter = new ChatsAdapter(getActivity(), conversations);
                adapter.setOnClickListener(new OnRecyclerViewItemClickListener() {
                    @Override public void onClick(int position) {
                        Conversation conversation = adapter.getItem(position);
                        if (conversation != null) {
                            Log.d(TAG, "Channel " + conversation.getChannel().getName() + " is selected");
                            presenter.onConversationClick(conversation);
                        }
                    }

                    @Override public void onLongClick(int position) {
                        Conversation conversation = adapter.getItem(position);
                        if (conversation != null) {
                            presenter.onConversationLongClick(conversation);
                            showLeaveDialog(conversation);
                        }
                    }
                });
                conversationsList.setAdapter(adapter);
            } else {
                adapter.swapData(conversations);
            }
        } else {
            Log.w(TAG, "Fragment is detached, won't update list");
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

                        removeItem(adapter.getData().indexOf(conversation));
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

    @Override
    public void setProgressIndicator(boolean active) {
        swipeContainer.setRefreshing(active);
    }

    @Override
    public void createNewChat() {
        startActivity(ChooseUserActivity.getIntentToCreateChannel(getActivity()));
    }

    @Override public void showConversationUpdate(Conversation conversation, boolean isNew) {
        if(null != adapter) {
            if(isNew) {
                adapter.addItem(conversation);
            } else {
                adapter.updateItem(conversation);
            }
        }
    }

    @Override
    public void showChatDetails(Conversation conversation) {
        startActivity(ChatActivity.getIntentWithChannel(getActivity(), conversation));
    }

    @Override public void showLeaveConfirmation(Conversation conversation) {
        showLeaveDialog(conversation);
    }

    @Override
    public void dismissLeaveDialog() {
        if (leaveDialog != null && leaveDialog.isShowing()) {
            leaveDialog.dismiss();
        }
    }

    private void removeItem(int position) {
        conversationsList.removeViewAt(position);
        adapter.removeItem(position);
    }
}
