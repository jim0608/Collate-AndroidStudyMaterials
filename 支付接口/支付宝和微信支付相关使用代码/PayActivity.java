package com.dayizhihui.dayishi.member.common.pay;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.ALog;
import com.bumptech.glide.Glide;
import com.dayizhihui.dayishi.member.R;
import com.dayizhihui.dayishi.member.main.MainActivity;
import com.dayizhihui.dayishi.member.mall.adapters.RecommendAdapter;
import com.dayizhihui.dayishi.member.mall.bean.DrugItemBean;
import com.dayizhihui.dayishi.member.mall.presenter.BuyGoodsPercenter;
import com.dayizhihui.dayishi.member.percenter.OrderCenterActivity;
import com.dayizhihui.dayishi.member.percenter.presenter.OrderCenterPresenter;
import com.dayizhihui.dyzhlib.common.ActivityManager;
import com.dayizhihui.dyzhlib.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Descrip：
 * Author： Zhangjinming
 * CreateTime on 2019/4/18 0018.
 */
public class PayActivity extends BaseActivity implements CommonView {
    private View view_loveOfYou;
    private ImageView img_pay_state;
    private TextView tv_pay_state, tv_gotoMainActivity, tv_view_orders;
    //一般变量
    private BuyGoodsPercenter percenter;

    private int flag;
    private int errCode;
    private String resultStatus;
    private int itemType;
    private String itemTypes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_status);
        percenter = new BuyGoodsPercenter();
        percenter.attachView(this);

        flag = getIntent().getExtras().getInt("flag");

        init();
    }


    private void init() {
        view_loveOfYou = (View) findViewById(R.id.view_loveOfYou);//猜你喜欢
        img_pay_state = (ImageView) findViewById(R.id.img_pay_state);//图片
        tv_pay_state = (TextView) findViewById(R.id.tv_pay_state);//结果

        tv_gotoMainActivity = (TextView) findViewById(R.id.tv_gotoMainActivity);//回首页
        tv_view_orders = (TextView) findViewById(R.id.tv_view_orders);//去订单中心


        tv_gotoMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityManager.Instance().finishOtherActivity(MainActivity.class);
            }
        });
        tv_view_orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(OrderCenterActivity.class);
                ActivityManager.Instance().finishOtherActivity(MainActivity.class);
            }
        });
        initView();
        if (flag == 1) {//积分
            ALog.d(itemType);
            initInteger();
        } else if (flag == 2) {//微信
            errCode = getIntent().getExtras().getInt("errCode");
            initWxPayStates();
        } else if (flag == 3) {//ali
            resultStatus = getIntent().getExtras().getString("resultStatus");
            initAliPay();
        }
    }



    //猜你喜欢数据
    private void initView() {
        itemType = getIntent().getExtras().getInt("itemType");
        final List<DrugItemBean> footData = new ArrayList<>();
        final GridView gridView = view_loveOfYou.findViewById(R.id.gv_likes);
        percenter.getRecommend(new BuyGoodsPercenter.GetRecommendListener() {
            @Override
            public void getRecommends(List<DrugItemBean> datas) {
                Random random = new Random();
                int s = random.nextInt(datas.size()) % (datas.size() + 1);
                footData.add(datas.get(s));
                footData.add(datas.get(random.nextInt(datas.size())));
                footData.add(datas.get(random.nextInt(datas.size())));

                RecommendAdapter gridBaseAdapter = new RecommendAdapter(PayActivity.this, footData);
                gridView.setAdapter(gridBaseAdapter);
            }
        }, itemType);
    }

    //积分支付
    private void initInteger(){
        Glide.with(this)
                .load(R.drawable.ic_personal_group_tips_success)
                .into(img_pay_state);
        tv_pay_state.setText("支付成功");
    }

    //微信支付返回
    private void initWxPayStates() {
        if (errCode == 0) {
            Glide.with(this)
                    .load(R.drawable.ic_personal_group_tips_success)
                    .into(img_pay_state);
            tv_pay_state.setText("支付成功");

        } else if (errCode == -1) {
            Glide.with(this)
                    .load(R.drawable.ic_personal_group_tips_warning)
                    .into(img_pay_state);
            tv_pay_state.setText("支付失败，请稍后重试！\n如果重试后还无法支付请联系我们客服！");
        } else if (errCode == -2) {
            Glide.with(this)
                    .load(R.drawable.ic_personal_group_check_fail)
                    .into(img_pay_state);
            tv_pay_state.setText("支付失败，您可以稍后在订单中心支付！");
        }
    }
    //Ali支付返回
    private void initAliPay() {
        if (resultStatus.equals("9000")){
            Glide.with(this)
                    .load(R.drawable.ic_personal_group_tips_success)
                    .into(img_pay_state);
            tv_pay_state.setText("支付成功");
        }else {
            Glide.with(this)
                    .load(R.drawable.ic_personal_group_check_fail)
                    .into(img_pay_state);
            tv_pay_state.setText("支付失败，您可以稍后在订单中心支付！");
        }
    }
}
