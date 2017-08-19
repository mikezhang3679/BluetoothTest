/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guangyao.bluetoothtest.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.guangyao.bluetoothtest.App;
import com.guangyao.bluetoothtest.SampleGattAttributes;
import com.guangyao.bluetoothtest.constans.BleConstans;
import com.guangyao.bluetoothtest.constans.Constans;
import com.guangyao.bluetoothtest.utils.DataHandlerUtils;
import com.guangyao.bluetoothtest.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public static final UUID CCCD = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID
            .fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID
            .fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID
            .fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private static final int SEND_PACKET_SIZE = 20;
    private static final int FREE = 0;
    private static final int SENDING = 1;
    private static final int RECEIVING = 2;


    private int ble_status = FREE;
    private int packet_counter = 0;
    private int send_data_pointer = 0;
    private byte[] send_data = null;
    private boolean first_packet = false;
    private boolean final_packet = false;
    private boolean packet_send = false;
    private Timer mTimer;
    private int time_out_counter = 0;
    private int TIMER_INTERVAL = 100;
    private int TIME_OUT_LIMIT = 100;
    public ArrayList<byte[]> data_queue = new ArrayList<>();
    boolean sendingStoredData = false;
    private Timer timer;
    private boolean hasRecevieDisConnected;


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start com.wakeup.bluetoothtest.service discovery:" +
                        mBluetoothGatt.discoverServices());
                if (timer != null) timer.cancel();
                App.mConnected = true;
                hasRecevieDisConnected = false;

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                App.mConnected = false;
                App.mBluetoothLeService.close();
                if (!hasRecevieDisConnected) {
                    hasRecevieDisConnected = true;
                    if (!"".equals(SPUtils.getString(getApplicationContext(), SPUtils.DEVICE_ADDRESS, ""))) {
                        //开启一个定时器
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, SPUtils.getString(getApplicationContext(), SPUtils.DEVICE_ADDRESS, ""));
                                    if (App.BLE_ON){
                                        App.mBluetoothLeService.connect(SPUtils.getString(getApplicationContext(), SPUtils.DEVICE_ADDRESS, ""));
                                    }else {
                                        Log.i(TAG,"请开启蓝牙");
                                    }
                            }
                        }, 2000, 4000);

                    }
                }


            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                enableTXNotification();//允许接收蓝牙设备发送过来的数据

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }
    };
    private SendDataToBleReceiver sendDataToBleReceiver;

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }

        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            byte[] data = characteristic.getValue();
            Log.d(TAG, "broadcastUpdate: received from ble:" + DataHandlerUtils.bytesToHexStr(data));

            if (ble_status == FREE || ble_status == RECEIVING) {
                ble_status = RECEIVING;
                if (data != null) {
                    intent.putExtra(EXTRA_DATA, data);

                    sendBroadcast(intent);

                }
                ble_status = FREE;

            } else if (ble_status == SENDING) {
                if (final_packet) {
                    final_packet = false;
                }
                ble_status = FREE;
            }
        }


    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth com.wakeup.bluetoothtest.adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


    class SendDataToBleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BleConstans.ACTION_SEND_DATA_TO_BLE)) {
                byte[] send_data = intent.getByteArrayExtra(Constans.EXTRA_SEND_DATA_TO_BLE);
                if (send_data != null) {
                    BLE_send_data_set(send_data, false);
                }
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sendDataToBleReceiver = new SendDataToBleReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstans.ACTION_SEND_DATA_TO_BLE);
        LocalBroadcastManager.getInstance(this).registerReceiver(sendDataToBleReceiver, intentFilter);


        //监听蓝牙开关
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bluetoothStatusChangeReceiver, filter);


    }
    private final BroadcastReceiver bluetoothStatusChangeReceiver
            = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF){
                    Log.i(TAG,"BluetoothAdapter.STATE_OFF 蓝牙关闭了");// 蓝牙关闭了
                    App.BLE_ON=false;
//                    devicesGatts.clear();
//                    resetBluetooth();

                } else if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON){
//                    initBluetooth();
                    App.BLE_ON=true;
                    Log.i(TAG,"BluetoothAdapter.STATE_ON  蓝牙开启了");//// 蓝牙开启了

                }
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendDataToBleReceiver);
    }

    /**
     * 设置数据到内部缓冲区对BLE发送数据
     */
    private void BLE_send_data_set(byte[] data, boolean retry_status) {
        if (ble_status != FREE || mConnectionState != STATE_CONNECTED) {
            //蓝牙没有连接或是正在接受或发送数据，此时将要发送的指令加入集合
            if (sendingStoredData) {
                if (!retry_status) {
                    data_queue.add(data);
                }
                return;
            } else {
                data_queue.add(data);
                start_timer();
            }

        } else {
            ble_status = SENDING;

            if (data_queue.size() != 0) {
                send_data = data_queue.get(0);
                sendingStoredData = false;
            } else {
                send_data = data;
            }
            packet_counter = 0;
            send_data_pointer = 0;
            //第一个包
            first_packet = true;
            BLE_data_send();

            if (data_queue.size() != 0) {
                data_queue.remove(0);
            }

            if (data_queue.size() == 0) {
                if (mTimer != null) {
                    mTimer.cancel();
                }
            }
        }
    }

    /**
     * 定时器
     */
    private void start_timer() {
        sendingStoredData = true;
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer_Tick();
            }
        }, 100, TIMER_INTERVAL);
    }

    /**
     * @brief Interval timer function.
     */
    private void timer_Tick() {

        if (data_queue.size() != 0) {
            sendingStoredData = true;
            BLE_send_data_set(data_queue.get(0), true);
        }

        if (time_out_counter < TIME_OUT_LIMIT) {
            time_out_counter++;
        } else {
            ble_status = FREE;
            time_out_counter = 0;
        }
        return;
    }


    /**
     * @brief Send data using BLE. 发送数据到蓝牙
     */
    private void BLE_data_send() {
        int err_count = 0;
        int send_data_pointer_save;
        int wait_counter;
        boolean first_packet_save;
        while (!final_packet) {
            //不是最后一个包
            byte[] temp_buffer;
            send_data_pointer_save = send_data_pointer;
            first_packet_save = first_packet;
            if (first_packet) {
                //第一个包

                if ((send_data.length - send_data_pointer) > (SEND_PACKET_SIZE)) {
                    temp_buffer = new byte[SEND_PACKET_SIZE];//20
                    for (int i = 0; i < SEND_PACKET_SIZE; i++) {
                        //将原数组加入新创建的数组
                        temp_buffer[i] = send_data[send_data_pointer];
                        send_data_pointer++;
                    }
                } else {
                    //发送的数据包不大于20
                    temp_buffer = new byte[send_data.length - send_data_pointer];
                    for (int i = 0; i < temp_buffer.length; i++) {
                        //将原数组未发送的部分加入新创建的数组
                        temp_buffer[i] = send_data[send_data_pointer];
                        send_data_pointer++;
                    }
                    final_packet = true;
                }
                first_packet = false;
            } else {
                //不是第一个包
                if (send_data.length - send_data_pointer >= SEND_PACKET_SIZE) {
                    temp_buffer = new byte[SEND_PACKET_SIZE];
                    temp_buffer[0] = (byte) packet_counter;
                    for (int i = 1; i < SEND_PACKET_SIZE; i++) {
                        temp_buffer[i] = send_data[send_data_pointer];
                        send_data_pointer++;
                    }
                } else {
                    final_packet = true;
                    temp_buffer = new byte[send_data.length - send_data_pointer + 1];
                    temp_buffer[0] = (byte) packet_counter;
                    for (int i = 1; i < temp_buffer.length; i++) {
                        temp_buffer[i] = send_data[send_data_pointer];
                        send_data_pointer++;
                    }
                }
                packet_counter++;
            }
            packet_send = false;

            boolean status = writeRXCharacteristic(temp_buffer);
            if ((status == false) && (err_count < 3)) {
                err_count++;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                send_data_pointer = send_data_pointer_save;
                first_packet = first_packet_save;
                packet_counter--;
            }
            // Send Wait
            for (wait_counter = 0; wait_counter < 5; wait_counter++) {
                if (packet_send == true) {
                    break;
                }
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        final_packet = false;
        ble_status = FREE;
    }

    /**
     * @brief writeRXCharacteristic
     */
    public boolean writeRXCharacteristic(byte[] value) {
        BluetoothGattService RxService = mBluetoothGatt
                .getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return false;
        }

        BluetoothGattCharacteristic RxChar = RxService
                .getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            return false;
        }
        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        Log.d(TAG, "Send command：status：" + status + "-->" + DataHandlerUtils.bytesToHexStr(value));
        return status;
    }

    /**
     * @brief enableTXNotification
     */
    @SuppressLint("InlinedApi")
    public void enableTXNotification() {
        BluetoothGattService RxService = mBluetoothGatt
                .getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }

        BluetoothGattCharacteristic TxChar = RxService
                .getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

}
