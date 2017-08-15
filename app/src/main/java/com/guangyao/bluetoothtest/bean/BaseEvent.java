package com.guangyao.bluetoothtest.bean;

/**
 * Created by lq on 2017/2/10.
 */
public class BaseEvent {
    private EventType mEventType;
    private Object mObject;

    public BaseEvent() {
    }

    public BaseEvent(EventType mEventType) {
        this.mEventType = mEventType;
    }

    public void setEventType(EventType mEventType) {
        this.mEventType = mEventType;
    }

    public EventType getEventType() {
        return mEventType;
    }

    public Object getmObject() {
        return mObject;
    }

    public void setmObject(Object mObject) {
        this.mObject = mObject;
    }

    public enum EventType {
        ONSERVICECONNECTED

    }

}
