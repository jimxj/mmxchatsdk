package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.view.ViewGroup;
import com.magnet.max.android.UserProfile;
import java.util.List;

public class UserProfilesAdapter extends BaseUsersAdapter<BaseUsersAdapter.UserViewHolder, UserProfile> {

    public UserProfilesAdapter(Context context, List<UserProfile> users, ItemComparator<UserProfile> comparator) {
        super(context, users, UserProfile.class, comparator);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createUserViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(final BaseUsersAdapter.UserViewHolder viewHolder, int position) {
        viewHolder.bindTo(getItem(position));
    }
}