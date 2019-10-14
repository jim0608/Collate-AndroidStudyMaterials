#TextView设置文本侧边的图标：
 
 -可以在TextView中设置drawableBottom、drawableLeft...,但是就是不能在XML中设置图片大小，需要自定义TextView才可以
 -在代码中设置
 ```
 {
	 Drawable drawable=getResources().getDrawable(R.drawable.ic_phone);
	 drawable.setBounds(0,0,30,35);//第一0是距左边距离，第二0是距上边距离，30、35分别是长宽
	 tv_phone.setCompoundDrawables(drawable,null,null,null);//只放左边
 }
 ```
 ---
##设置textView结尾...后面显示的文字和颜色，，超过行数则显示省略号
```java
public class TextViewMaxLinesUtil {
    /**
     * 设置textView结尾...后面显示的文字和颜色
     *
     * @param context    上下文
     * @param textView   TextView
     * @param maxLines   当前TextView的maxLines
     * @param originText 原文本
     * @param endText    结尾文字
     * @param endColorID 结尾文字颜色id
     * @param func       点击事件
     * @param isExpand   当前是否是展开状态
     */
    public static void setTextViewShowMore(final Context context,
                                           final TextView textView,
                                           final int maxLines,
                                           final String originText,
                                           final String endText,
                                           @ColorRes final int endColorID,
                                           final Action1<?> func,
                                           final boolean isExpand) {
        if (TextUtils.isEmpty(originText)) {
            return;
        }
        textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (isExpand) {
                    textView.setText(originText);
                } else {
                    int paddingLeft = textView.getPaddingLeft();
                    int paddingRight = textView.getPaddingRight();
                    TextPaint paint = textView.getPaint();
                    //特意多算一个字的长度，因为可能由于标点符号等影响"endText"在一行显示不全
                    float moreTextLen = paint.measureText(endText) + textView.getTextSize();
                    final float originTextLen = paint.measureText(originText);
                    float availableTextWidth = (textView.getWidth() - paddingLeft - paddingRight) * maxLines;
                    if (originTextLen <= availableTextWidth) {
                        return;
                    }
                    CharSequence ellipsizeStr = TextUtils.ellipsize(originText, paint,
                            availableTextWidth - moreTextLen, TextUtils.TruncateAt.END);
                    if (ellipsizeStr.length() < originText.length()) {
                        class MyClick extends ClickableSpan {
                            @Override
                            public void onClick(View widget) {
                                func.call();
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setColor(context.getResources().getColor(endColorID));
                            }
                        }
                        CharSequence temp = ellipsizeStr + endText;
                        SpannableStringBuilder ssb = new SpannableStringBuilder(temp);
                        //这个一定要记得设置，不然点击不生效
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        ssb.setSpan(new MyClick(), temp.length() - endText.length(), temp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        textView.setText(ssb);
                    } else {
                        textView.setText(originText);
                    }
                }
            }
        });
    }

    public interface Action1<T> {
        public void call(T... t);
    }

}
```
---
##同一个字符串中设置不同的颜色
###学习网址：https://blog.csdn.net/wzping435/article/details/53522076——https://blog.csdn.net/Duxiaopan96/article/details/77648354
```
TextView wifi_hint_tv = (TextView) findViewById(R.id.wifi_hint_tv);
String tipsStr = "<font color=\"#939393\">" + getString(R.string.config_wifi_hint) + "</font>" + "<font color=\"#2ec6f6\">" + getString(R.string.switch_wifi) + "</font>";
wifi_hint_tv.setText(Html.fromHtml(tipsStr));//这儿是把颜色展示出来的重点
```
#设置在TextView字体当中画一条横线
```
textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
```
