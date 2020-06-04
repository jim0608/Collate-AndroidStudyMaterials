package com.dayizhihui.dayishi.member.common.pay.persenter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.Global;
import com.dayizhihui.dayishi.member.common.bean.AuthResult;
import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.PayResult;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.PayView;
import com.dayizhihui.dayishi.member.common.pay.model.AliPModel;
import com.dayizhihui.dayishi.member.common.pay.model.AliPayModel;
import com.dayizhihui.dayishi.member.common.pay.model.IntegralModel;
import com.dayizhihui.dayishi.member.common.pay.model.WxPayModel;
import com.dayizhihui.dyzhlib.mvp.presenter.BasePresenter;
import com.dayizhihui.dyzhlib.utils.PreferenceUtil;
import com.dayizhihui.dyzhlib.util.ToastUtils;

import java.util.Map;

/**
 * Descrip：支付宝支付present
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public class AliPayPresenter extends BasePresenter<AliPModel, PayView> implements AliPayIPresenter {
    private String cjId = PreferenceUtil.getPrefString(Global.T_CJ_ID, "");
    private String userId = PreferenceUtil.getPrefString(Global.T_ID, "");

    private WxPayModel wxModel;
    private IntegralModel integralModel;

    @Override
    public void orderPersenter(GetAllOrderBean orderBean) {
        PayServiceRequest result = new PayServiceRequest();
        result.setId(orderBean.getId());
        result.setOrder_id(orderBean.getOrder_id());
        getModel().payModel(result, this);
    }

    @Override
    public void paySucess(String order_info) {
        payV2(order_info);
    }

    @Override
    public AliPModel createModel() {

        return  new AliPayModel();
    }

    public void payV2(final String orderInfo) {
        /*
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo 的获取必须来自服务端；
         */
        ALog.dTag("PAY_ALI",orderInfo);
        final Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                getView().payError();
                PayTask alipay = new PayTask((Activity) getView());
                String version = alipay.getVersion();
                Map<String, String> result = alipay.payV2(orderInfo, true);
                ALog.dTag("PAY_ALI", result.toString()+"version:"+version);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
    /**
     * 支付宝支付业务示例
     */

    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_AUTH_FLAG = 2;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            Bundle bundle = new Bundle();
            bundle.putInt("flag",3);
            bundle.putInt("itemType",2);

            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    bundle.putString("resultStatus",resultStatus);
                    ALog.dTag("PAY_ALI", payResult);
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        ToastUtils.showShort("支付成功！");

                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        ToastUtils.showShort("失败:" + payResult);
                    }

                    break;
                }
                case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        ToastUtils.showShort( "成功:" + authResult);
                    } else {
                        // 其他状态值则为授权失败
                        ToastUtils.showShort( "失败:" + authResult);
                    }
                    break;
                }
                default:
                    break;
            }
            getView().gotoOrderActivity(bundle);
        };
    };

}
