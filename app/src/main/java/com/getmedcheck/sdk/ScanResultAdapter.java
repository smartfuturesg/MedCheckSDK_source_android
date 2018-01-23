package com.getmedcheck.sdk;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getmedcheck.lib.model.BleDevice;

public class ScanResultAdapter extends BaseAdapter<BleDevice, ScanResultAdapter.ViewHolder> {

    public ScanResultAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BleDevice bleDevice = getListItem(position);
        holder.tvDeviceName.setText(bleDevice.getDeviceName());
        holder.tvDeviceAddress.setText(bleDevice.getMacAddress());
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvDeviceName;
        TextView tvDeviceAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceAddress = itemView.findViewById(R.id.tvDeviceAddress);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getListItem(getAdapterPosition()), getAdapterPosition());
            }
        }
    }
}
