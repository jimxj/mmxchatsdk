package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.magnet.magnetchat.R;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import java.util.List;

public class UsersAdapter extends BaseUsersAdapter<BaseUsersAdapter.UserViewHolder, User> {
    private List<User> selectedUsers;

    public UsersAdapter(Context context, List<User> users, List<User> selectedUsers, ItemComparator<User> comparator) {
        super(context, users, User.class, comparator);
        this.selectedUsers = selectedUsers;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createUserViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(final BaseUsersAdapter.UserViewHolder viewHolder, int position) {
        if (viewHolder != null) {
            viewHolder.bindTo(getItem(position), position > 0 ? getItem(position - 1) : null);

            colorSelected(viewHolder.itemView, getItem(position));
        }
    }

    /**
     * Colors the item mView, if item is selected or returns to default color
     *
     * @param view
     * @param user
     */
    private void colorSelected(View view, UserProfile user) {
        if (selectedUsers != null && selectedUsers.contains(user)) {
            view.setBackgroundResource(R.color.itemSelected);
        } else {
            view.setBackgroundResource(R.color.itemNotSelected);
        }
    }
}