package com.dayizhihui.dayishi.member.common.pay.persenter;


import android.content.Context;
import android.os.Bundle;

import com.blankj.ALog;
import com.dayizhihui.dayishi.member.common.Global;
import com.dayizhihui.dayishi.member.common.pay.PayView;
import com.dayizhihui.dayishi.member.percenter.bean.GetAllOrderBean;
import com.dayizhihui.dayishi.member.common.bean.OrderDetailsBean;
import com.dayizhihui.dayishi.member.common.bean.PayServiceRequest;
import com.dayizhihui.dayishi.member.common.pay.model.IntegralModel;
import com.dayizhihui.dayishi.member.common.pay.model.IntegralPModel;
import com.dayizhihui.dayishi.member.percenter.bean.AddressBean;
import com.dayizhihui.dyzhlib.mvp.presenter.BasePresenter;
import com.dayizhihui.dyzhlib.utils.PreferenceUtil;

import java.util.List;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/10 0010.
 */
public class IntegralPayPresenter extends BasePresenter<IntegralPModel, PayView> implements IntegralPayIPresenter {
    private String cjId = PreferenceUtil.getPrefString(Global.T_CJ_ID, "");
    private String userId = PreferenceUtil.getPrefString(Global.T_ID, "");


    @Override
    public void orderPersenter(AddressBean addressInfo, List<OrderDetailsBean> detailsBeans, int order_type, int item_type) {
        PayServiceRequest result = new PayServiceRequest();
        result.setCj_id(cjId);
        result.setUser_id(userId);
        result.setCus_id(userId);
        result.setProvince_id(addressInfo.getProvince_id());//省
        result.setCity_id(addressInfo.getCity_id());//市
        result.setDistrict_id(addressInfo.getDistrict_id());//区
        result.setDetailed_address(addressInfo.getDetailed_address());//地址详情
        result.setCus_name(addressInfo.getCus_name());// 收货人姓名
        result.setCus_phone(addressInfo.getCus_phone());// 收货人手机号
        result.setSex(Integer.parseInt(addressInfo.getSex()));//性别1男0女
        result.setOrder_type(order_type);//订单类型 0药店配送，1到店自取
        result.setItem_type(String.valueOf(item_type));//0折扣商品 1积分商品
        result.setOrderDetails(detailsBeans);//商品明细
        result.setRemarks("");//备注
        getModel().orderModel(result, (Context) getView(), this);
    }

    @Override
    public void getOrderBean(GetAllOrderBean orderBean) {
        getView().payMethod(orderBean);
    }

    @Override
    public void toIntegerPay(GetAllOrderBean orderBean) {
        getModel().payModel(orderBean, this);
    }

    @Override
    public void paySucess(GetAllOrderBean orderBean) {
        Bundle bundle = new Bundle();
        bundle.putInt("flag",1);
        bundle.putInt("itemType", orderBean.getItem_type());
        ALog.d(orderBean.toString());
        getView().gotoOrderActivity(bundle);
    }

    @Override
    public void payError() {
        getView().payError();
    }

    @Override
    public IntegralPModel createModel() {

        return new IntegralModel();
    }
}
