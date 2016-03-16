package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.databinding.ItemMessageBinding;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.mvp.api.MessageHandler;
import com.magnet.magnetchat.ui.adapters.base.BaseSortedAdapter;
import com.magnet.magnetchat.ui.adapters.base.BindingViewHolder;
import com.magnet.magnetchat.ui.custom.CircleAvatarOrNameView;
import com.magnet.magnetchat.ui.views.helpers.MessageViewHelper;
import java.util.List;

public class MessagesAdapter extends BaseSortedAdapter<MessagesAdapter.MessageViewHolder, Message> {
    private final static String TAG = MessagesAdapter.class.getSimpleName();
    private final MessageHandler mMessageHandler;

    public MessagesAdapter(Context context, List<Message> messages, ItemComparator<Message> comparator, MessageHandler messageHandler) {
        super(context, messages, Message.class, comparator);
        this.mMessageHandler = messageHandler;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemMessageBinding binding = DataBindingUtil.inflate(mInflater, R.layout.item_message, parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        holder.bindTo(getItem(position));
    }

    public class MessageViewHolder extends BindingViewHolder<Message, ItemMessageBinding> implements View.OnClickListener, View.OnLongClickListener {
        public MessageViewHolder(@NonNull ItemMessageBinding viewDataBinding) {
            super(viewDataBinding);
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

        @Override
        public void bindTo(@NonNull Message item) {
            mBinding.setMessage(item);
            int position = getAdapterPosition();
            mBinding.setPreviousMessage(position > 0 ? getItem(position - 1) : null);
            mBinding.setIsMessageFromMe(MessageViewHelper.isMessageFromMe(item));
            mBinding.setClickHandler(new MessageViewHandler(item));

            mBinding.executePendingBindings();
        }
    }

    public class MessageViewHandler implements View.OnClickListener, View.OnLongClickListener {
        private final Message mMessage;

        public MessageViewHandler(Message message) {
            this.mMessage = message;
        }

        //public void onSenderClick(CircleAvatarOrNameView view) {
        //    mMessageHandler.onSenderClick(mMessage);
        //}
        //
        //public void onTextMessageClick(TextView view) {
        //    mMessageHandler.onMessageBodyClick(mMessage);
        //}
        //
        //public void onTextMessageLongClick(TextView view) {
        //    mMessageHandler.onMessageBodyLongClick(mMessage);
        //}
        //
        //public void onMediaMessageClick(ImageView view) {
        //    mMessageHandler.onMessageBodyClick(mMessage);
        //}
        //
        //public void onMediaMessageLongClick(ImageView view) {
        //    mMessageHandler.onMessageBodyLongClick(mMessage);
        //}

        @Override public void onClick(View v) {
            if(v.getId() == R.id.viewOtherAvatar) {
                mMessageHandler.onSenderClick(mMessage);
            } else if(v.getId() == R.id.itemMessageText || v.getId() == R.id.itemMessageImage) {
                mMessageHandler.onMessageBodyClick(mMessage);
            }
        }

        @Override public boolean onLongClick(View v) {
            if(v.getId() == R.id.itemMessageText || v.getId() == R.id.itemMessageImage) {
                mMessageHandler.onMessageBodyLongClick(mMessage);
            }
            return false;
        }
    }
}
