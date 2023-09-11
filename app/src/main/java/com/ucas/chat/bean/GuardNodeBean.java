package com.ucas.chat.bean;


import java.io.Serializable;

public class GuardNodeBean implements Serializable {
    private String GuardNodeOne;
    private String GuardNodeTwo;
    private String ValidTime;

    public GuardNodeBean(String GuardNodeOne, String GuardNodeTwo,String ValidTime){
        this.GuardNodeOne = GuardNodeOne;
        this.GuardNodeTwo = GuardNodeTwo;
        this.ValidTime = ValidTime;
    }
    public  String getGuardNodeOne(){
        return GuardNodeOne;
    }
    public  String getGuardNodeTwo(){
        return GuardNodeTwo;
    }
    public  String getValidTime(){
        return ValidTime;
    }
    public void setGuardNodeOne(String guardNodeOne){
        this.GuardNodeOne = guardNodeOne;
    }
    public void setGuardNodeTwo(String guardNodeTwo){
        this.GuardNodeTwo = guardNodeTwo;
    }
    public void setValidTime(String validTime){
        this.ValidTime = validTime;
    }

    @Override
    public String toString() {
        return "GuardNodeBean{" +
                "GuardNodeOne='" + GuardNodeOne + '\'' +
                ", GuardNodeTwo='" + GuardNodeTwo + '\'' +
                ", ValidTime='" + ValidTime + '\'' +
                '}';
    }
}
