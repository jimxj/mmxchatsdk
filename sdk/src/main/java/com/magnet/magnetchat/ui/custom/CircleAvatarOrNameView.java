/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.magnet.magnetchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CircleAvatarOrNameView extends FrameLayout {

  private CircleImageView ivAvatar;
  private CircleNameView cnvName;

  public CircleAvatarOrNameView(Context context) {
    this(context, null);
  }

  public CircleAvatarOrNameView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CircleAvatarOrNameView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    //Inflate and attach the content
    LayoutInflater.from(context).inflate(R.layout.view_circle_avatar_or_name, this);

    ivAvatar = (CircleImageView) findViewById(R.id.imageUserAvatar);
    cnvName = (CircleNameView) findViewById(R.id.viewUserAvatar);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleAvatarOrNameView, 0, 0);

    // ImageView
    //int radius = a.getDimensionPixelSize(R.styleable.CircleAvatarOrNameView_radius, 0);
    //int diameter = radius * 2;
    //ViewGroup.LayoutParams imageParams= ivAvatar.getLayoutParams();
    //imageParams.width = diameter;
    //imageParams.height = diameter;
    //ivAvatar.setLayoutParams(imageParams);
    //
    //// NameView
    //ViewGroup.LayoutParams params= cnvName.getLayoutParams();
    //params.width = diameter;
    //params.height = diameter;
    //cnvName.setLayoutParams(params);

    //int backgroundColor = a.getColor(R.styleable.CircleAvatarOrNameView_colorBackground, 0);
    //cnvName.setBackgroundColor(backgroundColor);

    //int textColor = a.getColor(R.styleable.CircleAvatarOrNameView_colorText, 0);
    //cnvName.setProperties(new CircleNameViewProperties.PropertyBuilder().addTextColor(textColor).build());

  }

  @BindingAdapter(value = {"imageResId", "userName", "avatarUrl"}, requireAll = false)
  public static void setAvatarOrName(CircleAvatarOrNameView view, @DrawableRes
  int imageResId, String name, String avatarUrl) {
    if(imageResId > 0) {
      view.setAvatar(imageResId);
    } else {
      view.setAvatarAndName(avatarUrl, name);
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
  }

  public void setAvatarAndName(final String url, final String name) {
    if (!TextUtils.isEmpty(url)) {
      Glide.with(getContext()).load(url).listener(new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
          setName(name);
          return false;
        }

        @Override public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
          showImage();
          return false;
        }
      }).into(ivAvatar);
    } else {
      setName(name);
    }
  }

  public void setName(String name) {
    cnvName.setUserName(name);
    showName();
  }

  public void setAvatar(int imageResId) {
    ivAvatar.setImageResource(imageResId);
    showImage();
  }

  private void showImage() {
    if(ivAvatar.getVisibility() != VISIBLE) {
      ivAvatar.setVisibility(VISIBLE);
    }
    if(cnvName.getVisibility() == VISIBLE) {
      cnvName.setVisibility(GONE);
    }
  }

  private void showName() {
    if(cnvName.getVisibility() != VISIBLE) {
      cnvName.setVisibility(VISIBLE);
    }
    if(ivAvatar.getVisibility() == VISIBLE) {
      ivAvatar.setVisibility(GONE);
    }
  }
}
