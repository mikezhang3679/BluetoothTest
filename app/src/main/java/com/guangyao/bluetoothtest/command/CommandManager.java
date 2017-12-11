package com.guangyao.bluetoothtest.command;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.guangyao.bluetoothtest.bean.DateModel;
import com.guangyao.bluetoothtest.constans.BleConstans;
import com.guangyao.bluetoothtest.constans.Constans;
import com.guangyao.bluetoothtest.utils.DataHandlerUtils;

import java.util.Calendar;


public class CommandManager {
    
    private static final String TAG = "CommandManager";
    private static Context mContext;
    private static CommandManager instance;

    private CommandManager() {
    }

    public static synchronized CommandManager getInstance(Context context) {
        if (mContext == null) {
            mContext = context;
        }
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    /**
     * 跌倒提醒
     * @param control  0(关闭)  1(打开)
     */
    public void setFalldownAlert(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x11;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 清除数据
     */
    public void setClearData(){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x23;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) 0x00;
        broadcastData(bytes);
    }

    /**
     * 恢复手环出厂设置
     */
    public void setResetBand(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xFF;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }


    /**
     * 单次、实时测量
     * @param status  心率：0X09(单次) 0X0A(实时)
     *                血氧：0X11(单次) 0X12(实时)
     *                血压：0X21(单次) 0X22(实时)
     *
     * @param control 0关  1开
     */
    public void setOnceOrRealTimeMeasure(int status, int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x31;
        bytes[5] = (byte) status;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 一键测量
     * @param control  0(关)  1(开)
     */
    public void setOnceKeyMeasure(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x32;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte)control;
        broadcastData(bytes);
    }


    /**
     * 下拉同步数据
     * @param timeInMillis 传入一个时间点，手环传这个时间点之后的整点数据过来
     * @param timeInMillis2 传入一个时间点，手环传这个时间点之后的运动模式数据过来

    public void setSyncData(long timeInMillis,long timeInMillis2) {//一个用于获取整点数据，一个用于获取运动模式数据
        DateModel dateModel = new DateModel(timeInMillis,timeInMillis2);
        byte[] data = new byte[18];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 15;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x51;
        data[5] = (byte) 0x80;
//        data[6] = (byte)0;//占位符，没意义
        data[7] = (byte) ((dateModel.year - 2000));
        data[8] = (byte) (dateModel.month);
        data[9] = (byte) (dateModel.day);
        data[10] = (byte) (dateModel.hour);
        data[11] = (byte) (dateModel.minute);

        data[12] = (byte) ((dateModel.year2 - 2000)); //获取运动模式数据 需要的
        data[13] = (byte) (dateModel.month2);
        data[14] = (byte) (dateModel.day2);
        data[15] = (byte) (dateModel.hour2);
        data[16] = (byte) (dateModel.minute2);
        data[17] = (byte) (dateModel.second2);

        broadcastData(data);
    }*/


    /**
     * 2.6  下拉同步数据   00 09 ff 51 80 00 10 0a 14 11 00
     */
    public void setSyncData(long timeInMillis) {
        DateModel dateModel = new DateModel(timeInMillis);
        byte[] data = new byte[12];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 9;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x51;
        data[5] = (byte) 0x80;
//        data[6] = (byte)0;//占位符，没意义
        data[7] = (byte) ((dateModel.year - 2000));
        data[8] = (byte) (dateModel.month);
        data[9] = (byte) (dateModel.day);
        data[10] = (byte) (dateModel.hour);
        data[11] = (byte) (dateModel.minute);
        broadcastData(data);
    }
    /**
     * 下拉同步睡眠数据
     */
    public void setSyncSleepData(long timeInMillis) {
        DateModel dateModel = new DateModel(timeInMillis);
        byte[] data = new byte[10];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 7;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x52;
        data[5] = (byte) 0x80;
//        data[6] = (byte)0;//占位符，没意义
        data[7] = (byte) ((dateModel.year - 2000));
        data[8] = (byte) (dateModel.month);
        data[9] = (byte) (dateModel.day - 1);
        broadcastData(data);
    }

    /**
     * 查找手环
     */
    public void FindBracelet() {
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x71;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }
    /**
     * 马达测试
     */
    public void motorText(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB1;
        bytes[5] = (byte)0x80;
        bytes[6] = (byte)control;
        broadcastData(bytes);
    }
    /**
     * 智能提醒
     * @param MessageId
     * @param type  0开  1关  2来消息通知
     */
    public void setSmartAlert(int MessageId, int type) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 5;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x72;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) MessageId;
        bytes[7] = (byte) type;
        broadcastData(bytes);
    }

    /**
     * 抬手亮屏
     * @param control  0关  1开
     */
    public void setUpHandLightScreen(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x77;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 整点测量
     * @param control  0关  1开
     */
    public void setOnTimeMeasure(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x78;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte)control;
        broadcastData(bytes);
    }

    /**
     * 摇摇拍照指令
     * @param control 0关  1开
     */
    public void setSharkTakePhoto(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x79;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }


    /**
     * 防丢
     * @param control  0关  1开
     */
    public void setAntiLostAlert(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7A;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 中英文切换
     * @param control  0中文  1英文
     */
    public void setSwitchChineseOrEnglish(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7B;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 时间制切换
     * @param control  0（24小时制）  1(12小时制)
     */
    public void set12HourSystem(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7C;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 同步天气信息
     * @param weather  0多云 1晴天 2雪天 3雨天
     * @param temp  0(0度以上)  1(0度以下)
     */
    public void setSyncWeather(int weather, int temp) {
        byte[] bytes = new byte[9];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 6;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7E;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) weather;
        bytes[7] = (byte) temp;
        bytes[8] = (byte) (temp >= 0 ? 0 : 1);
        broadcastData(bytes);
    }
    /**
     * 同步时间  ab 00 0b ff 93 80 00 07 e0 0a 0e 09 33 12
     */
    public void setTimeSync() {
        //当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        byte[] data = new byte[14];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte) 11;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x93;
        data[5] = (byte) 0x80;
//        data[6] = (byte)0;//占位符
        data[7] = (byte) ((year & 0xff00) >> 8);
        data[8] = (byte) (year & 0xff);
        data[9] = (byte) (month & 0xff);
        data[10] = (byte) (day & 0xff);
        data[11] = (byte) (hour & 0xff);
        data[12] = (byte) (minute & 0xff);
        data[13] = (byte) (second & 0xff);
        broadcastData(data);
    }

    /**
     * 中英文切换
     * @param type  0中文   1英文
     */
    public void setChineseOrEnglish(int type){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7B;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) type;
        broadcastData(bytes);
    }

    /**
     * 挂断电话
     */
    public void setHangUpPhone(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x81;
        bytes[5] = (byte) 0;
        broadcastData(bytes);
    }

    /**
     * 智能提醒
     * @param MessageId
     * @param type
     */
    public void setSmartWarnNoContent(int MessageId, int type) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 5;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x72;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) MessageId;//来电提醒、短信提醒等
        bytes[7] = (byte) type;//0开 1关 2来消息通知
        broadcastData(bytes);
    }

    /**
     * 智能提醒,带消息内容
     * @param MessageId
     * @param type
     */
    public void setSmartWarn(int MessageId, int type, String content) {
        byte[] bytes1 = null;
        int length = 0;
        if (!TextUtils.isEmpty(content)){
            bytes1 = content.getBytes();
            length = bytes1.length;
        }
        byte[] bytes2 = new byte[8];
        bytes2[0] = (byte) 0xAB;
        bytes2[1] = (byte) 0;
        bytes2[2] = (byte) (length+5);
        bytes2[3] = (byte) 0xFF;
        bytes2[4] = (byte) 0x72;
        bytes2[5] = (byte) 0x80;
        bytes2[6] = (byte) MessageId;//来电提醒、短信提醒等
        bytes2[7] = (byte) type;//0开 1关 2来消息通知
        byte[] bytes = DataHandlerUtils.addBytes(bytes2, bytes1);
        broadcastData(bytes);
    }

    /**
     * 查看电量
     */
    public void getBatteryInfo(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x91;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }

    /**
     * 查看版本信息
     */
    public void getVersionInfo(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x92;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }






    /**
     * 校准时间
     */
    public void setCalibrationTime(int hour, int minute) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 5;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7D;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) hour;//小时
        bytes[7] = (byte) minute;//分
        broadcastData(bytes);
    }

    /**
     * 屏显 ab 00 04 ff b0 80 01
     */
    public void screenShow(int control){
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB0;
        bytes[5] = (byte)0x80;
        bytes[6] = (byte)control;
        broadcastData(bytes);
    }
    /**
     * 智能提醒,带消息内容
     * @param MessageId
     * @param type
     */
    public void smartWarnInfo(int MessageId, int type,String data) {
        byte[] bytes1 = data.getBytes();
        int length = bytes1.length;
        Log.e(TAG,"length:"+length);
        byte[] bytes = new byte[8];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) (length+5);
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x72;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) MessageId;//来电提醒、短信提醒等
        bytes[7] = (byte) type;//0开 1关 2来消息通知
        byte[] bytes2 = DataHandlerUtils.addBytes(bytes, bytes1);
        broadcastData(bytes2);
    }
    /**
     *RSSI
     */
    public void rssiTest(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB5;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }


    /**
     * 按键测试
     */
    public void buttonClick(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB6;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }
    /**
     * 三轴传感器
     */
    public void sensorTest(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB3;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }

    /**
     * 心率传感器
     */
    public void heartRateSensorTest(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB4;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }

    /**
     * 单次、实时测量
     *
     * @param status
     * @param control
     */
    public void realTimeAndOnceMeasure(int status, int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x31;
        //心率：0X09(单次) 0X0A(实时)  血氧：0X11(单次) 0X12(实时) 血压：0X21 0X22
        bytes[5] = (byte) status;
        bytes[6] = (byte) control;//0关  1开
        broadcastData(bytes);
    }


    /**
     * 关机
     */
    public void Shutdown(){
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xFF;
        bytes[5] = (byte)0x80;
        broadcastData(bytes);
    }


    /**
     * 中英文切换
     */
    public void chineseEnglishSwitch(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x7B;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     *   2.5 一键测量
     */
    public void oneKeyMeasure(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x32;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * 查找手环
     */
    public void findBand() {
        byte[] bytes = new byte[6];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 3;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x71;
        bytes[5] = (byte) 0x80;
        broadcastData(bytes);
    }

    /**
     * 跌倒提醒
     *
     * @param control
     */
    public void falldownWarn(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x11;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;//0关闭 1开
        broadcastData(bytes);
    }

    /**
     * 设置指针
     * @param control
     */
    public void setPointer(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0xB7;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;//0顺时针 1逆时针 2停止
        broadcastData(bytes);
    }




    /**
     * 设置祈祷
     */
    public void setPray(int id, int status, int startH, int startM,int repeat) {
        byte[] data = new byte[11];
        data[0] = (byte) 0xAB;
        data[1] = (byte) 0;
        data[2] = (byte)8;
        data[3] = (byte) 0xff;
        data[4] = (byte) 0x96;
        data[5] = (byte)0x80;
        data[6] = (byte) id;
        data[7] = (byte) status;
        data[8] = (byte) startH;
        data[9] = (byte) startM;
        data[10] = (byte) repeat ;
        broadcastData(data);
    }


    /**
     * 心电开始测量
     */
    public void startMeasureEcg(){
        byte[] bytes = new byte[2];
        bytes[0] = (byte) 0xAC;
        bytes[1] = (byte) 0x01;
        broadcastData(bytes);
    }

    /**
     * 3.8 整点测量
     */
    public void hourlyMeasure(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x78;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }


    public void bootLoader(int control) {
        byte[] bytes = new byte[7];
        bytes[0] = (byte) 0xAB;
        bytes[1] = (byte) 0;
        bytes[2] = (byte) 4;
        bytes[3] = (byte) 0xFF;
        bytes[4] = (byte) 0x25;
        bytes[5] = (byte) 0x80;
        bytes[6] = (byte) control;
        broadcastData(bytes);
    }

    /**
     * @brief Broadcast intent with pointed bytes.
     * @param[in] bytes Array of byte to send on BLE.
     */
    private void broadcastData(byte[] bytes) {
        final Intent intent = new Intent(BleConstans.ACTION_SEND_DATA_TO_BLE);
        intent.putExtra(Constans.EXTRA_SEND_DATA_TO_BLE, bytes);
        try {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

}
