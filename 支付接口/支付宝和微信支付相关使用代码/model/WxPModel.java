package com.dayizhihui.dayishi.member.common.pay.model;

import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.persenter.WxPayPresenter;
import com.dayizhihui.dyzhlib.mvp.IModel;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface WxPModel extends IModel {
    void payModel(PayServiceRequest payResult, WxPayPresenter wxPayPresenter);
}
