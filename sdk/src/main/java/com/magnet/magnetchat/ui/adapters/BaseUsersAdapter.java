package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.databinding.ItemUserBinding;
import com.magnet.max.android.UserProfile;
import java.util.List;

public abstract class BaseUsersAdapter<V extends BaseUsersAdapter.UserViewHolder, T extends UserProfile> extends BaseSortedAdapter<V, T> {

    public BaseUsersAdapter(Context context, List<T> data, Class<T> clazz,
        ItemComparator comparator) {
        super(context, data, clazz, comparator);
    }

    /**
     * ViewHolder for user items.
     */
    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemUserBinding mBinding;

        public UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            itemView.setOnClickListener(this);
        }

        public void bindTo(@NonNull T user, T previousUser) {
            mBinding.setUser(user);
            mBinding.setPreviousUser(previousUser);
            mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(getAdapterPosition());
            }
        }
    }

    @NonNull protected UserViewHolder createUserViewHolder(ViewGroup parent) {
        ItemUserBinding
            binding = DataBindingUtil.inflate(mInflater, R.layout.item_user, parent, false);
        return new UserViewHolder(binding);
    }
}