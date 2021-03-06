package com.stratagile.qlink.entity.otc;

import android.os.Parcel;
import android.os.Parcelable;

import com.stratagile.qlink.entity.BaseBack;

public class EntrustOrderInfo extends BaseBack<EntrustOrderInfo.OrderBean> {

    /**
     * order : {"unitPrice":1,"minAmount":1,"qgasAddress":"qlc_1fyz7ksawbgak4tqfyhspsbo4udsao1x8prui9unp6ggw7rpifea6ia76pj7","lockingAmount":0,"type":"BUY","userId":"bafe415310bd41fdb055fb0fe6cd1080","tradeTokenChain":"QLC_CHAIN","completeAmount":0,"head":"/data/dapp/head/f597e8eccf8e4624bcd92ef04d6881cb.jpg","number":"20190821101702153221","totalAmount":100,"usdtAddress":"","orderTime":"2019-08-21 10:17:03","nickname":"hzpa","payTokenChain":"NEO_CHAIN","tradeToken":"QGAS","id":"798ef9850b15451db2a9b8dc4fdb3f5f","maxAmount":100,"otcTimes":0,"status":"NORMAL","payToken":"QLC"}
     */

    private OrderBean order;

    public OrderBean getOrder() {
        return order;
    }

    public void setOrder(OrderBean order) {
        this.order = order;
    }

    public static class OrderBean implements Parcelable {
        public String getTxid() {
            return txid;
        }

        public void setTxid(String txid) {
            this.txid = txid;
        }

        /**
         * unitPrice : 1.0
         * minAmount : 1.0
         * qgasAddress : qlc_1fyz7ksawbgak4tqfyhspsbo4udsao1x8prui9unp6ggw7rpifea6ia76pj7
         * lockingAmount : 0.0
         * type : BUY
         * userId : bafe415310bd41fdb055fb0fe6cd1080
         * tradeTokenChain : QLC_CHAIN
         * completeAmount : 0.0
         * head : /data/dapp/head/f597e8eccf8e4624bcd92ef04d6881cb.jpg
         * number : 20190821101702153221
         * totalAmount : 100.0
         * usdtAddress :
         * orderTime : 2019-08-21 10:17:03
         * nickname : hzpa
         * payTokenChain : NEO_CHAIN
         * tradeToken : QGAS
         * id : 798ef9850b15451db2a9b8dc4fdb3f5f
         * maxAmount : 100.0
         * otcTimes : 0
         * status : NORMAL
         * payToken : QLC
         */

        private double unitPrice;
        private double minAmount;
        private String qgasAddress;
        private double lockingAmount;
        private String type;
        private String userId;
        private String tradeTokenChain;
        private double completeAmount;
        private String head;
        private String number;
        private double totalAmount;
        private String usdtAddress;
        private String orderTime;
        private String nickname;
        private String payTokenChain;
        private String tradeToken;
        private String id;
        private double maxAmount;
        private int otcTimes;
        private String status;
        private String payToken;
        private String txid;


        protected OrderBean(Parcel in) {
            unitPrice = in.readDouble();
            minAmount = in.readDouble();
            qgasAddress = in.readString();
            lockingAmount = in.readDouble();
            type = in.readString();
            userId = in.readString();
            tradeTokenChain = in.readString();
            completeAmount = in.readDouble();
            head = in.readString();
            number = in.readString();
            totalAmount = in.readDouble();
            usdtAddress = in.readString();
            orderTime = in.readString();
            nickname = in.readString();
            payTokenChain = in.readString();
            tradeToken = in.readString();
            id = in.readString();
            maxAmount = in.readDouble();
            otcTimes = in.readInt();
            status = in.readString();
            payToken = in.readString();
            txid = in.readString();
        }

        public static final Creator<OrderBean> CREATOR = new Creator<OrderBean>() {
            @Override
            public OrderBean createFromParcel(Parcel in) {
                return new OrderBean(in);
            }

            @Override
            public OrderBean[] newArray(int size) {
                return new OrderBean[size];
            }
        };

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public double getMinAmount() {
            return minAmount;
        }

        public void setMinAmount(double minAmount) {
            this.minAmount = minAmount;
        }

        public String getQgasAddress() {
            return qgasAddress;
        }

        public void setQgasAddress(String qgasAddress) {
            this.qgasAddress = qgasAddress;
        }

        public double getLockingAmount() {
            return lockingAmount;
        }

        public void setLockingAmount(double lockingAmount) {
            this.lockingAmount = lockingAmount;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getTradeTokenChain() {
            return tradeTokenChain;
        }

        public void setTradeTokenChain(String tradeTokenChain) {
            this.tradeTokenChain = tradeTokenChain;
        }

        public double getCompleteAmount() {
            return completeAmount;
        }

        public void setCompleteAmount(double completeAmount) {
            this.completeAmount = completeAmount;
        }

        public String getHead() {
            return head;
        }

        public void setHead(String head) {
            this.head = head;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getUsdtAddress() {
            return usdtAddress;
        }

        public void setUsdtAddress(String usdtAddress) {
            this.usdtAddress = usdtAddress;
        }

        public String getOrderTime() {
            return orderTime;
        }

        public void setOrderTime(String orderTime) {
            this.orderTime = orderTime;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPayTokenChain() {
            return payTokenChain;
        }

        public void setPayTokenChain(String payTokenChain) {
            this.payTokenChain = payTokenChain;
        }

        public String getTradeToken() {
            return tradeToken;
        }

        public void setTradeToken(String tradeToken) {
            this.tradeToken = tradeToken;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getMaxAmount() {
            return maxAmount;
        }

        public void setMaxAmount(double maxAmount) {
            this.maxAmount = maxAmount;
        }

        public int getOtcTimes() {
            return otcTimes;
        }

        public void setOtcTimes(int otcTimes) {
            this.otcTimes = otcTimes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPayToken() {
            return payToken;
        }

        public void setPayToken(String payToken) {
            this.payToken = payToken;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(unitPrice);
            dest.writeDouble(minAmount);
            dest.writeString(qgasAddress);
            dest.writeDouble(lockingAmount);
            dest.writeString(type);
            dest.writeString(userId);
            dest.writeString(tradeTokenChain);
            dest.writeDouble(completeAmount);
            dest.writeString(head);
            dest.writeString(number);
            dest.writeDouble(totalAmount);
            dest.writeString(usdtAddress);
            dest.writeString(orderTime);
            dest.writeString(nickname);
            dest.writeString(payTokenChain);
            dest.writeString(tradeToken);
            dest.writeString(id);
            dest.writeDouble(maxAmount);
            dest.writeInt(otcTimes);
            dest.writeString(status);
            dest.writeString(payToken);
            dest.writeString(txid);
        }
    }
}
