package com.sum10.drone_user;

public class CallInfo {
    private String address;
    private String locker;
    private boolean ack;

    public CallInfo() {}

    public CallInfo(String address, String locker, boolean ack) {
        this.address = address;
        this.locker = locker;
        this.ack = ack;
    }

    public void setAddress(String address) { this.address = address; }

    public void setAck(boolean ack) { this.ack = ack; }

    public void setLocker(String locker) { this.locker = locker; }

    public String getAddress() { return address; }

    public boolean getAck() { return ack; }

    public String getLocker() { return locker; }
}
