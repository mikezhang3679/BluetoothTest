package com.guangyao.bluetoothtest;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.guangyao.bluetoothtest.bean.BaseEvent;
import com.guangyao.bluetoothtest.service.BluetoothLeService;

import de.greenrobot.event.EventBus;


/**
 * Created by Zhang on 2017/4/22.
 */

public class App extends Application {
    private final static String TAG = App.class.getSimpleName();

    public static BluetoothLeService mBluetoothLeService;
    public static boolean mConnected = false;
    public static boolean isConnecting = false;
    public static boolean BLE_ON = false;


    @Override
    public void onCreate() {
        super.onCreate();
        bindBleService();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        BLE_ON=adapter.isEnabled();
        Log.i(TAG,"BLE_ON   "+BLE_ON);


    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("zgy", "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            Log.d("zgy", "onServiceConnected");

            EventBus.getDefault().post(new BaseEvent(BaseEvent.EventType.ONSERVICECONNECTED));


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void bindBleService() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }



}
