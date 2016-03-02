package com.magnet.magnetchat.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.magnet.magnetchat.R;
import com.magnet.magnetchat.core.managers.ChannelCacheManager;
import com.magnet.magnetchat.helpers.PermissionHelper;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.model.Conversation;
import com.magnet.magnetchat.model.Message;
import com.magnet.magnetchat.mvp.api.ChatContract;
import com.magnet.magnetchat.mvp.api.OnRecyclerViewItemClickListener;
import com.magnet.magnetchat.mvp.presenters.ChatPresenterImpl;
import com.magnet.magnetchat.ui.adapters.MessagesAdapter;
import com.magnet.magnetchat.util.Logger;
import com.magnet.magnetchat.util.Utils;
import com.magnet.max.android.User;
import com.magnet.max.android.UserProfile;
import com.magnet.mmx.client.api.MMX;
import com.magnet.mmx.client.api.MMXChannel;
import com.magnet.mmx.client.api.MMXMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import nl.changer.polypicker.Config;
import nl.changer.polypicker.ImagePickerActivity;

public class ChatActivity extends BaseActivity implements ChatContract.View, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = ChatActivity.class.getSimpleName();

    public static final String TAG_CHANNEL_NAME = "channelName";
    public static final String TAG_CHANNEL_OWNER_ID = "channelOwnerId";
    public static final String TAG_CHANNEL_DETAIL = "channelDetail";
    public static final String TAG_CREATE_WITH_RECIPIENTS = "createWithRecipients";
    public static final String TAG_CREATE_NEW = "createNew";

    private static final String[] ATTACHMENT_VARIANTS = {"Send photo", "Send location", /*"Send video",*/ "Cancel"};

    public static final int INTENT_REQUEST_GET_IMAGES = 14;
    public static final int INTENT_SELECT_VIDEO = 13;

    public static final int REQUEST_LOCATION = 1111;
    public static final int REQUEST_VIDEO = 1112;
    public static final int REQUEST_IMAGE = 1113;

    private MessagesAdapter mAdapter;
    private RecyclerView messagesListView;
    private String channelName;
    private AlertDialog attachmentDialog;
    private GoogleApiClient googleApiClient;

    private ProgressBar chatMessageProgress;
    AppCompatEditText editMessage;
    TextView sendMessageButton;
    Toolbar toolbar;

    ChatContract.UserActionsListener presenter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_chat;
    }

    @Override
    protected int getBaseViewID() {
        return R.id.main_content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //For keeping toolbar when user input message
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setSupportActionBar(toolbar);
        //if (getSupportActionBar() != null) {
        //    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //}

        editMessage = (AppCompatEditText) findViewById(R.id.chatMessageField);
        sendMessageButton = (TextView) findViewById(R.id.chatSendBtn);

        chatMessageProgress = (ProgressBar) findViewById(R.id.chatMessageProgress);

        setOnClickListeners(sendMessageButton);
        findViewById(R.id.chatAddAttachment).setOnClickListener(this);

        messagesListView = (RecyclerView) findViewById(R.id.chatMessageList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false);
        messagesListView.setLayoutManager(layoutManager);

        channelName = getIntent().getStringExtra(TAG_CHANNEL_NAME);
        if (null != channelName) {
            Conversation currentConversation = ChannelCacheManager.getInstance().getConversationByName(channelName);
            if (currentConversation != null) {
                presenter = new ChatPresenterImpl(this, currentConversation);
            } else {
                showMessage("Can load the conversation");
                finish();
                return;
            }
        } else {
            ArrayList<UserProfile> recipients = getIntent().getParcelableArrayListExtra(TAG_CREATE_WITH_RECIPIENTS);
            if (recipients != null) {
                presenter = new ChatPresenterImpl(this, recipients);
            } else {
                showMessage("Can load the conversation");
                finish();
                return;
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.chatSendBtn) {
            String text = getSimpleText(editMessage);
            if (text != null && !text.isEmpty()) {
                sendMessageButton.setEnabled(false);
                presenter.onSendText(text);
            }
        } else if(v.getId() == R.id.chatAddAttachment) {
            showAttachmentDialog();
        }
    }

    @Override
    protected void onPause() {
        MMX.unregisterListener(eventListener);
        if (attachmentDialog != null && attachmentDialog.isShowing()) {
            attachmentDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.onRefreshMessages();

        MMX.registerListener(eventListener);
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuChatOpenDetails) {
            showChatDetails(presenter.getCurrentConversation());
        } else if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void setProgressIndicator(boolean active) {
        chatMessageProgress.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
    }

    @Override public void showMessages(List<Message> messages) {
        if(null == mAdapter) {
            mAdapter = new MessagesAdapter(this, messages);
            mAdapter.setmOnClickListener(new OnRecyclerViewItemClickListener() {
                @Override public void onClick(int position) {
                    presenter.onMessageClick(mAdapter.getItem(position));
                }

                @Override public void onLongClick(int position) {

                }
            });
            messagesListView.setAdapter(mAdapter);
        } else {
            mAdapter.swapData(messages);
        }
    }

    @Override public void showRecipients(List<UserProfile> recipients) {
        if (recipients.size() == 1) {
            setTitle(UserHelper.getDisplayNames(recipients));
        } else {
            setTitle("Group");
        }
    }

    @Override public void showNewMessage(Message message) {
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(mAdapter.getItemCount());
            messagesListView.smoothScrollToPosition(mAdapter.getItemCount());
        }
    }

    @Override public void showImagePicker() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        Config config = new Config.Builder()
            .setTabBackgroundColor(R.color.white)
            .setSelectionLimit(1)
            .build();
        ImagePickerActivity.setConfig(config);
        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
    }

    @Override public void clearInput() {
        editMessage.setText("");
    }

    @Override public void setSendEnabled(boolean enabled) {
        sendMessageButton.setEnabled(true);
    }

    @Override public void showLocation(Message message) {
        if (!Utils.isGooglePlayServiceInstalled()) {
            Utils.showMessage(this, "It seems Google play services is not available, can't use location API");
        } else {
            String uri = String.format(Locale.ENGLISH, "geo:%s?z=16&q=%s", message.getLatitudeLongitude(), message.getLatitudeLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            try {
                this.startActivity(intent);
            } catch (Throwable e) {
                Log.e(TAG, "Can find any app to show map", e);
                Utils.showMessage(this, "Can find any app to show map");
            }
        }
    }

    @Override public void showImage(Message message) {
        if (message.getAttachment() != null) {
            String newImagePath = message.getAttachment().getDownloadUrl();
            Log.d(TAG, "Viewing photo : " + newImagePath + "\n" + message.getAttachment());
            if (newImagePath != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newImagePath));
                intent.setDataAndType(Uri.parse(newImagePath), "image/*");
                try {
                    this.startActivity(intent);
                } catch (Throwable e) {
                    Log.e(TAG, "Can find any app to mView image", e);
                    Utils.showMessage(this, "Can find any app to mView image");
                }
            }
        }
    }

    @Override public void showChatDetails(Conversation conversation) {
        if (conversation != null) {
            startActivity(DetailsActivity.createIntentForChannel(this, conversation));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_REQUEST_GET_IMAGES) {
                Parcelable[] parcelableUris = intent.getParcelableArrayExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
                if (parcelableUris == null) {
                    return;
                }
                Uri[] uris = new Uri[parcelableUris.length];
                System.arraycopy(parcelableUris, 0, uris, 0, parcelableUris.length);

                presenter.onSendImages(uris);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allPermitted = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                allPermitted = false;
                break;
            }
        }
        if (allPermitted) {
            switch (requestCode) {
                case REQUEST_IMAGE:
                    showImagePicker();
                    break;
                case REQUEST_LOCATION:
                    sendLocation();
                    break;
                //case REQUEST_VIDEO:
                //    selectVideo();
                //    break;
            }
        } else {
            showMessage("Can't do it without permission");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private boolean needPermission(int requestCode, String... permissions) {
        return PermissionHelper.checkPermission(this, requestCode, permissions);
    }

    private void showAttachmentDialog() {
        if (attachmentDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(ATTACHMENT_VARIANTS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            if (!needPermission(REQUEST_IMAGE, PermissionHelper.CAMERA_PERMISSION, PermissionHelper.STORAGE_PERMISSION)) {
                                showImagePicker();
                            }
                            break;
                        case 1:
                            if (!needPermission(REQUEST_LOCATION, PermissionHelper.LOCATION_PERMISSION1, PermissionHelper.LOCATION_PERMISSION2)) {
                                sendLocation();
                            }
                            break;
                        //case 2:
                        //    if (!needPermission(REQUEST_VIDEO, PermissionHelper.STORAGE_PERMISSION)) {
                        //        selectVideo();
                        //    }
                        //    break;
                        case 3:
                            break;
                    }
                    attachmentDialog.dismiss();
                }
            });
            builder.setCancelable(true);
            attachmentDialog = builder.create();
        }
        attachmentDialog.show();
    }

    private void sendLocation() {
        if (!Utils.isGooglePlayServiceInstalled()) {
            showMessage("It seems Google play services is not available, can't use location API");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMessage("Location permission is not enabled");
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
            presenter.onSendLocation(currentLocation);
        } else {
            showMessage("Can't get location");
        }
    }

    private MMX.EventListener eventListener = new MMX.EventListener() {
        @Override
        public boolean onMessageReceived(MMXMessage mmxMessage) {
            Logger.debug(TAG, "Received message in : " + mmxMessage);
            MMXChannel channel = mmxMessage.getChannel();
            if (channel != null && mAdapter != null) {
                String messageChannelName = channel.getName();
                if (messageChannelName.equalsIgnoreCase(channelName)) {
                    presenter.onNewMessage(Message.createMessageFrom(mmxMessage));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onMessageAcknowledgementReceived(User from, String messageId) {
            if (mAdapter != null) {
                //updateList();
            }
            return true;
        }
    };

    public static Intent getIntentWithChannel(Context context, Conversation conversation) {
        if (null != conversation && null != conversation.getChannel()) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(TAG_CHANNEL_NAME, conversation.getChannel().getName());
            return intent;
        } else {
            Log.e(TAG, "getIntentWithChannel return null because conversation or channel is null");
            return null;
        }
    }

    public static Intent getIntentForNewChannel(Context context, ArrayList<UserProfile> recipients) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putParcelableArrayListExtra(TAG_CREATE_WITH_RECIPIENTS, recipients);
        return intent;
    }

    private void setTitle(String title) {
        toolbar.setTitle(title);
    }
}
