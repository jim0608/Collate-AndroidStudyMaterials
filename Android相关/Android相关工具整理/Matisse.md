# Matisse
[Matisse](https://github.com/zhihu/Matisse)

引入
`implementation 'com.zhihu.android:matisse:0.5.1'`

打开相册
```
//初始化
Matisse.from(this@pickerPhoto)
                    .choose(MimeType.ofImage())//图片类型
                    .countable(false)//true:选中后显示数字;false:选中后显示对号
                    .maxSelectable(1)//可选的最大数
                    .capture(true)//是否可以拍照
                    .captureStrategy(CaptureStrategy(true, "com.dayizhihui.dayishi.clerk.FileProvider"))
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .imageEngine(GlideLoadEngine())

                    .forResult(request)

```

处理用户选择
```
/**
     * 处理用户选择的图片
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                SELECT_PHOTO_INDEX -> {
                   var mSelected = Matisse.obtainPathResult(data)

                    ALog.d(mSelected[0].toString())

                    if(mSelected != null){
                        val path = mSelected[0].toString()
                        val file = File(path)
                        if (!file.exists()) {
                            showToast("未选择图片")
                        } else {
                            //半身照本地地址
                            mIllPath = path
                            Glide.with(this).load(file).into(activity_personal_info_introduce_ill)
                        }
                    }

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
```


```
//文件

//AndroidManifest provider
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="com.dayizhihui.dayishi.clerk.FileProvider"
    android:exported="false"
    android:grantUriPermissions="true"
    tools:replace="android:authorities">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/custom_file_paths"
        tools:replace="android:resource"/>
</provider>

//custom_file_paths
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <!-- Matisse start -->
    <paths>
        <external-path
            name="my_images"
            path="Pictures" />
    </paths>
    <!-- Matisse end -->


    <!-- 融云配置 start-->
    <external-path
        name="rc_external_path"
        path="." />
    <root-path
        name="rc_root_path"
        path="" />
    <!-- 融云配置 end-->

</resources>
```

### 设置主题颜色

默认有两种主题 Matisse.Zhihu / Matisse.Dracula
```
Matisse.from(this@PersonalSettingActivity)
            .choose(MimeType.ofImage())//图片类型
            .theme(R.style.Matisse.Zhihu)
```

### 使用Glide 4.0以上时

需要重写Glide加载引擎
```
public class GlideLoadEngine implements ImageEngine {

    /**
     * Load thumbnail of a static image resource.
     *
     * @param context     Context
     * @param resize      Desired size of the origin image
     * @param placeholder Placeholder drawable when image is not loaded yet
     * @param imageView   ImageView widget
     * @param uri         Uri of the loaded image
     */
    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView,
                                 Uri uri) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }
}
```

### 问题

1. 使用 v0.5.1 时，选择超过限定张照片崩溃
    string中加入
    ```
    <plurals name="error_over_count">
        <item quantity="one">你只能选择一个文件</item>
        <item quantity="many">你只能选择 %1$d 个文件</item>
    </plurals>
    ```