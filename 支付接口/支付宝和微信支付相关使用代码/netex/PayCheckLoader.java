package com.dayizhihui.dayishi.member.common.pay.netex;

import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.network.BaseLoader;
import com.dayizhihui.dayishi.member.common.bean.ResultInfo;
import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.OrderInfo;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.bean.WeChatOrderBean;
import com.google.gson.Gson;


import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Callback;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public class PayCheckLoader extends BaseLoader {
    private PayService payService;

    public PayCheckLoader() {
        payService = createService(PayService.class);
    }

    //Ali支付
    public void AliPayLoder(PayServiceRequest payResult, Callback<ResultInfo<OrderInfo>> callback) {
        setBaseRequest(payResult);
        ALog.dTag("PAY_ALI",payResult.toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(payResult));

        retrofit2.Call<ResultInfo<OrderInfo>> call = payService.getAliOrderInfor(body);
        call.enqueue(callback);
    }

    //微信支付
    public void WxPayLoder(PayServiceRequest payResult, Callback<ResultInfo<WeChatOrderBean>> callback) {
        setBaseRequest(payResult);
        ALog.dTag("PAY_WX",payResult.toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(payResult));

        retrofit2.Call<ResultInfo<WeChatOrderBean>> call = payService.getWxOrderInfor(body);
        call.enqueue(callback);
    }

    //生成订单
    public void IntegralPayLoder(PayServiceRequest payResult, Callback<ResultInfo<GetAllOrderBean>> callback) {
        setBaseRequest(payResult);
        ALog.dTag("PAY_INTEGER",payResult.toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(payResult));

        retrofit2.Call<ResultInfo<GetAllOrderBean>> call = payService.getIntegralOrder(body);
        call.enqueue(callback);
    }

    //积分兑换获取订单
    public void IntegralPay(PayServiceRequest result, Callback<ResultInfo<Object>> callback) {
        setBaseRequest(result);
        ALog.dTag("PAY_INTEGER",result.toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(result));

        retrofit2.Call<ResultInfo<Object>> call = payService.getIntegralPay(body);
        call.enqueue(callback);
    }
}
