package com.guangyao.bluetoothtest.bean;

import java.util.Calendar;

public class DateModel {

    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;



    public DateModel(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        //获取整点数据需要传入的时间
        calendar.setTimeInMillis(timeInMillis);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);



    }
}
