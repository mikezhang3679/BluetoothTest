package com.guangyao.bluetoothtest.bean;

import java.util.Calendar;

public class DateModel {

    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;

    public int year2;
    public int month2;
    public int day2;
    public int hour2;
    public int minute2;
    public int second2;



    public DateModel(long timeInMillis,long timeInMillis2) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        //获取整点数据需要传入的时间
        calendar.setTimeInMillis(timeInMillis);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);

        //获取运动模式数据需要传入的时间
        calendar2.setTimeInMillis(timeInMillis2);
        year2 = calendar2.get(Calendar.YEAR);
        month2 = calendar2.get(Calendar.MONTH) + 1;
        day2 = calendar2.get(Calendar.DAY_OF_MONTH);
        hour2 = calendar2.get(Calendar.HOUR_OF_DAY);
        minute2 = calendar2.get(Calendar.MINUTE);
        second2 = calendar2.get(Calendar.SECOND);


    }
}
