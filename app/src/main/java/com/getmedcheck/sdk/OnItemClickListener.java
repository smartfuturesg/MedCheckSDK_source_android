package com.getmedcheck.sdk;

import android.view.View;

public interface OnItemClickListener<T> {
    void onItemClick(View view, T object, int position);
}