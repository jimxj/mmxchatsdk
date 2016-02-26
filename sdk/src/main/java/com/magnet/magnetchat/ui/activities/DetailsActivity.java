package com.magnet.magnetchat.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.ui.adapters.UsersAdapter;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.max.android.util.StringUtil;
import com.magnet.mmx.client.api.ListResult;
import com.magnet.mmx.client.api.MMXChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class DetailsActivity extends BaseActivity {

    public static final String TAG_CHANNEL = "channel";

    RecyclerView listView;

    ProgressBar detailsProgress;

    private MMXChannel currentChannel;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Details");

        detailsProgress = (ProgressBar) findViewById(R.id.detailsProgress);
        listView = (RecyclerView) findViewById(R.id.detailsSubscribersList);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);
        listView.setLayoutManager(layoutManager);

        currentChannel = getIntent().getParcelableExtra(TAG_CHANNEL);
        if (currentChannel != null) {
            detailsProgress.setVisibility(View.VISIBLE);
            currentChannel.getAllSubscribers(100, 0, new MMXChannel.OnFinishedListener<ListResult<User>>() {
                @Override public void onSuccess(ListResult<User> userListResult) {
                    onComplete();

                    List<UserProfile> userProfiles = new ArrayList<>(userListResult.items.size());
                    for(User u : userListResult.items) {
                        if(!u.getUserIdentifier().equals(User.getCurrentUserId())) {
                            userProfiles.add(u);
                        }
                    }
                    Collections.sort(userProfiles, UserHelper.getUserProfileComparator());
                    bindAdapter(userProfiles);
                }

                @Override
                public void onFailure(MMXChannel.FailureCode failureCode, Throwable throwable) {
                    onComplete();

                    exit(failureCode.toString());
                }

                private void onComplete() {
                    detailsProgress.setVisibility(View.GONE);
                }
            });
        } else {
            exit("Channel not set");
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private UsersAdapter.AddUserListener addUserListener = new UsersAdapter.AddUserListener() {
        @Override
        public void addUser() {
            startActivity(ChooseUserActivity.getIntentToAddUserToChannel(DetailsActivity.this, currentChannel.getName()));
            finish();
        }
    };

    public static Intent createIntentForChannel(Context context, Conversation conversation) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(TAG_CHANNEL, conversation.getChannel());
        return intent;
    }

    private void bindAdapter(List<UserProfile> users) {
        UsersAdapter adapter = new UsersAdapter(this, users, StringUtil.isStringValueEqual(currentChannel.getOwnerId(), User.getCurrentUserId()) ? addUserListener : null);
        listView.setAdapter(adapter);
    }

    private void exit(String error) {
        showMessage("Couldn't load channel due to " + error + ". Please try later");
        finish();
    }

}
