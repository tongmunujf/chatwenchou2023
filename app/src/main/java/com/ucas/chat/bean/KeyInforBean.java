package com.ucas.chat.bean;

import java.io.Serializable;

public class KeyInforBean implements Serializable {

    private String keyName ;
    private String keyValue;

    public KeyInforBean() {
    }

    public KeyInforBean(String keyName, String keyValue) {
        this.keyName = keyName;
        this.keyValue = keyValue;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    @Override
    public String toString() {
        return "KeyInforBean{" +
                "keyName='" + keyName + '\'' +
                ", keyValue='" + keyValue + '\'' +
                '}';
    }
}
