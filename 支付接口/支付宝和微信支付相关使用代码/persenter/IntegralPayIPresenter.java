package com.dayizhihui.dayishi.member.common.pay.persenter;

import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;

import com.dayizhihui.dayishi.member.common.bean.OrderDetailsBean;
import com.dayizhihui.dayishi.member.percenter.bean.AddressBean;

import java.util.List;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface IntegralPayIPresenter {
    void orderPersenter(AddressBean addressInfo, List<OrderDetailsBean> detailsBeans, int order_type, int item_type);
    void getOrderBean(GetAllOrderBean orderBean);
    void toIntegerPay(GetAllOrderBean orderBean);
    void paySucess(GetAllOrderBean itemType);
    void payError();
}
