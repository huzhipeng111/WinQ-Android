package com.stratagile.qlink.entity.defi;

public class CheckHubState {

    /**
     * state : 1
     * stateStr : DepositDone
     * amount : 500000000
     * ethTxHash : 0xa95f6f8768f31323491b87257884de0f9c5c9dc11e966472ce4f0e5eae95d557
     * neoTxHash : 0x0de3d8f60ea75a184ace5944957927adc981a8ac42e5046a60a74320e138e09e
     * ethUserAddr : 0x255eEcd17E11C5d2FFD5818da31d04B5c1721D7C
     * neoUserAddr : AXa39WUxN6rXjRMt36Zs88XZi5hZHcF8GK
     * startTime : 2020-12-01T09:00:44Z
     * lastModifyTime : 2020-12-01T09:01:33Z
     */

    private int state;
    private String stateStr;
    private String amount;
    private String ethTxHash;
    private String neoTxHash;
    private String ethUserAddr;
    private String neoUserAddr;
    private String startTime;
    private String lastModifyTime;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateStr() {
        return stateStr;
    }

    public void setStateStr(String stateStr) {
        this.stateStr = stateStr;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getEthTxHash() {
        return ethTxHash;
    }

    public void setEthTxHash(String ethTxHash) {
        this.ethTxHash = ethTxHash;
    }

    public String getNeoTxHash() {
        return neoTxHash;
    }

    public void setNeoTxHash(String neoTxHash) {
        this.neoTxHash = neoTxHash;
    }

    public String getEthUserAddr() {
        return ethUserAddr;
    }

    public void setEthUserAddr(String ethUserAddr) {
        this.ethUserAddr = ethUserAddr;
    }

    public String getNeoUserAddr() {
        return neoUserAddr;
    }

    public void setNeoUserAddr(String neoUserAddr) {
        this.neoUserAddr = neoUserAddr;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
}
