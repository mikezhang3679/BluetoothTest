package com.guangyao.bluetoothtest.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.guangyao.bluetoothtest.R;
import com.guangyao.bluetoothtest.adapter.LeDeviceListAdapter;
import com.guangyao.bluetoothtest.bean.DeviceBean;
import com.guangyao.bluetoothtest.constans.Constans;
import com.guangyao.bluetoothtest.utils.MyUtils;

import java.util.ArrayList;


/**
 * Created by Zhang on 2017/4/21.
 * email:zgy921229@foxmail.com
 */

public class DeviceScanActivity extends AppCompatActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager;

    private static final int REQUEST_ENABLE_BT = 1;
    private Context context;
    private ListView listView;
    private ArrayList<DeviceBean> deviceBeens;
    private Handler mHandler;
    private boolean mScanning;
    private static final long SCAN_PERIOD = 10000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private Runnable runnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        context = this;
        mHandler = new Handler();

        getSupportActionBar().setTitle(R.string.device_list);
        listView = (ListView) findViewById(R.id.listview);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.not_support, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initPermission();//6.0权限


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                if (device == null) {
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(Constans.ADDRESS, device.getAddress());
                intent.putExtra(Constans.NAME, device.getName());
                setResult(RESULT_OK, intent);
                if (mScanning) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                finish();

            }
        });
    }


    private void initPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.need_location);
                builder.setMessage(R.string.location_warn);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        deviceBeens = new ArrayList<>();
        mLeDeviceListAdapter = new LeDeviceListAdapter(context, deviceBeens);
        listView.setAdapter(mLeDeviceListAdapter);

        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
             runnable = new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            };
            mHandler.postDelayed(runnable, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, final int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DeviceBean deviceBean = new DeviceBean();
                    deviceBean.setDevice(bluetoothDevice);
                    deviceBean.setRssi(i);
                    mLeDeviceListAdapter.addDevice(deviceBean);
                    mLeDeviceListAdapter.addDevice(bluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();

                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_list, menu);

        if (!mScanning) {
            menu.findItem(R.id.stop).setVisible(false);
            menu.findItem(R.id.re_search).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.stop).setVisible(true);
            menu.findItem(R.id.re_search).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);

        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.re_search:
                if (!bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                    if (!MyUtils.isOpenLocationService(context)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.avaliable_gps);
                    builder.setTitle(R.string.notify);
                    builder.setPositiveButton(R.string.common_sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent callGPSSettingIntent = new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    }).show();

                     }

                }


                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.sort:
                mLeDeviceListAdapter.sort();
                mLeDeviceListAdapter.notifyDataSetChanged();
                break;
            case R.id.stop:
                mHandler.removeCallbacks(runnable);
                scanLeDevice(false);
                break;
        }

        return super.onOptionsItemSelected(item);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("zgy", "coarse location permission granted");

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.function_limited);
                    builder.setMessage(R.string.location_refused);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }

                break;
        }
    }
}
