/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;

public class BindingHolder {
  protected ViewDataBinding binding;

  public ViewDataBinding getBinding() {
    return binding;
  }

  public void setBinding(ViewDataBinding binding) {
    this.binding = binding;
  }
}
