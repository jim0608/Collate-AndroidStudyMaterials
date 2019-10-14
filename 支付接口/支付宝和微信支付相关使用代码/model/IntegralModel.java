package com.dayizhihui.dayishi.member.common.pay.model;

import android.app.Activity;
import android.content.Context;

import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.bean.ResultInfo;
import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.netex.PayCheckLoader;
import com.dayizhihui.dayishi.member.common.pay.persenter.IntegralPayPresenter;
import com.dayizhihui.dyzhlib.util.ToastUtils;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Descrip：积分model
 * Author： Zhangjinming
 * CreateTime on 2019/4/12 0012.
 */
public class IntegralModel implements IntegralPModel {

    @Override
    public void orderModel(PayServiceRequest payResult, final Context context, final IntegralPayPresenter integralPayPresenter) {
        ALog.dTag("PAY_INTEGER", payResult);
        new PayCheckLoader().IntegralPayLoder(payResult, new Callback<ResultInfo<GetAllOrderBean>>() {
            @Override
            public void onResponse(Call<ResultInfo<GetAllOrderBean>> call, Response<ResultInfo<GetAllOrderBean>> response) {
                try {
                    if (response.body().isSuccess()) {
                        integralPayPresenter.getOrderBean(response.body().getObject());
                        ALog.dTag("PAY_INTEGER", response.body().getObject().toString());
                    } else {
                        ToastUtils.showShort( response.body().getMessage());

                        ALog.dTag("PAY_INTEGER", response.body());
                        integralPayPresenter.getView().payError();
                        integralPayPresenter.getView().finishPay();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    integralPayPresenter.getView().payError();
                    integralPayPresenter.getView().finishPay();
                    ALog.dTag("IntegralModel_Exception", e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResultInfo<GetAllOrderBean>> call, Throwable t) {
                t.printStackTrace();
                integralPayPresenter.getView().payError();
                integralPayPresenter.getView().finishPay();
            }
        });
    }

    @Override
    public void payModel(final GetAllOrderBean orderBean, final IntegralPayPresenter integralPayPresenter) {
        PayServiceRequest request = new PayServiceRequest();
        request.setId(orderBean.getId());
        request.setOrder_id(orderBean.getOrder_id());
        request.setCj_id(orderBean.getCj_id());
        request.setCus_id(orderBean.getCus_id());
        ALog.dTag("PAY_INTEGER", request.toString());
        new PayCheckLoader().IntegralPay(request, new Callback<ResultInfo<Object>>() {
            @Override
            public void onResponse(Call<ResultInfo<Object>> call, Response<ResultInfo<Object>> response) {
                try {
                    ALog.json("PAY_INTEGER", new Gson().toJson(response));
                    if (response.body().isSuccess()) {
                        integralPayPresenter.paySucess(orderBean);
//                        ToastUtils.showShort(response.body().getMessage());
                    } else {
                        integralPayPresenter.payError();
                        ToastUtils.showShort(response.body().getMessage());
                    }
                } catch (Exception e) {
                    integralPayPresenter.payError();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResultInfo<Object>> call, Throwable t) {
                integralPayPresenter.payError();
                t.printStackTrace();
            }
        });
    }


}
