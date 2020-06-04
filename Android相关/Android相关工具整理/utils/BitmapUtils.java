package com.blackbox.lerist.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by Lerist on 2015/9/4, 0004.
 */
public class BitmapUtils {
    /**
     * 获取bitmap的亮度
     *
     * @param bitmap
     * @return
     */
    public static int getBright(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int y[] = {0, 4, 9, 13, 18, 23, 28, 33, 38, 43, 48};
        int x[] = {0, width / 8, width * 2 / 8, width * 3 / 8,
                width * 4 / 8, width * 5 / 8, width * 6 / 8,
                width * 7 / 8, width - 1};

        int r;
        int g;
        int b;
        int number = 0;
        double tempBright = 0;
        Integer localTemp;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++) {
                number++;
                localTemp = bitmap.getPixel(x[i], y[j]);
                r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                b = (localTemp | 0xffffff00) & 0x0000ff;
                tempBright = tempBright + 0.299 * r + 0.587 * g + 0.114 * b;
            }
        }
        bitmap.recycle();
        int bright = (int) (tempBright / number);

        return bright;
    }

    /**
     * 根据质量压缩
     *
     * @param bitmap
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap, int maxSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int quality = 90;
        while (baos.toByteArray().length / 1024 > maxSize && quality >= 0) {  //循环判断如果压缩后图片是否大于maxSize kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//这里压缩options%，把压缩后的数据存放到baos中
            quality -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap newBitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片


        //镜子效果
//        Matrix matrix = new Matrix();
//        matrix.setScale(-1, 1);
//        matrix.postTranslate(newBitmap.getWidth(), 0);
//        newBitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth()
//                , newBitmap.getHeight(), matrix, true);

        bitmap.recycle();
        return newBitmap;
    }

    /**
     * 设置位图大小
     *
     * @param bitmap
     * @param maxWidth 指定最大宽度(压缩图片)
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //如果原图片比指定宽度小则不压缩
        if (width <= maxWidth) {
            return bitmap;
        }
        float temp = ((float) height) / ((float) width);
        int newHeight = (int) ((maxWidth) * temp);
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }

    /**
     * 压缩图片
     *
     * @param bitmap   源图片
     * @param width    想要的宽度
     * @param height   想要的高度
     * @param isAdjust 是否自动调整尺寸, true图片就不会拉伸，false严格按照你的尺寸压缩
     * @return Bitmap
     */
    public static Bitmap reduce(Bitmap bitmap, int width, int height, boolean isAdjust) {
        // 如果想要的宽度和高度都比源图片小，就不压缩了，直接返回原图
        if (bitmap.getWidth() < width && bitmap.getHeight() < height) {
            return bitmap;
        }
        // 根据想要的尺寸精确计算压缩比例, 方法详解：public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode);
        // scale表示要保留的小数位, roundingMode表示如何处理多余的小数位，BigDecimal.ROUND_DOWN表示自动舍弃
        float sx = new BigDecimal(width).divide(new BigDecimal(bitmap.getWidth()), 4, BigDecimal.ROUND_DOWN).floatValue();
        float sy = new BigDecimal(height).divide(new BigDecimal(bitmap.getHeight()), 4, BigDecimal.ROUND_DOWN).floatValue();
        if (isAdjust) {// 如果想自动调整比例，不至于图片会拉伸
            sx = (sx < sy ? sx : sy);
            sy = sx;// 哪个比例小一点，就用哪个比例
        }
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);// 调用api中的方法进行压缩，就大功告成了
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 放大或缩小图片
     *
     * @param bitmap 源图片
     * @param ratio  放大或缩小的倍数，大于1表示放大，小于1表示缩小
     * @return Bitmap
     */
    public static Bitmap zoom(Bitmap bitmap, float ratio) {
        if (ratio < 0f) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 创建logo(给图片加水印),
     *
     * @param bitmaps 原图片和水印图片
     * @param left    左边起点坐标
     * @param top     顶部起点坐标t
     * @return Bitmap
     */
    public static Bitmap createLogo(Bitmap[] bitmaps, int left, int top) {
        Bitmap newBitmap = Bitmap.createBitmap(bitmaps[0].getWidth(), bitmaps[0].getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        for (int i = 0; i < bitmaps.length; i++) {
            if (i == 0) {
                canvas.drawBitmap(bitmaps[0], 0, 0, null);
            } else {
                canvas.drawBitmap(bitmaps[i], left, top, null);
            }
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        }
        return newBitmap;
    }

    /**
     * 在图片上印字
     *
     * @param bitmap 源图片
     * @param text   印上去的字
     * @param param  字体参数分别为：颜色,大小,是否加粗,起点x,起点y; 比如：{color : 0xFF000000, size : 30, bold : true, x : 20, y : 20}
     * @return Bitmap
     */
    public static Bitmap printWord(Bitmap bitmap, String text, Map<String, Object> param) {
        if (StringUtils.isEmpty(text) || null == param) {
            return bitmap;
        }
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        Paint paint = new Paint();
        paint.setColor(null != param.get("color") ? (Integer) param.get("color") : Color.BLACK);
        paint.setTextSize(null != param.get("size") ? (Integer) param.get("size") : 20);
        paint.setFakeBoldText(null != param.get("bold") ? (Boolean) param.get("bold") : false);
        canvas.drawText(text, null != param.get("x") ? (Integer) param.get("x") : 0, null != param.get("y") ? (Integer) param.get("y") : 0, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newBitmap;
    }

    /**
     * 旋转图片
     *
     * @param bitmap 源图片
     * @param angle  旋转角度(90为顺时针旋转,-90为逆时针旋转)
     * @return Bitmap
     */
    public static Bitmap rotate(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 水平翻转
     *
     * @param bitmap
     * @return
     */
    public static Bitmap flipHorizontal(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 垂直翻转
     *
     * @param bitmap
     * @return
     */
    public static Bitmap flipVertical(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    /**
     * 把Bitmap转Byte
     */
    public static byte[] bitmap2Bytes(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
