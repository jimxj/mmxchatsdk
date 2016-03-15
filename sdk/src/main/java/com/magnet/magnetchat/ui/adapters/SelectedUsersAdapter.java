package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.databinding.ItemSelectedUserBinding;
import com.magnet.magnetchat.ui.adapters.base.BindingViewHolder;
import com.magnet.max.android.UserProfile;
import java.util.List;

public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.SelectedUserViewHolder> {

    private LayoutInflater inflater;
    private List<UserProfile> userList;
    private Context context;

    public class SelectedUserViewHolder extends
        BindingViewHolder<UserProfile, ItemSelectedUserBinding> {
        public SelectedUserViewHolder(ItemSelectedUserBinding itemView) {
            super(itemView);
        }

        @Override
        public void bindTo(@NonNull UserProfile item) {
            mBinding.setUser(item);
            mBinding.executePendingBindings();
        }
    }

    public SelectedUsersAdapter(Context context, List<? extends UserProfile> users) {
        this.context = context;
        this.userList = (List<UserProfile>) users;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public SelectedUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemSelectedUserBinding
            binding = DataBindingUtil.inflate(inflater, R.layout.item_selected_user, parent, false);
        return new SelectedUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final SelectedUserViewHolder holder, int position) {
        if(null != holder) {
            holder.bindTo(userList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}
