package com.dayizhihui.dayishi.member.common.pay.model;

import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.persenter.AliPayPresenter;
import com.dayizhihui.dyzhlib.mvp.IModel;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface AliPModel extends IModel {
    void payModel(PayServiceRequest payResult, AliPayPresenter aliPayPresenter);
}
