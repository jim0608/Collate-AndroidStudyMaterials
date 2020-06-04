package com.dayizhihui.dayishi.member.common.pay.model;

import android.content.Context;

import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.persenter.IntegralPayPresenter;
import com.dayizhihui.dyzhlib.mvp.IModel;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface IntegralPModel extends IModel {
//获取订单号
    void orderModel(PayServiceRequest payResult, Context context, IntegralPayPresenter integralPayPresenter);
//支付
    void payModel(GetAllOrderBean orderBean, IntegralPayPresenter integralPayPresenter);
}
