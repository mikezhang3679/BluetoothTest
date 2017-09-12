package com.guangyao.bluetoothtest.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.guangyao.bluetoothtest.App;
import com.guangyao.bluetoothtest.R;
import com.guangyao.bluetoothtest.bean.BaseEvent;
import com.guangyao.bluetoothtest.command.CommandManager;
import com.guangyao.bluetoothtest.constans.Constans;
import com.guangyao.bluetoothtest.service.BluetoothLeService;
import com.guangyao.bluetoothtest.utils.DataHandlerUtils;
import com.guangyao.bluetoothtest.utils.MyUtils;
import com.guangyao.bluetoothtest.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;



/**
 * Created by Zhang on 2017/6/30.
 * email:zgy921229@foxmail.com
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SEARCH = 1;
    private TextView device_address;
    private TextView device_name;
    private TextView test_result;
    private String address;
    private String name;
    private GridView gridView;
    private List<String> list;
    private CommandManager manager;
    private boolean isTestHR;
    private boolean isTestHR2;
    private boolean isTestHO;
    private boolean isTestHO2;
    private boolean isTestBP;
    private boolean isTestBP2;
    private MyAdapter myAdapter;
    private LocationManager locationManager;
    private Context mContext;
    private String mDeviceAddress;
    private boolean shakephoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        mContext=this;
        manager = CommandManager.getInstance(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initView();
        initdata();

        Click();
        mDeviceAddress = SPUtils.getString(mContext, SPUtils.DEVICE_ADDRESS, "");
       if (!"".equals(mDeviceAddress)){
           address=mDeviceAddress;
       }

        Integer integer = Integer.valueOf("0266", 16);
        Log.i("zgy", "integer"+String.valueOf(integer));

    }

    private void initView() {
        device_address = (TextView) findViewById(R.id.device_address);
        device_name = (TextView) findViewById(R.id.device_name);
        test_result = (TextView) findViewById(R.id.test_result);
        gridView = (GridView) findViewById(R.id.gridView);
    }

    private void Click() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (App.mConnected) {

                    switch (i) {
                        case 0:
                            manager.motorText(1); //find_braclet

                            break;
                        case 1:
                            manager.screenShow(1);//screen_show
                            break;
                        case 2:
                            manager.smartWarnInfo(7, 2, "微克科技");//text

                            break;
                        case 3:
                            manager.rssiTest();//rssi
                            break;
                        case 4:
                            manager.buttonClick();//button
                            break;
                        case 5:
                            manager.getBatteryInfo();//battery

                            break;
                        case 6:
                            manager.sensorTest();//three_six
                            break;

                        case 7:
                            manager.heartRateSensorTest();//heart_senor

//

                            break;
                        case 8: //clear_data
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.warn);
                            builder.setMessage(R.string.sure);
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    manager.setClearData();

                                }
                            });
                            builder.show();
                            break;
                        case 9: //restore
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                            builder2.setTitle(R.string.warn);
                            builder2.setMessage(R.string.restore_factory);
                            builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                            builder2.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    manager.setClearData();
                                    manager.Shutdown();

                                }
                            });
                            builder2.show();

                            break;
                        case 10://pull_to_refresh
                            manager.setSyncData(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);//2.6
                            break;

                        case 11://摇摇拍照
                            if (!shakephoto){
                                shakephoto=true;
                                manager.setSharkTakePhoto(1);
                            }else {
                                manager.setSharkTakePhoto(0);
                                shakephoto=false;
                            }
                            break;

                        case 12://防丢
                            manager.setAntiLostAlert(1);
                            break;
                        case 13://版本号
                            manager.getVersionInfo();
                            break;
                        case 14://同步时间
                            manager.setTimeSync();

                            break;
                        case 15://跌倒
                            manager.falldownWarn(1);
                            break;
                        case 16://心率单次测量
                            if (isTestHR2 || isTestHO || isTestHO2 || isTestBP || isTestBP2) {
                                Toast.makeText(MainActivity.this, "请先关闭其他测量", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!isTestHR) {
                                    manager.realTimeAndOnceMeasure(0X09, 1);//单次测量
                                    isTestHR = true;
                                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                } else {
                                    manager.realTimeAndOnceMeasure(0X09, 0);//单次测量
                                    isTestHR = false;
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }


                            break;
                        case 17:// 心率实时测量
                            if (isTestHR || isTestHO || isTestHO2 || isTestBP || isTestBP2) {
                                Toast.makeText(MainActivity.this, "请先关闭其他测量", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!isTestHR2) {
                                    manager.realTimeAndOnceMeasure(0x0A, 1);//实时测量
                                    isTestHR2 = true;
                                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                } else {
                                    manager.realTimeAndOnceMeasure(0x0A, 0);//实时测量
                                    isTestHR2 = false;
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }
                            break;
                        case 18://血氧单次测量
                            if (isTestHR || isTestHR2 || isTestHO2 || isTestBP || isTestBP2) {
                                Toast.makeText(MainActivity.this, "请先关闭其他测量", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!isTestHO) {
                                    manager.realTimeAndOnceMeasure(0X11, 1);//实时测量
                                    isTestHO = true;
                                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                } else {
                                    manager.realTimeAndOnceMeasure(0X11, 0);//实时测量
                                    isTestHO = false;
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }
                            break;
                        case 19:// 血氧实时测量
                            if (isTestHR || isTestHR2 || isTestHO || isTestBP || isTestBP2) {
                                Toast.makeText(MainActivity.this, "请先关闭其他测量", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!isTestHO2) {
                                    manager.realTimeAndOnceMeasure(0X12, 1);//实时测量
                                    isTestHO2 = true;
                                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                } else {
                                    manager.realTimeAndOnceMeasure(0X12, 0);//实时测量
                                    isTestHO2 = false;
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }
                            break;
                        case 20://  血压单次测量
                            if (isTestHR || isTestHR2 || isTestHO || isTestHO2 || isTestBP2) {
                                Toast.makeText(MainActivity.this, "请先关闭其他测量", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!isTestBP) {
                                    manager.realTimeAndOnceMeasure(0X21, 1);//实时测量
                                    isTestBP = true;
                                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                } else {
                                    manager.realTimeAndOnceMeasure(0X21, 0);//实时测量
                                    isTestBP = false;
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }
                            break;
                        case 21:// 血压实时测量
                            if (isTestHR || isTestHR2 || isTestHO || isTestHO2 || isTestBP) {
                                Toast.makeText(MainActivity.this, "请先关闭其他测量", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!isTestBP2) {
                                    manager.realTimeAndOnceMeasure(0X22, 1);//实时测量
                                    isTestBP2 = true;
                                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                } else {
                                    manager.realTimeAndOnceMeasure(0X22, 0);//实时测量
                                    isTestBP2 = false;
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                }
                            }
                            break;
                        case 22:// clockwise
                            manager.setPointer(0);//顺时针


                            break;

                        case 23://anti_clockwise
                            manager.setPointer(1);//逆时针


                            break;

                        case 24://stop_pointer

                            manager.setPointer(2);//停止

                            break;

                        case 25://ecg_data

                            Intent intent = new Intent(MainActivity.this, EcgDataActivity.class);
                            startActivity(intent);
                            break;
                        case 26://pray

                            Intent intent2 = new Intent(MainActivity.this, PrayActivity.class);
                            startActivity(intent2);
                            break;
                        case 27:

                            manager.hourlyMeasure(1);//3.8

                            break;


                        default:
                            break;
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.disconnect, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("zgy", "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        device_address.setText(address);
        device_name.setText(name);
        invalidateOptionsMenu();//更新菜单栏


    }

    private IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!App.mConnected) {
            menu.findItem(R.id.disconnect_ble).setVisible(false);
        } else {
            menu.findItem(R.id.disconnect_ble).setVisible(true);
        }
        if (!App.isConnecting) {
            menu.findItem(R.id.menu_refresh).setActionView(
                    null);
        } else {
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);//正在连接
        }
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_ble:


                if (!MyUtils.isOpenLocationService(mContext)) {
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

                }else {
                    Intent intent = new Intent(MainActivity.this, DeviceScanActivity.class);
                    startActivityForResult(intent, REQUEST_SEARCH);
                }

                break;
            case R.id.disconnect_ble:
                App.mBluetoothLeService.disconnect();
               SPUtils.putString(mContext, SPUtils.DEVICE_ADDRESS, "");//下次不会再重连

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SEARCH && resultCode == RESULT_OK) {
            address = data.getStringExtra(Constans.ADDRESS);
            name = data.getStringExtra(Constans.NAME);
            if (!TextUtils.isEmpty(address)) {
                App.mBluetoothLeService.connect(address);
                App.isConnecting = true;
                invalidateOptionsMenu();//显示正在连接 ...
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }


    //接收蓝牙状态改变的广播
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                App.isConnecting = false;
                //todo 更改界面ui
                device_address.setText(address);
                device_name.setText(name);
                invalidateOptionsMenu();//更新菜单栏
                Log.d("BluetoothLeService", "连上");
                SPUtils.putString(context,SPUtils.DEVICE_ADDRESS,address);



            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                //todo 更改界面ui
                device_address.setText(R.string.disconnect2);
                device_name.setText("");
                invalidateOptionsMenu();//更新菜单栏
                Log.d("BluetoothLeService", "断开");





            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                final byte[] txValue = intent
                        .getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Log.i("BluetoothLeService", "Received command" + DataHandlerUtils.bytesToHexStr(txValue));

                List<Integer> datas = DataHandlerUtils.bytesToArrayList(txValue);

                Log.i("zgy", datas.toString());

                //RSSI
                if (datas.get(4) == 0XB5) {// [171, 0, 4, 255, 181, 128, 72]
                    Integer rssi = datas.get(6);
                    Log.d("zgy", "RSSI" + rssi);
                    test_result.setText("-" + rssi);
                }

                //按键测试
                if (datas.get(4) == 0XB6) {//[171, 0, 4, 255, 182, 128, 0]
                    Integer button = datas.get(6);
                    if (button == 0) {
                        test_result.setText(R.string.no_press);
                    } else if (button == 1) {
                        test_result.setText(R.string.press);

                    }

                }
                //充电 、电量
                if (datas.get(4) == 0X91) {//[171, 0, 5, 255, 145, 128, 0, 100]
                    Integer integer = datas.get(6);//是否充电
                    Integer integer1 = datas.get(7);//电量多少
                    if (integer == 0) {
                        test_result.setText(getString(R.string.no_charge) + getString(R.string.electricity) + integer1 + "%");
                    } else if (integer == 1) {
                        test_result.setText(getString(R.string.charging) + getString(R.string.electricity) + integer1 + "%");
                    }
                }

                //三轴传感器
                if (datas.get(4) == 0XB3) {//[171, 0, 5, 255, 179, 128, 1, 1]
                    Integer integer1 = datas.get(6);//通信是否正常
                    Integer integer2 = datas.get(7);//初始化是否成功
                    if (integer1 == 0) {
                        if (integer2 == 0) {
                            test_result.setText(getString(R.string.abnormal_communication) + getString(R.string.Initialization_unsuccessful));
                        } else if (integer2 == 1) {
                            test_result.setText(getString(R.string.abnormal_communication) + getString(R.string.Initialize_successfully));

                        }
                    } else if (integer1 == 1) {
                        if (integer2 == 0) {
                            test_result.setText(getString(R.string.communication_normal) + getString(R.string.Initialization_unsuccessful));

                        } else if (integer2 == 1) {
                            test_result.setText(getString(R.string.communication_normal) + getString(R.string.Initialize_successfully));

                        }
                    }

                }

                //心率传感器
                if (datas.get(4) == 0XB4) {//[171, 0, 4, 255, 180, 128, 1]
                    Integer integer = datas.get(6);
                    if (integer == 0) {
                        test_result.setText(getString(R.string.abnormal_communication));

                    } else if (integer == 1) {
                        test_result.setText(getString(R.string.communication_normal));

                    }

                }
                //测量心率
                if (datas.get(4) == 0x31) {//[171, 0, 5, 255, 49, 10, 0, 190]   [171, 0, 5, 255, 49, 10, 84, 48]
                    Integer integer = datas.get(6);
                    test_result.setText(String.valueOf(integer));
                }

            }
        }
    };


    private void initdata() {
        list = new ArrayList<>();
        list.add(getString(R.string.find_braclet));
        list.add(getString(R.string.screen_show));
        list.add(getString(R.string.text));
        list.add(getString(R.string.rssi));
        list.add(getString(R.string.button));
        list.add(getString(R.string.battery));
        list.add(getString(R.string.three_six));
        list.add(getString(R.string.heart_senor));
        list.add(getString(R.string.clear_data));
        list.add(getString(R.string.restore));
        list.add(getString(R.string.pull_to_refresh));
        list.add(getString(R.string.shake_to_photo));
        list.add(getString(R.string.anti_lost));
        list.add(getString(R.string.version_code));
        list.add(getString(R.string.sync_time));
        list.add(getString(R.string.fall));
        list.add(getString(R.string.hr_one));
        list.add(getString(R.string.real_time));
        list.add(getString(R.string.ho_once));
        list.add(getString(R.string.ho_real_time));
        list.add(getString(R.string.bp_once));
        list.add(getString(R.string.bp_real_time));

        //加点
        list.add(getString(R.string.clockwise));
        list.add(getString(R.string.anti_clockwise));
        list.add(getString(R.string.stop_pointer));

        list.add(getString(R.string.ecg_data));

        //以色列祈祷app
        list.add(getString(R.string.pray));


        list.add(getString(R.string.hourly_mearure));


        myAdapter = new MyAdapter(list);
        gridView.setAdapter(myAdapter);

    }


    class MyAdapter extends BaseAdapter {
        private List<String> list;

        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.channel_item, null);
                viewHolder.text = (TextView) convertView.findViewById(R.id.channel_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.text.setText(list.get(i));
            return convertView;
        }
    }

    class ViewHolder {
        TextView text;
    }


    public void onEventMainThread(BaseEvent baseEvent) {
        switch (baseEvent.getEventType()){
            case ONSERVICECONNECTED:
               Log.i("zgy","ONSERVICECONNECTED");

                Log.i("zgy","mDeviceAddress"+ mDeviceAddress);
            if (!"".equals(mDeviceAddress)) {
                Log.i("zgy","connect");
                if (App.BLE_ON){
                    App.mBluetoothLeService.connect(mDeviceAddress);
                    App.isConnecting = true;
                    invalidateOptionsMenu();//显示正在连接 ...
                }else {
                    Toast.makeText(this,"请开启蓝牙",Toast.LENGTH_SHORT).show();
                }

            }
                break;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
