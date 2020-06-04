package com.dayizhihui.dayishi.clerk.common.widgets.toolbar

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dayizhihui.dayishi.clerk.R
import com.dayizhihui.dyzhlib.common.BaseActivity
import kotlinx.android.synthetic.main.layout_toolbar.*

/**
 * 标题栏基础Activity
 * Add by LuJinming
 */

open class BaseToolbarActivity:BaseActivity(){
    private lateinit var mContent_frame:FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_toolbar, window.decorView.rootView as ViewGroup, false)
        mContent_frame = view.findViewById(R.id.content_frame)

        val contentView = LayoutInflater.from(this).inflate(layoutResID, mContent_frame, false)

        mContent_frame.addView(contentView)

        setContentView(view)

        initToolBar()

    }

    private fun initToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        setTitle(title)
    }

    override fun setTitle(title: CharSequence?) {
        toolbar_tv_title.text = title
    }

    override fun setTitle(titleId: Int) {
        toolbar_tv_title.setText(titleId)
    }

    //默认的返回按钮
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


    protected fun setRightTextButtonEnable(rid: String, onClickListener: View.OnClickListener) {
        tv_title_right.text = rid
        tv_title_right.setOnClickListener(onClickListener)
        tv_title_right.visibility = View.VISIBLE
        iv_title_right.visibility = View.GONE
    }

    //bar右侧是图标
    protected fun setRightImageButtonEnable(@DrawableRes rid: Int, onClickListener: View.OnClickListener) {
        iv_title_right.setImageResource(rid)
        iv_title_right.setOnClickListener(onClickListener)
        iv_title_right.visibility = View.VISIBLE
        tv_title_right.visibility = View.GONE

    }

}