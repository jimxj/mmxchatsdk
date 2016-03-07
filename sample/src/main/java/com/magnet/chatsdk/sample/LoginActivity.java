package com.magnet.chatsdk.sample;

import android.content.Intent;
import android.os.Bundle;

import com.magnet.magnetchat.ui.activities.BaseActivity;
import com.magnet.magnetchat.ui.views.section.login.LoginView;
import com.magnet.max.android.ApiError;

public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private LoginView viewLogin;

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
        viewLogin = (LoginView) findViewById(R.id.viewLogin);
        viewLogin.setOnLoginEventListener(loginEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewLogin.onResume();
    }

    private void goToHomeActivity() {
        startActivity(HomeActivity.class, true);
    }

    private void goToRegisterActivity() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    /**
     * Method which provide the listening of the login view actions
     */
    private final LoginView.OnLoginEventListener loginEventListener = new LoginView.OnLoginEventListener() {
        @Override
        public void onLoginPerformed() {
            goToHomeActivity();
        }

        @Override
        public void onRegisterPressed() {
            goToRegisterActivity();
        }

        @Override
        public void onLoginError(ApiError apiError, String message) {
            showMessage(message);
        }
    };
}

