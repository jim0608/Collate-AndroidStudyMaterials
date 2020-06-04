package com.dayizhihui.dayishi.member.common.pay.netex;

import com.dayizhihui.dayishi.member.common.bean.ResultInfo;
import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.OrderInfo;
import com.dayizhihui.dayishi.member.common.bean.WeChatOrderBean;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface PayService {
    //支付宝支付下单
    @POST("app/alipay/saveOrder")
    Call<ResultInfo<OrderInfo>> getAliOrderInfor(@Body RequestBody requestBody);

    //微信支付下单
    @POST("app/wechat/saveOrder")
    Call<ResultInfo<WeChatOrderBean>>getWxOrderInfor(@Body RequestBody requestBody);

    //微信支付下单
    @POST("app/order/wechatPayCallback")
    Call<ResultInfo<GetAllOrderBean>>getWxResult(@Body RequestBody requestBody);

    //积分支付下单
    @POST("app/order/saveOrder")
    Call<ResultInfo<GetAllOrderBean>>getIntegralOrder(@Body RequestBody requestBody);

    //积分支付下单成功开始支付
    @POST("app/order/exchangeProducts")
    Call<ResultInfo<Object>>getIntegralPay(@Body RequestBody requestBody);
}
