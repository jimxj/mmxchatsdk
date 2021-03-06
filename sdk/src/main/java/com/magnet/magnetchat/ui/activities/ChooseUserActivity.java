package com.magnet.magnetchat.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.magnet.magnetchat.Constants;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.callbacks.EndlessLinearRecyclerViewScrollListener;
import com.magnet.magnetchat.callbacks.OnRecyclerViewItemClickListener;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.mvp.api.ChooseUserContract;
import com.magnet.magnetchat.mvp.presenters.ChooseUserPresenterImpl;
import com.magnet.magnetchat.ui.adapters.SelectedUsersAdapter;
import com.magnet.magnetchat.ui.adapters.UsersAdapter;
import com.magnet.magnetchat.ui.custom.CustomSearchView;
import com.magnet.max.android.User;

import java.util.ArrayList;
import java.util.List;


public class ChooseUserActivity extends BaseActivity implements ChooseUserContract.View {
    private static final String TAG = "ChooseUserActivity";
    public static final String TAG_ADD_USER_TO_CHANNEL = "onUsersSelected";

    private RecyclerView userList;
    private RecyclerView selectedUserList;
    private TextView tvSelectedAmount;
    private LinearLayout llSelectedUsers;
    private ProgressBar userSearchProgress;
    private Toolbar toolbar;

    private UsersAdapter mAdapter;
    private SelectedUsersAdapter selectedAdapter;
    private ArrayList<User> selectedUsers;

    private ChooseUserContract.Presenter mPresenter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_choose_user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setOnClickListeners(R.id.fabAdd);

        tvSelectedAmount = (TextView) findViewById(R.id.tvSelectedUsersAmount);
        llSelectedUsers = (LinearLayout) findViewById(R.id.llSelectedUsers);
        userSearchProgress = (ProgressBar) findViewById(R.id.chooseUserProgress);

        userList = (RecyclerView) findViewById(R.id.chooseUserList);
        LinearLayoutManager userListLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        userList.setLayoutManager(userListLayoutManager);
        userList.addOnScrollListener(new EndlessLinearRecyclerViewScrollListener(userListLayoutManager) {
            @Override public void onLoadMore(int page, int totalItemsCount) {
                Log.d(TAG, "------------onLoadMore User: " + page + "/" + totalItemsCount + "\n");
                mPresenter.onLoad(totalItemsCount, Constants.USER_PAGE_SIZE);
            }
        });

        selectedUserList = (RecyclerView) findViewById(R.id.selectedUserList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        selectedUserList.setLayoutManager(layoutManager);
        selectedUsers = new ArrayList<>();
        selectedAdapter = new SelectedUsersAdapter(this, selectedUsers);
        selectedUserList.setAdapter(selectedAdapter);

        String channelName = getIntent().getStringExtra(TAG_ADD_USER_TO_CHANNEL);
        if (channelName != null) {
            setTitle("Add contacts");
            mPresenter = new ChooseUserPresenterImpl(this, channelName);
        } else {
            setTitle("All contacts");
            mPresenter = new ChooseUserPresenterImpl(this);
        }

        mPresenter.onLoad(0, Constants.USER_PAGE_SIZE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabAdd) {
            mPresenter.onUsersSelected(selectedUsers);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose_user, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final CustomSearchView search = (CustomSearchView) menu.findItem(R.id.menuUserSearch).getActionView();
            search.setHint("Search contacts");
            search.setOnQueryTextListener(queryTextListener);
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    search.onActionViewCollapsed();
                    hideKeyboard();
                    mPresenter.onSearchReset();
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

    //MVP METHODS

    /**
     * Method which provide to switching of the search user progress
     *
     * @param active
     */
    @Override
    public void setProgressIndicator(boolean active) {
        if (userSearchProgress != null) {
            userSearchProgress.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Method which provide the list updating from the list of users object
     *
     * @param users users list
     */
    @Override
    public void showUsers(@NonNull List<User> users, boolean toAppend) {
        if (null == mAdapter) {
            mAdapter =
                new UsersAdapter(this, users, selectedUsers, mPresenter.getItemComparator());
            userList.setAdapter(mAdapter);
            mAdapter.setOnClickListener(userClickListener);
        } else {
            if(toAppend) {
                mAdapter.addItem(users);
            } else {
                mAdapter.swapData(users);
            }
        }
    }

    /**
     * Method which provide the closing of the Activity
     */
    @Override
    public void finishSelection() {
        finish();
    }

    /**
     * Method which provide the getting of the activity
     *
     * @return current activity
     */
    @Override
    public Activity getActivity() {
        return this;
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
            mPresenter.onSearch(UserHelper.createNameQuery(query), ChooseUserContract.DEFAULT_USER_ORDER);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.isEmpty()) {
                hideKeyboard();
                mPresenter.onSearchReset();
            }
            return true;
        }
    };

    /**
     * Listener which provide to the user click listening
     */
    private final OnRecyclerViewItemClickListener userClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onClick(int position) {
            hideKeyboard();
            User user = mAdapter.getItem(position);

            if (user != null) {
                boolean isAdding = true;
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                    isAdding = false;
                } else {
                    selectedUsers.add(user);
                }
                if (selectedUsers.size() > 0) {
                    tvSelectedAmount.setText(String.format("%d selected", selectedUsers.size()));

                    if(isAdding && 1 == selectedUsers.size()) {
                        llSelectedUsers.setVisibility(View.VISIBLE);
                    }

                    if(isAdding) {
                        selectedAdapter.notifyItemInserted(selectedUsers.size());
                    } else {
                        selectedAdapter.notifyItemRemoved(selectedUsers.size());
                    }
                } else {
                    llSelectedUsers.setVisibility(View.GONE);
                }
            }

            mAdapter.colorSelected(userList.getLayoutManager().findViewByPosition(position), user);
            //mAdapter.notifyItemChanged(position);
        }

        @Override
        public void onLongClick(int position) {

        }
    };

}
