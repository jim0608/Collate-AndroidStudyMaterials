package com.dayizhihui.dayishi.member.common.pay;

import android.os.Bundle;

import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dyzhlib.mvp.IView;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public interface PayView extends IView {
    void gotoOrderActivity(Bundle bundle);
    void payMethod(GetAllOrderBean orderBean);
    void payError();
    void finishPay();
}
