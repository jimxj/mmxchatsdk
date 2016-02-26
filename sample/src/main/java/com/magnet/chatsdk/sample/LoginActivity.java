package com.magnet.chatsdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.CheckBox;
import com.magnet.chatsdk.sample.R;
import com.magnet.magnetchat.core.managers.InternetConnectionManager;
import com.magnet.magnetchat.helpers.UserHelper;
import com.magnet.magnetchat.ui.activities.BaseActivity;
import com.magnet.magnetchat.ui.activities.ChatListActivity;
import com.magnet.magnetchat.util.Logger;
import com.magnet.max.android.ApiError;
import com.magnet.max.android.User;
import com.magnet.mmx.client.api.MMX;
import java.net.SocketTimeoutException;

public class LoginActivity extends BaseActivity {
  private static final String TAG = LoginActivity.class.getSimpleName();

  CheckBox remember;
  AppCompatEditText editEmail;
  AppCompatEditText editPassword;
  View viewProgress;

  @Override
  protected int getLayoutResource() {
    return R.layout.activity_login;
  }

  @Override
  protected int getBaseViewID() {
    return R.id.main_content;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    remember = (CheckBox) findViewById(R.id.loginRemember);
    editEmail = (AppCompatEditText) findViewById(R.id.loginEmail);
    editPassword = (AppCompatEditText) findViewById(R.id.loginPassword);
    viewProgress = (View) findViewById(R.id.viewProgress);

    setOnClickListeners(
        R.id.loginCreateAccountBtn, R.id.loginSignInBtn);
    //Logger.debug("SessionStatus", User.getSessionStatus());
    //if (User.SessionStatus.LoggedIn == User.getSessionStatus()) {
    //    goToHomeActivity();
    //} else if (User.SessionStatus.CanResume == User.getSessionStatus()) {
    //    User.resumeSession(new ApiCallback<Boolean>() {
    //        @Override
    //        public void success(Boolean aBoolean) {
    //            if (aBoolean) {
    //                goToHomeActivity();
    //            } else {
    //                handleError("");
    //            }
    //        }
    //
    //        @Override
    //        public void failure(ApiError apiError) {
    //            handleError(apiError.getMessage());
    //        }
    //
    //        private void handleError(String errorMessage) {
    //            Logger.debug(TAG, "Failed to resume session due to ", errorMessage);
    //            changeLoginMode(false);
    //        }
    //    });
    //}
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (User.getCurrentUser() != null) {
      changeLoginMode(true);
    } else {
      changeLoginMode(false);
    }

  }

  @Override
  public void onClick(View v) {
    hideKeyboard();
    if (v.getId() == R.id.loginCreateAccountBtn ) {
      startActivity(new Intent(this, RegisterActivity.class));
    } else if (v.getId() == R.id.loginSignInBtn) {
      runLoginFromFields();
    }
  }

  private void runLoginFromFields() {
    if (InternetConnectionManager.getInstance().isAnyConnectionAvailable()) {
      final String email = getSimpleText(editEmail);
      final String password = getSimpleText(editPassword);
      boolean shouldRemember = remember.isChecked();
      if (checkStrings(email, password)) {
        changeLoginMode(true);
        UserHelper.login(email, password, shouldRemember, loginListener);
      } else {
        showLoginFailed();
      }
    } else {
      showNoConnection();
    }
  }

  private void showLoginFailed() {
    showMessage("Email or password is incorrect", "Please check them and try again");
    changeLoginMode(false);
  }

  private void showLoginErrorCause(String cause) {
    showMessage(cause + " Please try again");
    changeLoginMode(false);
  }

  private void showNoConnection() {
    showMessage("No connection", "Check Internet connection and try again");
    changeLoginMode(false);
  }

  private void changeLoginMode(boolean runLogining) {
    if (runLogining == true) {
      viewProgress.setVisibility(View.VISIBLE);
    } else {
      viewProgress.setVisibility(View.GONE);
    }
  }

  private void goToHomeActivity() {
    startActivity(ChatListActivity.class, true);
  }

  private void goToHomeActivity(boolean changeLoginMode) {
    startActivity(ChatListActivity.class, true);
    changeLoginMode(changeLoginMode);
  }

  private UserHelper.OnLoginListener loginListener = new UserHelper.OnLoginListener() {
    @Override
    public void onSuccess() {
      goToHomeActivity(false);
    }

    @Override
    public void onFailedLogin(ApiError apiError) {
      Logger.error("login", apiError);
      changeLoginMode(false);
      if (apiError.getMessage().contains(MMX.FailureCode.BAD_REQUEST.getDescription())) {
        showLoginErrorCause("A bad request submitted to the server.");
      } else if (apiError.getMessage().contains(MMX.FailureCode.SERVER_AUTH_FAILED.getDescription())) {
        showLoginErrorCause("Server authentication failure.");
      } else if (apiError.getMessage().contains(MMX.FailureCode.DEVICE_CONCURRENT_LOGIN.getDescription())) {
        showLoginErrorCause("Concurrent logins are attempted.");
      } else if (apiError.getMessage().contains(MMX.FailureCode.DEVICE_ERROR.getDescription())) {
        showLoginErrorCause("A client error.");
      } else if (apiError.getMessage().contains(MMX.FailureCode.SERVER_ERROR.getDescription())) {
        showLoginErrorCause("A server error.");
      } else if (apiError.getMessage().contains(MMX.FailureCode.SERVICE_UNAVAILABLE.getDescription())) {
        showLoginErrorCause("Service is not available due to network or server issue.");
      } else if(null != apiError.getCause() && apiError.getCause() instanceof SocketTimeoutException) {
        showLoginErrorCause("Request timeout. Please check network.");
      } else {
        showLoginFailed();
      }
    }
  };

}

