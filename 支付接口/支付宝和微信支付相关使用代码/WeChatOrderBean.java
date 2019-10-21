package com.dayizhihui.dayishi.member.common.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/19 0019.
 */
public class WeChatOrderBean {

    /**
     * msg :
     * prepay : {"package":"Sign=WXPay","appid":"wxce151122e3aa7ae3","sign":"4B78E2646238F0EEC48DD1E229863380","partnerid":"1522121781","prepayid":"wx19143241217677e69d139d1a0892827759","noncestr":"3f54e3eb484d48679457b842c6d0c319","timestamp":"20190419143236"}
     * code : success
     * type : 2
     */

    private String msg;
    private PrepayBean prepay;
    private String code;
    private String type;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public PrepayBean getPrepay() {
        return prepay;
    }

    public void setPrepay(PrepayBean prepay) {
        this.prepay = prepay;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class PrepayBean {
        /**
         * package : Sign=WXPay
         * appid : wxce151122e3aa7ae3
         * sign : 4B78E2646238F0EEC48DD1E229863380
         * partnerid : 1522121781
         * prepayid : wx19143241217677e69d139d1a0892827759
         * noncestr : 3f54e3eb484d48679457b842c6d0c319
         * timestamp : 20190419143236
         */

        @SerializedName("package")
        private String packageX;
        private String appid;
        private String sign;
        private String partnerid;
        private String prepayid;
        private String noncestr;
        private String timestamp;

        public String getPackageX() {
            return packageX;
        }

        public void setPackageX(String packageX) {
            this.packageX = packageX;
        }

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getPartnerid() {
            return partnerid;
        }

        public void setPartnerid(String partnerid) {
            this.partnerid = partnerid;
        }

        public String getPrepayid() {
            return prepayid;
        }

        public void setPrepayid(String prepayid) {
            this.prepayid = prepayid;
        }

        public String getNoncestr() {
            return noncestr;
        }

        public void setNoncestr(String noncestr) {
            this.noncestr = noncestr;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
