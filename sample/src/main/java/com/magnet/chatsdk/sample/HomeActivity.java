package com.magnet.chatsdk.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.magnet.magnetchat.ui.activities.ChatListActivity;
import com.magnet.max.android.User;

public class HomeActivity extends AppCompatActivity {

  @InjectView(R.id.btnChats)
  Button btnChats;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    ButterKnife.inject(this);

    Toolbar toolbar = (Toolbar) findViewById(com.magnet.magnetchat.R.id.toolbar);
    //setSupportActionBar(toolbar);
    toolbar.setTitle(User.getCurrentUser().getDisplayName());

    //getSupportActionBar().setTitle(User.getCurrentUser().getDisplayName());
  }

  @OnClick(R.id.btnChats)
  public void onChatsButtonClick(View v) {
    startActivity(new Intent(this, ChatListActivity.class));
  }
}
