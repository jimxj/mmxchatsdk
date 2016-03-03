package com.magnet.chatsdk.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.magnet.chatsdk.sample.R;
import com.magnet.magnetchat.helpers.FileHelper;
import com.magnet.magnetchat.helpers.IntentHelper;
import com.magnet.magnetchat.ui.activities.BaseActivity;
import com.magnet.magnetchat.ui.activities.ChatListActivity;
import com.magnet.magnetchat.util.AppLogger;
import com.magnet.max.android.ApiCallback;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.Attachment;
import com.magnet.max.android.User;
import com.magnet.max.android.auth.model.UpdateProfileRequest;
import com.magnet.max.android.util.StringUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Artli_000 on 11.02.2016.
 */
public class EditProfileActivity extends BaseActivity {

  private static final int RESULT_LOAD_IMAGE = 1;
  private static final String TAG = EditProfileActivity.class.getSimpleName();

  View buttonClose;
  View buttonSaveChanges;
  View buttonChoosePicture;

  AppCompatTextView textEmail;
  AppCompatEditText editFirstName;
  AppCompatEditText editLastName;

  View viewProgress;

  CircleImageView imageViewAvatar;

  private User currentUser;

  @Override
  protected int getLayoutResource() {
    return R.layout.activity_edit_profile;
  }

  @Override
  protected int getBaseViewID() {
    return R.id.main_content;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    buttonClose = (View) findViewById(R.id.buttonClose);
    buttonSaveChanges = (View) findViewById(R.id.buttonSaveChanges);
    buttonChoosePicture = (View) findViewById(R.id.buttonChoosePicture);

    viewProgress = findViewById(R.id.viewProgress);
    viewProgress.setVisibility(View.GONE);

    textEmail = (AppCompatTextView) findViewById(R.id.textEmail);
    editFirstName = (AppCompatEditText) findViewById(R.id.editFirstName);
    editLastName = (AppCompatEditText) findViewById(R.id.editLastName);
    imageViewAvatar = (CircleImageView) findViewById(R.id.imageAvatar);

    currentUser = User.getCurrentUser();

    setOnClickListeners(buttonClose,
        buttonSaveChanges,
        buttonChoosePicture);

    onUserUpdate();
    onUpdateUserAvatar();
  }

  /**
   * Method which provide the updating UI with accordance to the currentUser information
   */
  private void onUserUpdate() {
    currentUser = User.getCurrentUser();
    if (currentUser != null) {
      textEmail.setText(currentUser.getEmail());
      editFirstName.setText(currentUser.getFirstName());
      editLastName.setText(currentUser.getLastName());
    }
  }

  /**
   * Method which provide the updating of the user avatar
   */
  private void onUpdateUserAvatar() {
    if ((currentUser != null) && (currentUser.getAvatarUrl() != null)) {
      Glide.with(this)
          .load(User.getCurrentUser().getAvatarUrl())
          .placeholder(R.mipmap.ic_user)
              //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
          .centerCrop()
          .into(imageViewAvatar);
    }
  }

  /**
   * Method which provide the save changes with accordance to UI
   */
  private void onSaveChanges() {
    if (StringUtil.isEmpty(getSimpleText(editFirstName))) {
      showMessage("First name is required");
      editFirstName.requestFocus();
      return;
    }
    if (StringUtil.isEmpty(getSimpleText(editLastName))) {
      showMessage("Last name is required");
      editLastName.requestFocus();
      return;
    }

    showProgress(true);

    UpdateProfileRequest request = new UpdateProfileRequest.Builder()
        .firstName(getSimpleText(editFirstName))
        .lastName(getSimpleText(editLastName))
        .build();

    User.updateProfile(request, new ApiCallback<User>() {
      @Override
      public void success(User user) {
        showProgress(false);
        showMessage("You\'ve updated your profile");
      }

      @Override
      public void failure(ApiError apiError) {
        showProgress(false);
        AppLogger.error(this, apiError.toString());
      }
    });
  }

  /**
   * Method which provide the progress showing
   *
   * @param isNeedShowProgress
   */
  protected void showProgress(boolean isNeedShowProgress) {
    if (isNeedShowProgress == true) {
      viewProgress.setVisibility(View.VISIBLE);
    } else {
      viewProgress.setVisibility(View.GONE);
    }
  }

  /**
   * Method which provide the updating of the server avatar
   */
  private void updateServerAvatar(Bitmap bitmap, final String mimeType) {
    if(null != User.getCurrentUser()) {
      User.getCurrentUser().setAvatar(bitmap, mimeType, new ApiCallback<String>() {
        @Override public void success(String s) {
          AppLogger.info(this, "Set user avatar successfuly " + s);
        }

        @Override public void failure(ApiError apiError) {
          AppLogger.error(this, String.format("Failed to set user avatar %s", apiError.toString()));
        }
      });
    } else {
      showMessage("Current user session timeout, please logout and login");
    }
  }

  @Override
  public void onClick(View v) {
    hideKeyboard();
    if (v.getId() == R.id.buttonClose) {
      onBackPressed();
    } else if (v.getId() ==  R.id.buttonSaveChanges) {
      onSaveChanges();
    } else if (v.getId() == R.id.buttonChoosePicture) {
      startActivityForResults(IntentHelper.pickImage());
    }

  }

  @Override
  public void onBackPressed() {
    showProgress(false);
    startActivity(HomeActivity.class, true);

  }

  @Override
  protected void onActivityResult(int requestCode, Intent data) {
    if (requestCode == RESULT_LOAD_IMAGE && null != data) {
      Uri selectedImage = data.getData();
      final String picturePath = FileHelper.getPath(this, selectedImage);
      if(null != picturePath) {
        //setImageBySource(imageViewAvatar, picturePath);
        Glide.with(this).load(selectedImage).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>(200, 200) {
          @Override
          public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
            setImageFromBitmap(bitmap, Attachment.getMimeType(picturePath, Attachment.MIME_TYPE_IMAGE));
          }
        });
      } else {
        Log.w(TAG, "Failed to load image from Uri " + selectedImage + ", trying to use inputstream");

        Bitmap bitmap = FileHelper.getImageBitmap(this, selectedImage);
        if(null != bitmap) {
          setImageFromBitmap(bitmap, FileHelper.getMimeType(this, selectedImage));
        }
      }
    }
  }

  private void setImageFromBitmap(final Bitmap bitmap, final String mimeType) {
    imageViewAvatar.setImageBitmap(bitmap);
    runOnMainThread(0.5, new OnActionPerformer() {
      @Override public void onActionPerform() {
        updateServerAvatar(bitmap, mimeType);
      }
    });
  }
}
