package com.guangyao.bluetoothtest.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.guangyao.bluetoothtest.App;
import com.guangyao.bluetoothtest.R;
import com.guangyao.bluetoothtest.bean.PointBean;
import com.guangyao.bluetoothtest.command.CommandManager;
import com.guangyao.bluetoothtest.service.BluetoothLeService;
import com.guangyao.bluetoothtest.utils.DataHandlerUtils;
import com.guangyao.bluetoothtest.view.PathView2;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


/**
 * Created by Zhang on 2017/6/30.
 * email:zgy921229@foxmail.com
 */

public class EcgDataActivity extends AppCompatActivity {
    private final static String TAG = EcgDataActivity.class.getSimpleName();
    private static final int DRAWPATH = 1;
    private static final int DRAWPATH2 = 2;

    private TextView tv_ecgdata;
    private CommandManager manager;
    private String text = "";
    private int i;
    private ScrollView scrollView;

    private List<List<Integer>> totalList;
    private PathView2 pathView2;
    private List<PointBean> pointList;//左边的点集合
    private List<PointBean> pointList2;//右边的点集合
    private List<Integer> y;//纵坐标集合(逐渐增多,满屏后清空)
    private int spacing=2;
    private int pointNum;//一个屏幕点的个数
    private int width;
    private TextView heart;
    private List<Integer> datasy;
    private Timer timer;
    private int count;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DRAWPATH:
                    DrawPath();
                    handler.sendEmptyMessageDelayed(DRAWPATH,10);

                case DRAWPATH2:
//                    DrawPath();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgdata);
        manager = CommandManager.getInstance(this);
        totalList=new ArrayList<>();
        tv_ecgdata = (TextView) findViewById(R.id.tv_ecgdata);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        pathView2 = (PathView2) findViewById(R.id.pathview2);
        heart = (TextView) findViewById(R.id.heart);
        getSupportActionBar().setTitle(R.string.ecg_test_data);

        manager.startMeasureEcg();
        y=new ArrayList<>();
        pointList = new ArrayList<>();
        pointList2 = new ArrayList<>();
        width = getWindowManager().getDefaultDisplay().getWidth();
        pointNum = width / spacing;

        Log.i(TAG,"width "+width+"  pointNum "+pointNum);
    }


    //接收蓝牙状态改变的广播
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                App.mConnected = true;
                App.isConnecting = false;
                //todo 更改界面ui

                Log.d("BluetoothLeService", "连上");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                App.mConnected = false;
                //todo 更改界面ui

                invalidateOptionsMenu();//更新菜单栏
                App.mBluetoothLeService.close();//断开更彻底(没有这一句，在某些机型，重连会连不上)
                Log.d("BluetoothLeService", "断开");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                final byte[] txValue = intent
                        .getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Log.i(TAG, "接收的数据：" + DataHandlerUtils.bytesToHexStr(txValue));

                List<Integer> datas = DataHandlerUtils.bytesToArrayList(txValue);
                Log.d(TAG, "datas: "+datas.toString());

                if (datas.size()>1){
                    i++;
                    totalList.add(datas);

                    String temp = datas.toString();
                    text = text + "\n" + i + ". " + temp;
                    tv_ecgdata.setText(text);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部

                    }
                });


                //ac 01 心电图数据
                if (datas.get(0)==172 && datas.get(1)==1){
                    //数据组成的纵坐标值
                    datasy = DataHandlerUtils.bytesToArrayListForEcg(txValue);
                    if (datasy.size()>1){
                        i++;
                        totalList.add(datasy);
                        String temp = datasy.toString();
                        text = text + "\n" + i + ". " + temp;
                        tv_ecgdata.setText(text);
                    }
                    y.addAll(datasy);
                    Log.i("zgy",y.toString());

//                    if (count==0){
//                        handler.sendEmptyMessage(DRAWPATH);
//                    }
//                    count++;

                    DrawPath();


                }

                //ac 02 心率数据
                if (datas.get(0)==172 && datas.get(1)==2){
                    Log.i(TAG,datas.toString());
                    if (datas.get(3)!=0 ){
                        heart.setText(String.valueOf(datas.get(3)));
                    }

                }
                //ac 06 测量失败
                if (datas.get(0)==172 && datas.get(1)==6){
                    Toast.makeText(EcgDataActivity.this,"测量失败",Toast.LENGTH_SHORT).show();

                }
                //ac 05 测量完成
                if (datas.get(0)==172 && datas.get(1)==5){
                    timer.cancel();
                    Toast.makeText(EcgDataActivity.this,"测量完成",Toast.LENGTH_SHORT).show();

                }
            }
        }
    };

    private void DrawPath() {
        pointList.clear();

        for (int j = 0; j < y.size(); j++) {
            PointBean pointBean=new PointBean();
            pointBean.setX(j * spacing);
            pointBean.setY(y.get(j));

            pointList.add(pointBean);
            if (j< datasy.size()){
                if (pointList2.size() > 0) {//第二个集合删除元素
                    pointList2.remove(0);
                }
            }

            pathView2.setData(pointList, pointList2);

            if (pointList.size() >= pointNum + 1) {
                Log.e("zgy", "越界");
                pointList2.addAll(pointList);
                y.clear();
            }

        }
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
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ecg, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.test:
                manager.getBatteryInfo();

                break;
            case R.id.generate:
                for (List<Integer> integers : totalList) {
                    for (Integer integer : integers) {
                        Log.i("qiji",integer+"");
                    }

                }

                break;
            case R.id.clear:
                text="";
                i=0;
                tv_ecgdata.setText(text);

                break;
        }
        return super.onOptionsItemSelected(item);
    }



}

