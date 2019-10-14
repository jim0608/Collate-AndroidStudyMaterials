# ToolBar

[Toolbar的详细介绍和自定义Toolbar](https://blog.csdn.net/da_caoyuan/article/details/79557704)   
[MeterialDesign系列文章（一）ToolBar的使用](https://juejin.im/post/5ad84f38f265da50412ebc75#heading-11)   
[Android ToolBar 的简单封装](https://www.cnblogs.com/brucemengbm/p/7074218.html)   
[ToolBar封装策略](https://juejin.im/post/5ac1a251f265da2397070463)   

## 封装标题栏

主要抄袭 [ToolBar封装策略](https://juejin.im/post/5ac1a251f265da2397070463)的策略一

### 实现步骤

1. 写布局文件
    - 布局文件头部为 ToolBar
    - ToolBar继承自GroupView,因此可在布局内添加自定义的布局内容(标题，左侧文字，右侧文字等)
    - 剩下部分由 FrameLayout 占满用于填充内容页面
2. 写Activity通用继承类 BaseToolbarActivity
    - 重写setContentView
        + 将content页面进行填充
        + 设置ToolBar
    - 写一些标题栏的设置方法
        + 重写设置标题方法 setTitle
        + 设置文字信息及监听显隐

    ```
    //主要方法
    open class BaseToolbarActivity:AppCompatActivity(){
        private lateinit var mContent_frame:FrameLayout
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
    }
    ```

### 使用方法

```
//方式一 只有标题与返回按钮
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_personal_info)
    title = "我的信息"
}

//方式二 显示按钮
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_personal_info)
    title = "我的信息"

    /*setRightTextButtonEnable("个人简介", View.OnClickListener {
        showToast("个人简介")
    })*/

    setRightImageButtonEnable(R.drawable.star,View.OnClickListener {
        showToast("star")
    })
}

//方式三 使用ToolBar自带的按钮
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_personal_info)
    title = "我的信息"

}

override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_basic, menu)
    return true
}

//menu_basic.xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/action_search"
        android:icon="@drawable/star"
        android:orderInCategory="100"
        android:title="Search"
        app:showAsAction="always" />

    <item
        android:id="@+id/action_settings"
        android:orderInCategory="100"
        android:title="Settings"
        app:showAsAction="never" />
</menu>

```

## ToolBar使用总结
[Toolbar的详细介绍和自定义Toolbar](https://blog.csdn.net/da_caoyuan/article/details/79557704)   

1. 设置左侧 home按钮图标
    ```
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_menu);
    ```
2. 标题的设置
    - 如果使用默认的标题位置偏左
    - 可以在ToolBar闭包中添加TextView作为标题
        + 可设置居中
        + 设置原标题为空`getSupportActionBar.setTitle("")`
3. 关于menu文件
    - app:showAsAction 属性
        + ifRoom 表示在屏幕空间足够的情况下显示在Toolbar中，不够的话就显示在菜单中
        + never 表示永远不显示在Toolbar中，而是一直显示在菜单中
        + always 表示永远显示在Toolbar中，如果屏幕空间不够则不显示
    - action按钮只会显示图标，菜单中的action按钮只会显示文字。


