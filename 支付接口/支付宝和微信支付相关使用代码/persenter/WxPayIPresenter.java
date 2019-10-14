package com.dayizhihui.dayishi.member.common.pay.persenter;

import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.WeChatOrderBean;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface WxPayIPresenter {
    void orderPersenter(GetAllOrderBean orderBean);
    void getWxOrderBean(WeChatOrderBean.PrepayBean prepay);
}
