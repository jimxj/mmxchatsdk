/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.views.helpers;

import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;

/**
 * Helper to bind additional attributes to widgets
 */
public class ViewBindingHelper {

  /**
   * additional attributes for ImageView
   * @param view
   * @param url
   * @param placeHolder
   */
  @BindingAdapter(value = {"android:src", "placeHolder"}, requireAll = false)
  public static void setImageUrl(ImageView view, String url, @DrawableRes int placeHolder) {
    DrawableTypeRequest drawableTypeRequest = null;
    if(!TextUtils.isEmpty(url)) {
      drawableTypeRequest = Glide.with(view.getContext()).load(url);
    }

    if(0 != placeHolder) {
      if(null == drawableTypeRequest) {
        drawableTypeRequest = Glide.with(view.getContext()).load(placeHolder);
      } else {
        drawableTypeRequest.placeholder(placeHolder);
      }
    }

    if(null != drawableTypeRequest) {
      drawableTypeRequest.into(view);
    }
  }

  /**
   * additional attributes to TextView
   * @param view
   * @param resourceId
   */
  @BindingAdapter(value = {"backgroundRes"}, requireAll = false)
  public static void setTextBackgroundResource(TextView view, @DrawableRes int resourceId) {
    view.setBackgroundResource(resourceId);
  }

}
