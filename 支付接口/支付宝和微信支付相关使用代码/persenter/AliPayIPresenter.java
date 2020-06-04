package com.dayizhihui.dayishi.member.common.pay.persenter;

import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface AliPayIPresenter {
    void orderPersenter(GetAllOrderBean orderBean);
    void paySucess(String order_info);
}
