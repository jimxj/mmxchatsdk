package com.magnet.magnetchat.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.helpers.DateHelper;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.ui.views.section.chat.CircleNameView;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class ChatsAdapter extends BaseAdapter<ChatsAdapter.ConversationViewHolder, Conversation> {

    /**
     * View holder to show mConversations with user's avatars and messages
     */
    protected class ConversationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Conversation conversation;
        ImageView newMessage;
        CircleImageView imageAvatar;
        CircleNameView viewAvatar;
        TextView title;
        TextView date;
        TextView lastMessage;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            newMessage = (ImageView) itemView.findViewById(R.id.imConversationNewMsg);
            imageAvatar = (CircleImageView) itemView.findViewById(R.id.imageConversationOwnerAvatar);
            viewAvatar = (CircleNameView) itemView.findViewById(R.id.viewConversationOwnerAvatar);
            title = (TextView) itemView.findViewById(R.id.tvConversationTitle);
            date = (TextView) itemView.findViewById(R.id.tvConversationDate);
            lastMessage = (TextView) itemView.findViewById(R.id.tvConversationLastMsg);
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
    }

    public ChatsAdapter(Context context, List<Conversation> conversations) {
       super(context, conversations);
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        ConversationViewHolder viewHolder = holder;
        Conversation conversation = getItem(position);
        if (viewHolder != null && conversation != null) {
            viewHolder.conversation = conversation;
            prepareTitleAndAvatar(conversation, viewHolder);
            if (conversation.hasUnreadMessage()) {
                viewHolder.newMessage.setVisibility(View.VISIBLE);
            } else {
                viewHolder.newMessage.setVisibility(View.INVISIBLE);
            }
            viewHolder.date.setText(DateHelper.getConversationLastDate(conversation.getLastActiveTime()));
            viewHolder.lastMessage.setText(getLastMessage(conversation));
        }
    }

    /**
     * Searches last message for conversation
     *
     * @param conversation
     * @return empty line, if conversation has not any massage
     */
    protected String getLastMessage(Conversation conversation) {
        List<Message> messages = conversation.getMessages();
        if (messages != null && messages.size() > 0) {
            Message message = messages.get(messages.size() - 1);
            String msgType = message.getType();
            if (msgType == null) {
                msgType = Message.TYPE_TEXT;
            }
            switch (msgType) {
                case Message.TYPE_MAP:
                    return "User's location";
                case Message.TYPE_VIDEO:
                    return "User's video";
                case Message.TYPE_PHOTO:
                    return "User's photo";
                case Message.TYPE_TEXT:
                    String text = message.getText().replace(System.getProperty("line.separator"), " ");
                    if (text.length() > 23) {
                        text = text.substring(0, 20) + "...";
                    }
                    return text;
            }
        }
        return "";
    }

    /**
     * If user is not null, configures avatar for current conversation.
     * If user has no avatar, sets his initials
     *
     * @param user
     * @param viewHolder
     */
    protected void setUserAvatar(UserProfile user, final ConversationViewHolder viewHolder) {
        if (user != null) {
            viewHolder.title.setText(user.getDisplayName());
            viewHolder.viewAvatar.setUserName(user.getDisplayName());
            if (user.getAvatarUrl() != null) {
                viewHolder.imageAvatar.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(user.getAvatarUrl()).fitCenter().listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String s, Target<GlideDrawable> target,
                        boolean b) {
                        //Log.e("BaseConversation", "failed to load image ", e);
                        viewHolder.imageAvatar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override public boolean onResourceReady(GlideDrawable glideDrawable, String s,
                        Target<GlideDrawable> target, boolean b, boolean b1) {
                        return false;
                    }
                }).into(viewHolder.imageAvatar);
            } else {
                viewHolder.imageAvatar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Sets title to conversation item(supplier name) and avatar image.
     *
     * @param conversation object for current item
     * @param viewHolder
     */
    protected void prepareTitleAndAvatar(Conversation conversation, ConversationViewHolder viewHolder) {
        List<UserProfile> suppliers = conversation.getSuppliersList();
        //If all suppliers left conversation, show current user.
        if (suppliers.size() == 0) {
            User currentUser = User.getCurrentUser();
            if (currentUser != null) {
                viewHolder.title.setText(String.format("%s %s", currentUser.getFirstName(), currentUser.getLastName()));
                setUserAvatar(currentUser, viewHolder);
            }
        } else {
            viewHolder.title.setText(UserHelper.getDisplayNames(conversation.getSuppliersList()));
            if (suppliers.size() > 1) {
                Glide.with(getContext()).load(R.drawable.user_group).fitCenter().into(viewHolder.imageAvatar);
            } else {
                //If there is one supplier, show his avatar.
                setUserAvatar(suppliers.get(0), viewHolder);
            }
        }
    }
}
