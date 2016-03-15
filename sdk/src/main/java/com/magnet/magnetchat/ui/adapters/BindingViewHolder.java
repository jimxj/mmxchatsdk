/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.ui.adapters;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public abstract class BindingViewHolder<T, B extends ViewDataBinding> extends RecyclerView.ViewHolder {
  protected B mBinding;

  public BindingViewHolder(@NonNull B viewDataBinding) {
    super(viewDataBinding.getRoot());
    this.mBinding = viewDataBinding;
  }

  abstract public void bindTo(@NonNull T item);
}
