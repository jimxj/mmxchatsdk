package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.databinding.ItemChatBinding;
import com.magnet.magnetchat.model.Chat;
import com.magnet.magnetchat.ui.adapters.base.BaseSortedAdapter;
import com.magnet.magnetchat.ui.adapters.base.BindingViewHolder;
import com.magnet.magnetchat.ui.views.helpers.ChatViewHelper;
import java.util.List;

public class ChatsAdapter extends BaseSortedAdapter<ChatsAdapter.ConversationViewHolder, Chat> {
    private static final String TAG = "ChatsAdapter";

    public ChatsAdapter(Context context, List<Chat> conversations, ItemComparator<Chat> comparator) {
       super(context, conversations, Chat.class, comparator);
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemChatBinding
            binding = DataBindingUtil.inflate(mInflater, R.layout.item_chat, parent, false);
        return new ConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

    /**
     * View holder to show mConversations with user's avatars and messages
     */
    protected class ConversationViewHolder extends BindingViewHolder<Chat, ItemChatBinding> implements View.OnClickListener, View.OnLongClickListener {
        public ConversationViewHolder(ItemChatBinding binding) {
            super(binding);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(params);
        }

        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onLongClick(getAdapterPosition());
                return true;
            }
            return false;
        }

        @Override public void bindTo(@NonNull Chat item) {
            mBinding.setChat(item);
            mBinding.setSingleRecipient(ChatViewHelper.getSingleRecipient(item));
            mBinding.executePendingBindings();
        }
    }
}
