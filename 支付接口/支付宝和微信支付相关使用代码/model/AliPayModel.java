package com.dayizhihui.dayishi.member.common.pay.model;

import android.content.Context;

import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.bean.ResultInfo;
import com.dayizhihui.dayishi.member.common.bean.OrderInfo;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.netex.PayCheckLoader;
import com.dayizhihui.dayishi.member.common.pay.persenter.AliPayPresenter;
import com.dayizhihui.dyzhlib.util.ToastUtils;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public class AliPayModel implements AliPModel {

    @Override
    public void payModel(PayServiceRequest payResult, final AliPayPresenter aliPayPresenter) {
        ALog.dTag("PAY_ALIPAY", payResult);
        new PayCheckLoader().AliPayLoder(payResult, new Callback<ResultInfo<OrderInfo>>() {
            @Override
            public void onResponse(Call<ResultInfo<OrderInfo>> call, Response<ResultInfo<OrderInfo>> response) {
                try {
                    ALog.dTag("PAY_ALIPAY", new Gson().toJson(response.body()));
                    if (response.body().isSuccess()) {
                        aliPayPresenter.paySucess(response.body().getObject().getOrderInfo());
                    }else {
                       ToastUtils.showShort(response.body().getMessage());
                        aliPayPresenter.getView().payError();
                    }
                } catch (Exception e) {
                    aliPayPresenter.getView().payError();
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResultInfo<OrderInfo>> call, Throwable t) {
                ALog.a(t);
                aliPayPresenter.getView().payError();
            }
        });
    }


}
