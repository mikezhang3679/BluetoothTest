package com.guangyao.bluetoothtest.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guangyao.bluetoothtest.R;
import com.guangyao.bluetoothtest.bean.DeviceBean;

import java.util.ArrayList;

/**
 * Created by Zhang on 2017/4/22.
 */

// Adapter for holding devices found through scanning.
public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<DeviceBean> deviceBeens;
    private Context context;
    private DeviceBean deviceBean;

    public LeDeviceListAdapter(Context context, ArrayList<DeviceBean> deviceBeens) {
        super();
        this.mLeDevices = new ArrayList<>();
        this.deviceBeens = deviceBeens;
        this.context = context;

    }

    public void addDevice(DeviceBean deviceBean) {
        if (!mLeDevices.contains(deviceBean.getDevice())) {
            deviceBeens.add(deviceBean);
        }

    }

    public void addDevice(BluetoothDevice deviceBean) {
        if (!mLeDevices.contains(deviceBean)) {
            mLeDevices.add(deviceBean);
        }

    }

    public BluetoothDevice getDevice(int position) {
        return deviceBeens.get(position).getDevice();
    }

    public void clear() {
        deviceBeens.clear();//先清除这个
        mLeDevices.clear();
    }

    public void sort() {
        Log.d("zgy", "sort: ");

        for (int i = 0; i < deviceBeens.size(); i++) {
            for (int j = i + 1; j < deviceBeens.size(); j++) {
                if (deviceBeens.get(i).getRssi() < deviceBeens.get(j).getRssi()) {
                    deviceBean = deviceBeens.get(i);
                    deviceBeens.set(i, deviceBeens.get(j));
                    deviceBeens.set(j, deviceBean);
                }
            }
        }

    }

    @Override
    public int getCount() {
        return deviceBeens.size();
    }

    @Override
    public Object getItem(int i) {
        return deviceBeens.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.rssi = (TextView) view.findViewById(R.id.rssi);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = deviceBeens.get(i).getDevice();
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.rssi.setText(String.valueOf(deviceBeens.get(i).getRssi()));

        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView rssi;
    }
}
