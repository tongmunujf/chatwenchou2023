package com.ucas.chat.utils;

/**
 * Create by an_huang on 2020/7/22
 * Describe:
 */
public class EventBusEvent {

    public String msg;
    public Object data;

    public EventBusEvent(String msg) {
        this.msg = msg;
    }

    public EventBusEvent(String msg, Object data){
        this.msg = msg;
        this.data = data;
    }
}
