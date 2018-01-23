package com.getmedcheck.sdk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public abstract class BaseAdapter<T, S extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<S> {

    protected OnItemClickListener<T> onItemClickListener;
    protected Context context;
    private ArrayList<T> mArrayList = new ArrayList<>();

    public BaseAdapter(Context context) {
        this.context = context;
    }

    public void setDataArrayList(ArrayList<T> mArrayList) {
        this.mArrayList = mArrayList;
    }

    public void addItem(T object) {
        this.mArrayList.add(object);
        notifyDataSetChanged();
    }

    public void addItem(int index, T object) {
        if (index > mArrayList.size()) {
            index = mArrayList.size();
        }
        this.mArrayList.add(index, object);
        notifyDataSetChanged();
    }

    public void setItems(ArrayList<T> arrayList) {
        this.mArrayList.clear();
        this.mArrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void clear() {
        this.mArrayList.clear();
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<T> arrayList) {
        this.mArrayList.addAll(arrayList);
        notifyDataSetChanged();
    }

    public void remove(T item) {
        if (mArrayList.remove(item)) {
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ArrayList<T> getList() {
        return mArrayList;
    }

    public T getListItem(int position) {
        if (position >= mArrayList.size() && position < 0 && mArrayList.size() > 0 ) {
            return null;
        }
        return mArrayList.get(position);
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

}
