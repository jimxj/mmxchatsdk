package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.ui.views.section.chat.CircleNameView;
import com.magnet.max.android.UserProfile;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends BaseAdapter<UsersAdapter.UserViewHolder, UserProfile> {
    private List<UserProfile> selectedUsers;

    /**
     * ViewHolder for user items.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View currentView;
        CircleImageView imageAvatar;
        CircleNameView viewAvatar;
        AppCompatTextView firstName;
        AppCompatTextView lastName;
        TextView firstLetter;
        UserProfile user;

        public UserViewHolder(View itemView) {
            super(itemView);
            this.currentView = itemView;
            imageAvatar = (CircleImageView) itemView.findViewById(R.id.imageUserAvatar);
            viewAvatar = (CircleNameView) itemView.findViewById(R.id.viewUserAvatar);
            firstName = (AppCompatTextView) itemView.findViewById(R.id.itemUserFirstName);
            lastName = (AppCompatTextView) itemView.findViewById(R.id.itemUserLastName);
            firstLetter = (TextView) itemView.findViewById(R.id.itemUserFirstLetter);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(getAdapterPosition());
            }
        }
    }

    public UsersAdapter(Context context, List<? extends UserProfile> users) {
        this(context, UserHelper.convertToUserProfileList(users), null);
    }

    public UsersAdapter(Context context, List<UserProfile> users, List<? extends UserProfile> selectedUsers) {
        super(context, users);
        this.selectedUsers = (List<UserProfile>) selectedUsers;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_user, parent, false);
        UserViewHolder viewHolder = new UserViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final UserViewHolder viewHolder, int position) {
        if (viewHolder != null) {
            UserProfile user = getItem(position);
            UserProfile previous = null;
            viewHolder.user = user;
            if (position > 0) {
                previous = getItem(position - 1);
            }
            if (user.getFirstName() != null) {
                viewHolder.firstName.setText(user.getFirstName());
            }
            if (user.getLastName() != null) {
                viewHolder.lastName.setText(user.getLastName());
            }
            if (user.getFirstName() == null && user.getLastName() == null) {
                viewHolder.firstName.setText(user.getDisplayName());
            }

            char currentFirstLetter = getCharToGroup(user);
            char previousFirstLetter = getCharToGroup(previous);
            if (previous == null || currentFirstLetter != previousFirstLetter) {
                viewHolder.firstLetter.setVisibility(View.VISIBLE);
                viewHolder.firstLetter.setText(String.valueOf(currentFirstLetter).toUpperCase());
            } else {
                viewHolder.firstLetter.setVisibility(View.GONE);
            }

            viewHolder.viewAvatar.setUserName(user.getDisplayName());
            if (user.getAvatarUrl() != null) {
                viewHolder.imageAvatar.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(user.getAvatarUrl()).fitCenter().listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                        viewHolder.imageAvatar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                        return false;
                    }
                }).into(viewHolder.imageAvatar);
            } else {
                viewHolder.imageAvatar.setVisibility(View.GONE);
            }

            colorSelected(viewHolder.currentView, user);
        }
    }

    private char getCharToGroup(UserProfile userProfile) {
        char letter = ' ';
        String str = UserHelper.getUserNameToCompare(userProfile);
        if (str.length() > 0) {
            letter = str.charAt(0);
        }
        return letter;
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