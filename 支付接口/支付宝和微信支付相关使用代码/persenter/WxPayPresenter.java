package com.dayizhihui.dayishi.member.common.pay.persenter;


import android.content.Context;

import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.Global;
import com.dayizhihui.dayishi.member.common.pay.PayView;
import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.bean.WeChatOrderBean;
import com.dayizhihui.dayishi.member.common.pay.model.WxPModel;
import com.dayizhihui.dayishi.member.common.pay.model.WxPayModel;
import com.dayizhihui.dyzhlib.mvp.presenter.BasePresenter;
import com.dayizhihui.dyzhlib.utils.PreferenceUtil;
import com.google.gson.Gson;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public class WxPayPresenter extends BasePresenter<WxPModel, PayView> implements WxPayIPresenter {
    private String cjId = PreferenceUtil.getPrefString(Global.T_CJ_ID, "");
    private String userId = PreferenceUtil.getPrefString(Global.T_ID, "");

    private IWXAPI iwxapi; //微信支付api

    /**
     * 调起微信支付的方法
     * @param prepay
     */
    private void toWXPay(final WeChatOrderBean.PrepayBean prepay) {
        iwxapi = WXAPIFactory.createWXAPI((Context) getView(), null); //初始化微信api
        iwxapi.registerApp(prepay.getAppid()); //注册appid
        //一定注意要放在子线程
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayReq request = new PayReq(); //调起微信的对象
                //这几个参数的值，正是上面我们说的统一下单接口后返回来的字段，我们对应填上去即可
                request.appId = prepay.getAppid();
                request.partnerId = prepay.getPartnerid();
                request.prepayId = prepay.getPrepayid();
                request.packageValue = "Sign=WXPay";
                request.nonceStr = prepay.getNoncestr();
                request.timeStamp = prepay.getTimestamp();
                request.sign = prepay.getSign();
                ALog.dTag("PAY_WX",new Gson().toJson(request));
                iwxapi.sendReq(request);//发送调起微信的请求
                getView().payError();//取消Activity的ProgressDialog加载页面
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    @Override
    public void orderPersenter(GetAllOrderBean orderBean) {
        PayServiceRequest result = new PayServiceRequest();
        result.setId(orderBean.getId());
        result.setOrder_id(orderBean.getOrder_id());
        getModel().payModel(result, this);

    }

    @Override
    public void getWxOrderBean(WeChatOrderBean.PrepayBean prepay) {
        toWXPay(prepay);
    }

    @Override
    public WxPModel createModel() {

        return new WxPayModel();
    }


}
