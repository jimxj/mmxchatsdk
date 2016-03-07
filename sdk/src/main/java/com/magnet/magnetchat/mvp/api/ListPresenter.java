/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.magnetchat.mvp.api;

public interface ListPresenter<T> {

  void onLoad(int offset, int limit);

  void onSearch(String query, String sort);

  void onSearchReset();

  void onItemSelect(int position, T item);

  void onItemLongClick(int position, T item);
}