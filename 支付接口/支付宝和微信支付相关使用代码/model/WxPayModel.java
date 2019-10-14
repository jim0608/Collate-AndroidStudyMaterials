package com.dayizhihui.dayishi.member.common.pay.model;

import android.content.Context;

import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.bean.ResultInfo;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.bean.WeChatOrderBean;
import com.dayizhihui.dayishi.member.common.pay.netex.PayCheckLoader;
import com.dayizhihui.dayishi.member.common.pay.persenter.WxPayPresenter;
import com.dayizhihui.dyzhlib.util.ToastUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/12 0012.
 */
public class WxPayModel implements WxPModel {

    @Override
    public void payModel(PayServiceRequest payResult, final WxPayPresenter wxPayPresenter) {

        new PayCheckLoader().WxPayLoder(payResult, new Callback<ResultInfo<WeChatOrderBean>>() {
            @Override
            public void onResponse(Call<ResultInfo<WeChatOrderBean>> call, Response<ResultInfo<WeChatOrderBean>> response) {
                try {
                    ALog.dTag("PAY_WX", response.body().toString());
                    if (response.body().isSuccess()) {
                        wxPayPresenter.getWxOrderBean(response.body().getObject().getPrepay());
                    } else {
                        wxPayPresenter.getView().payError();
                        ToastUtils.showShort( response.body().getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    wxPayPresenter.getView().payError();
                    ToastUtils.showShort( response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResultInfo<WeChatOrderBean>> call, Throwable t) {
                t.printStackTrace();
                wxPayPresenter.getView().payError();
            }
        });
    }
}
