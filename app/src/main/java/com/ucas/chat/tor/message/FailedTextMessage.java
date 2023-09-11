package com.ucas.chat.tor.message;

public class FailedTextMessage {
    private String onionName;
    private String rawMessage;

    public String getOnionName() {
        return onionName;
    }

    public void setOnionName(String onionName) {
        this.onionName = onionName;
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public FailedTextMessage(String onionName, String rawMessage) {
        this.onionName = onionName;
        this.rawMessage = rawMessage;
    }

    @Override
    public String toString() {
        return "FailedTextMessage{" +
                "onionName='" + onionName + '\'' +
                ", rawMessage='" + rawMessage + '\'' +
                '}';
    }
}
