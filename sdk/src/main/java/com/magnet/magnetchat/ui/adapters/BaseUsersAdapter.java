package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.databinding.ItemUserBinding;
import com.magnet.magnetchat.ui.adapters.base.BaseSortedAdapter;
import com.magnet.magnetchat.ui.adapters.base.BindingViewHolder;
import com.magnet.max.android.UserProfile;
import java.util.List;

public abstract class BaseUsersAdapter<V extends BaseUsersAdapter.UserViewHolder, T extends UserProfile> extends
    BaseSortedAdapter<V, T> {

    public BaseUsersAdapter(Context context, List<T> data, Class<T> clazz,
        ItemComparator comparator) {
        super(context, data, clazz, comparator);
    }

    @NonNull
    protected UserViewHolder createUserViewHolder(ViewGroup parent) {
        ItemUserBinding
            binding = DataBindingUtil.inflate(mInflater, R.layout.item_user, parent, false);
        return new UserViewHolder(binding);
    }

    /**
     * ViewHolder for user items.
     */
    public class UserViewHolder extends BindingViewHolder<UserProfile, ItemUserBinding> implements View.OnClickListener {
        public UserViewHolder(ItemUserBinding binding) {
            super(binding);

            itemView.setOnClickListener(this);
        }

        @Override
        public void bindTo(@NonNull UserProfile user) {
            mBinding.setUser(user);
            int position = getAdapterPosition();
            mBinding.setPreviousUser(position > 0 ? getItem(position - 1) : null);
            mBinding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(getAdapterPosition());
            }
        }
    }
}