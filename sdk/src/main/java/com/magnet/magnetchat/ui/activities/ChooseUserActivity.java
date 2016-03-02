package com.magnet.magnetchat.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.magnet.magnetchat.R;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.mvp.api.ChooseUserContract;
import com.magnet.magnetchat.mvp.presenters.ChooseUserPresenterImpl;
import com.magnet.magnetchat.ui.adapters.SelectedUsersAdapter;
import com.magnet.magnetchat.ui.adapters.UsersAdapter;
import com.magnet.magnetchat.ui.custom.CustomSearchView;
import com.magnet.max.android.UserProfile;

import java.util.ArrayList;
import java.util.List;


public class ChooseUserActivity extends BaseActivity implements ChooseUserContract.View {

    public static final String TAG_ADD_USER_TO_CHANNEL = "addUserToChannel";


    private enum ActivityMode {MODE_TO_CREATE, MODE_TO_ADD_USER}

    private RecyclerView userList;
    private RecyclerView selectedUserList;
    private TextView tvSelectedAmount;
    private LinearLayout llSelectedUsers;
    private ProgressBar userSearchProgress;
    private Toolbar toolbar;

    private UsersAdapter adapter;
    private SelectedUsersAdapter selectedAdapter;
    private ActivityMode currentMode;
    private ArrayList<UserProfile> selectedUsers;

    private ChooseUserContract.Presenter presenter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_choose_user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new ChooseUserPresenterImpl(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setOnClickListeners(R.id.registerSaveBtn);

        tvSelectedAmount = (TextView) findViewById(R.id.tvSelectedUsersAmount);
        llSelectedUsers = (LinearLayout) findViewById(R.id.llSelectedUsers);
        userSearchProgress = (ProgressBar) findViewById(R.id.chooseUserProgress);

        userList = (RecyclerView) findViewById(R.id.chooseUserList);
        userList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        selectedUserList = (RecyclerView) findViewById(R.id.selectedUserList);
        selectedUserList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedUsers = new ArrayList<>();
        selectedAdapter = new SelectedUsersAdapter(this, selectedUsers);
        selectedUserList.setAdapter(selectedAdapter);

        presenter.searchUsers("");

        currentMode = ActivityMode.MODE_TO_CREATE;
        String channelName = getIntent().getStringExtra(TAG_ADD_USER_TO_CHANNEL);
        if (channelName != null) {
            Conversation conversation = ChannelCacheManager.getInstance().getConversationByName(channelName);
            presenter.setConversation(conversation);
            currentMode = ActivityMode.MODE_TO_ADD_USER;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("All contacts");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.registerSaveBtn) {
            onAddUserPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose_user, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final CustomSearchView search = (CustomSearchView) menu.findItem(R.id.menuUserSearch).getActionView();
            search.setHint("Search users");
            search.setOnQueryTextListener(queryTextListener);
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    search.onActionViewCollapsed();
                    hideKeyboard();
                    presenter.searchUsers("");
                    return true;
                }
            });
            search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard();
                    }
                }
            });
        }
        return true;
    }

    /**
     * Method which provide to create channel or add user
     */
    private void onAddUserPressed() {
        if (selectedUsers.size() > 0) {
            switch (currentMode) {
                case MODE_TO_ADD_USER:
                    presenter.addUserToChannel(selectedUsers);
                    break;
                case MODE_TO_CREATE:
                    startActivity(ChatActivity.getIntentForNewChannel(this, selectedUsers));
                    finish();
                    break;
            }
        } else {
            showMessage("Nobody was selected");
        }
    }

    //MVP METHODS

    /**
     * Method which provide the show message
     *
     * @param message current message
     */
    @Override
    public void showInformationMessage(String message) {
        showMessage(message);
    }

    /**
     * Method which provide to switching of the search user progress
     *
     * @param isNeedShow
     */
    @Override
    public void switchSearchUserProgress(boolean isNeedShow) {
        if (userSearchProgress == null) {
            return;
        }
        if (isNeedShow == true) {
            userSearchProgress.setVisibility(View.VISIBLE);
        } else {
            userSearchProgress.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Method which provide the list updating from the list of users object
     *
     * @param users users list
     */
    @Override
    public void updateList(@NonNull List<? extends UserProfile> users) {
        adapter = new UsersAdapter(this, users, selectedUsers);
        userList.setAdapter(adapter);
        adapter.setOnUserClickListener(userClickListener);
    }

    /**
     * Method which provide the closing of the Activity
     */
    @Override
    public void closeActivity() {
        finish();
    }

    /**
     * Method which provide to start of the another conversation
     *
     * @param anotherConversation conversation object
     */
    @Override
    public void startAnotherConversation(@Nullable Conversation anotherConversation) {
        Intent i = ChatActivity.getIntentWithChannel(ChooseUserActivity.this, anotherConversation);
        if (null != i) {
            startActivity(i);
            closeActivity();
        }
    }

    //STATIC METHODS

    public static Intent getIntentToCreateChannel(Context context) {
        return new Intent(context, ChooseUserActivity.class);
    }

    public static Intent getIntentToAddUserToChannel(Context context, String channelName) {
        Intent intent = new Intent(context, ChooseUserActivity.class);
        intent.putExtra(TAG_ADD_USER_TO_CHANNEL, channelName);
        return intent;
    }

    //LISTENERS AND CALLBACKS

    /**
     * Listener which provide the query text listening
     */
    private final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            hideKeyboard();
            presenter.searchUsers(query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.isEmpty()) {
                hideKeyboard();
                presenter.searchUsers("");
            }
            return true;
        }
    };

    /**
     * Listener which provide to the user click listening
     */
    private final UsersAdapter.OnUserClickListener userClickListener = new UsersAdapter.OnUserClickListener() {
        @Override
        public void onUserClick(UserProfile user, int position) {
            hideKeyboard();
            if (user != null) {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                if (selectedUsers.size() > 0) {
                    tvSelectedAmount.setText(String.format("%d selected", selectedUsers.size()));
                    llSelectedUsers.setVisibility(View.VISIBLE);
                } else {
                    llSelectedUsers.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                selectedAdapter.notifyDataSetChanged();
            }
        }
    };

}
