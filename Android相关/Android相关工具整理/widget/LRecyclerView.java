package com.blackbox.lerist.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.blackbox.lerist.interfaces.LOnScrollListener;
import com.blackbox.lerist.utils.Lerist;

import java.util.ArrayList;


/**
 * Created by Lerist on 2015/9/9, 0009.
 */
public class LRecyclerView extends RecyclerView {
    private View responceView;
    private LOnScrollListener lOnScrollListener;
    private ArrayList<View> mHeaderViews = new ArrayList<>();

    private ArrayList<View> mFooterViews = new ArrayList<>();
    private Adapter mAdapter;
    private boolean isCanScroll = true;
    private boolean isDisableClick = false;

    public LRecyclerView(Context context) {
        this(context, null);
    }

    public LRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (lOnScrollListener != null) {
                    lOnScrollListener.onScrollStateChanged(newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (lOnScrollListener != null) {

                    int orientation = getLayoutOrientation();

                    //垂直滑动
                    if (VERTICAL == orientation || -1 == orientation) {
                        if (dy > 0) {
                            lOnScrollListener.onScrollUp(dy);
                        }
                        if (dy < 0) {
                            lOnScrollListener.onScrollDown(dy);
                        }

                        if (isBordTop() && isBordBottom()) {
                            //在顶部也在底部(仅一页内容)时, 只回调顶部
                            lOnScrollListener.onBordTop();
                        } else {
                            if (isBordTop()) {
                                lOnScrollListener.onBordTop();
                            }
                            if (isBordBottom()) {
                                lOnScrollListener.onBordBottom();
                            }
                        }
                    }


                    //水平滑动
                    if (HORIZONTAL == orientation || -1 == orientation) {
                        if (dx > 0) {
                            lOnScrollListener.onScrollLeft(dx);
                        }

                        if (dx < 0) {
                            lOnScrollListener.onScrollRight(dx);
                        }

                        if (isBordLeft() && isBordRight()) {
                            //在左边界也在右边界(仅一页内容)时, 只回调左边界
                            lOnScrollListener.onBordLeft();
                        } else {
                            if (isBordLeft()) {
                                lOnScrollListener.onBordLeft();
                            }
                            if (isBordRight()) {
                                lOnScrollListener.onBordRight();
                            }
                        }
                    }

                    lOnScrollListener.onScrolled(dx, dy);
                }
            }
        });
    }

    /**
     * 获取当前内容方向
     *
     * @return {@link #VERTICAL} or {@link #HORIZONTAL} or {-1: 不支持的LayoutManager}
     */
    public int getLayoutOrientation() {

        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager || layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        } else {
            Log.e(this.getClass().getName(), "getLayoutOrientation: 不支持的LayoutManager");
        }
        return -1;
    }

    @Override
    public void setAdapter(Adapter adapter) {
//        if (getItemAnimator() == null) {
//            setItemAnimator(new FadeInAnimator());
//        }
        mAdapter = adapter;
        super.setAdapter(adapter);
    }

    /**
     * @param view
     * @deprecated 使用LRecyclerViewAdapter的addHeaderView()
     */
    public void addHeaderView(View view) {
//        mHeaderViews.clear();
//        mHeaderViews.add(view);
//        if (mAdapter != null) {
//            if (!(mAdapter instanceof LRecyclerViewHeaderFooterWrapAdapter)) {
//                mAdapter = new LRecyclerViewHeaderFooterWrapAdapter(mHeaderViews, mFooterViews, mAdapter);
//                setAdapter(mAdapter);
//            }
//            post(new Runnable() {
//                @Override
//                public void run() {
//                    mAdapter.notifyItemInserted(0);
//                }
//            });
//        }
    }

    /**
     * @param view
     * @deprecated 使用LRecyclerViewAdapter的addFooterView()
     */
    public void addFooterView(View view) {
//        mFooterViews.clear();
//        mFooterViews.add(view);
//        if (mAdapter != null) {
//            if (!(mAdapter instanceof LRecyclerViewHeaderFooterWrapAdapter)) {
//                mAdapter = new LRecyclerViewHeaderFooterWrapAdapter(mHeaderViews, mFooterViews, mAdapter);
//                setAdapter(mAdapter);
//            }
//            try {
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * @deprecated 使用LRecyclerViewAdapter的removeHeaderView()
     */
    public void removeHeaderView() {
//        if (mHeaderViews.isEmpty()) return;
//
//        mHeaderViews.clear();
//        if (mAdapter != null) {
//            if (!(mAdapter instanceof LRecyclerViewHeaderFooterWrapAdapter)) {
//            } else {
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAdapter.notifyItemRemoved(0);
//                    }
//                });
//            }
//        }
    }

    /**
     * @deprecated 使用LRecyclerViewAdapter的removeFooterView()
     */
    public void removeFooterView() {
//        if (mFooterViews.isEmpty()) return;
//        mFooterViews.clear();
//        if (mAdapter != null) {
//            if (!(mAdapter instanceof LRecyclerViewHeaderFooterWrapAdapter)) {
//            } else {
//                try {
//                    post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mAdapter.getItemCount() >= 0)
//                                mAdapter.notifyItemRemoved(mAdapter.getItemCount());
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    @Override
    public int getVerticalFadingEdgeLength() {
        return 0;
    }

    public boolean isCanScroll() {
        return isCanScroll;
    }

    public void setCanScroll(boolean canScroll) {
        isCanScroll = canScroll;
    }

    public boolean isDisableClick() {
        return isDisableClick;
    }

    public void setDisableClick(boolean disableClick) {
        isDisableClick = disableClick;
    }

    //    @Override
//    public boolean onInterceptTouchEvent(MotionEvent e) {
//
//        if (!isCanScroll && e.getAction() != MotionEvent.ACTION_DOWN) {
//            //禁止滑动
//            ViewGroup parent = (ViewGroup) getParent();
//            if (parent != null) {
//                parent.requestDisallowInterceptTouchEvent(false);
//                parent.onTouchEvent(e);
//            }
//            return false;
//        }
//
//        return super.onInterceptTouchEvent(e);
//    }

    float mLastMotionY = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isCanScroll) {
            //禁止滑动(保留点击事件)
            if (!(event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP)) {
                return false;
            }
        }

        if (isDisableClick) {
            //禁用点击
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mLastMotionY = event.getY();
                if (responceView != null) {
                    ((ViewParent) responceView).requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE: {

                float direction = mLastMotionY - event.getY();
                mLastMotionY = event.getY();

                //  Log.i("msg", "scroll: " + getScrollY() + "  MeasuredHeight:" + getMeasuredHeight() + " ContentHeight:" + getChildAt(0).getMeasuredHeight() + "   direction: " + direction);

                if ((isBordTop() && direction < 0)) {
                    //从顶部下滑
                    //将触摸事件交给父控件处理
                    if (responceView != null) {
                        ((ViewParent) responceView).requestDisallowInterceptTouchEvent(false);
                        responceView.onTouchEvent(event);
                        return false;
                    }
                } else if ((isBordBottom() && direction > 0)) {
                    //从底部上滑
                    //将触摸事件交给父控件处理
                    if (responceView != null) {
                        ((ViewParent) responceView).requestDisallowInterceptTouchEvent(false);
                        responceView.onTouchEvent(event);
                        return false;
                    }
                } else {
                    if (responceView != null) {
                        //告诉responceView，我的事件自己处理
                        ((ViewParent) responceView).requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (responceView != null) {
                    ((ViewParent) responceView).requestDisallowInterceptTouchEvent(false);
                }
                break;
        }

        try {
            return super.dispatchTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 设置滑动到顶部或底部响应的View
     *
     * @param responceView
     */
    public void setResponceView(View responceView) {
        this.responceView = responceView;
    }

    /**
     * 在顶部
     *
     * @return
     */
    public boolean isBordTop() {
        //canScrollVertically(1)的值表示是否能向上滚动，true表示能滚动，false表示已经滚动到底部
        //canScrollVertically(-1)的值表示是否能向下滚动，true表示能滚动，false表示已经滚动到顶部
        return !canScrollVertically(-1);
////        return getScrollState() == RecyclerView.SCROLL_STATE_IDLE && getLastVisibleItemPosition(true) <= 1;
//        return getLastVisibleItemPosition(true) == 0;
    }

    /**
     * 在底部
     *
     * @return
     */
    public boolean isBordBottom() {
        //canScrollVertically(1)的值表示是否能向上滚动，true表示能滚动，false表示已经滚动到底部
        //canScrollVertically(-1)的值表示是否能向下滚动，true表示能滚动，false表示已经滚动到顶部
        return !canScrollVertically(1);
//        if (getChildCount() <= 0) {
//            return true;
//        }
////        return getScrollState() == RecyclerView.SCROLL_STATE_IDLE && getLastVisibleItemPosition(false) >= getLayoutManager().getItemCount() - 1;
//        return getLastVisibleItemPosition(false) >= getLayoutManager().getItemCount() - 1;
    }


    public boolean isBordLeft() {
        //canScrollHorizontally(1)的值表示是否能向右滚动，true表示能滚动，false表示已经滚动到底部
        //canScrollHorizontally(-1)的值表示是否能向左滚动，true表示能滚动，false表示已经滚动到顶部
        return !canScrollHorizontally(-1);
    }

    public boolean isBordRight() {
        //canScrollHorizontally(1)的值表示是否能向右滚动，true表示能滚动，false表示已经滚动到底部
        //canScrollHorizontally(-1)的值表示是否能向左滚动，true表示能滚动，false表示已经滚动到顶部
        return !canScrollHorizontally(1);
    }

    public int getLastVisibleItemPosition(boolean scrollDown) {
        int lastCompletelyVisibleItemPosition = 0;
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            lastCompletelyVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            lastCompletelyVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] ints = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(ints);
            lastCompletelyVisibleItemPosition = (int) (scrollDown ? Lerist.findMin(ints) : Lerist.findMax(ints));
        } else {
            Log.e(this.getClass().getName(), "getLastVisibleItemPosition: 不支持的LayoutManager");
            lastCompletelyVisibleItemPosition = -1;
        }
        return lastCompletelyVisibleItemPosition;
    }

    public void setLOnScrollListener(LOnScrollListener lOnScrollListener) {
        this.lOnScrollListener = lOnScrollListener;
    }

    public static class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            if (getItemCount() <= 0) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
                return;
            }

            try {
                View view = recycler.getViewForPosition(0);
                if (view != null) {
                    measureChild(view, widthSpec, heightSpec);
                    int measuredWidth = MeasureSpec.getSize(widthSpec);
                    int measuredHeight = view.getMeasuredHeight();
                    setMeasuredDimension(measuredWidth, measuredHeight);
                }
            } catch (Exception e) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
            }
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("probe", "meet a IOOBE in RecyclerView");
            }
        }
    }

    public static class WrapContentGridLayoutManager extends GridLayoutManager {


        public WrapContentGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public WrapContentGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public WrapContentGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        @Override
        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            if (getItemCount() <= 0) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
                return;
            }
            try {
                View view = recycler.getViewForPosition(0);
                if (view != null) {
                    measureChild(view, widthSpec, heightSpec);
                    int measuredWidth = MeasureSpec.getSize(widthSpec);
                    int measuredHeight = view.getMeasuredHeight();
                    setMeasuredDimension(measuredWidth, measuredHeight);
                }
            } catch (Exception e) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
            }
        }
    }

    /**
     * 通用分割线
     *
     * @addItemDecoration
     */
    public static class RecycleViewDivider extends RecyclerView.ItemDecoration {

        private Paint mPaint;
        private Drawable mDivider;
        private int mDividerHeight = 2;//分割线高度，默认为1px
        private int mOrientation;//列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
        private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

        /**
         * 默认分割线：高度为2px，颜色为灰色
         *
         * @param context
         * @param orientation 列表方向
         */
        public RecycleViewDivider(Context context, int orientation) {
            if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL && orientation != -1) {
                throw new IllegalArgumentException("请输入正确的参数！(orientation)");
            }
            mOrientation = orientation;

            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
        }

        /**
         * 自定义分割线
         *
         * @param context
         * @param orientation 列表方向
         * @param drawableId  分割线图片
         */
        public RecycleViewDivider(Context context, int orientation, int drawableId) {
            this(context, orientation);
            mDivider = ContextCompat.getDrawable(context, drawableId);
            mDividerHeight = mDivider.getIntrinsicHeight();
        }

        /**
         * 自定义分割线
         *
         * @param context
         * @param orientation   列表方向 LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL, -1:水平垂直都绘制
         * @param dividerHeight 分割线高度
         * @param dividerColor  分割线颜色
         */
        public RecycleViewDivider(Context context, int orientation, int dividerHeight, int dividerColor) {
            this(context, orientation);
            mDividerHeight = dividerHeight;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(dividerColor);
            mPaint.setStyle(Paint.Style.FILL);
        }


        //获取分割线尺寸
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
//            if (mOrientation == LinearLayoutManager.VERTICAL) {
//                outRect.set(0, mDividerHeight / 2, 0, mDividerHeight / 2);
//            } else if (mOrientation == LinearLayoutManager.HORIZONTAL) {
//                outRect.set(mDividerHeight / 2, 0, mDividerHeight / 2, 0);
//            } else {
//                outRect.set(mDividerHeight / 2, mDividerHeight / 2,
//                        mDividerHeight / 2, mDividerHeight / 2);
//            }
        }

        //绘制分割线
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            super.onDrawOver(c, parent, state);
            if (mOrientation == LinearLayoutManager.VERTICAL) {
                drawVertical(c, parent);
            } else if (mOrientation == LinearLayoutManager.HORIZONTAL) {
                drawHorizontal(c, parent);
            } else {
                drawBoth(c, parent);
            }
        }

        private void drawBoth(Canvas canvas, RecyclerView parent) {
            final int parentLeft = parent.getPaddingLeft();
            final int parentRight = parent.getMeasuredWidth() - parent.getPaddingRight();
            final int parentTop = parent.getPaddingTop();
            final int parentBottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
            final int childSize = parent.getChildCount();
            for (int i = 0; i < childSize; i++) {
                final View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getTop() - layoutParams.topMargin;
                final int bottom = child.getBottom() + layoutParams.bottomMargin;
                final int left = child.getLeft() - layoutParams.leftMargin;
                final int right = child.getRight() + layoutParams.rightMargin;
                int dividerSize = mDividerHeight / 2;
                if (mDivider != null) {
                    //水平
                    mDivider.setBounds(left, top, left + dividerSize, bottom);
                    mDivider.draw(canvas);
                    mDivider.setBounds(right, top, right + dividerSize, bottom);
                    mDivider.draw(canvas);
                    //垂直
                    mDivider.setBounds(left, top, right, top + dividerSize);
                    mDivider.draw(canvas);
                    mDivider.setBounds(left, bottom, right, bottom + dividerSize);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, left + dividerSize, bottom, mPaint);
                    canvas.drawRect(right - dividerSize, top, right, bottom, mPaint);
                    canvas.drawRect(left, top, right, top + dividerSize, mPaint);
                    canvas.drawRect(left, bottom - dividerSize, right, bottom, mPaint);
                }
            }
        }

        //绘制横向 item 分割线
        private void drawHorizontal(Canvas canvas, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
            final int childSize = parent.getChildCount();
            for (int i = 0; i < childSize; i++) {
                final View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + layoutParams.bottomMargin;
                final int bottom = top + mDividerHeight;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }

        //绘制纵向 item 分割线
        private void drawVertical(Canvas canvas, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
            final int childSize = parent.getChildCount();
            for (int i = 0; i < childSize; i++) {
                final View child = parent.getChildAt(i);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + layoutParams.rightMargin;
                final int right = left + mDividerHeight;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }
    }

    /**
     * 间隔
     */
    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager || layoutManager instanceof StaggeredGridLayoutManager) {
                int childLayoutPosition = parent.getChildLayoutPosition(view);
                int spanCount;
                if (layoutManager instanceof GridLayoutManager) {
                    spanCount = ((GridLayoutManager)
                            layoutManager).getSpanCount();
                } else {
                    spanCount = ((StaggeredGridLayoutManager)
                            layoutManager).getSpanCount();
                }
                //是否处于第一行
                boolean isFristLine = childLayoutPosition < spanCount;
                //是否处于最后一行
                boolean isLastLine = childLayoutPosition >= parent.getAdapter().getItemCount() - spanCount;
                //所在行的位置
                int linePosition = childLayoutPosition % spanCount;
                //是否处于第一列
                boolean isFristColumn = linePosition == 0;
                //是否处于最后一列
                boolean isLastColumn = linePosition == spanCount - 1;

                if (isFristLine) {
                    outRect.top = space;
                } else {
                    outRect.top = space / 2;
                }

                if (isLastLine) {
                    outRect.bottom = space;
                } else {
                    outRect.bottom = space / 2;
                }

                if (isFristColumn) {
                    outRect.left = space;
                } else {
                    outRect.left = space / 2;
                }

                if (isLastColumn) {
                    outRect.right = space;
                } else {
                    outRect.right = space / 2;
                }

            } else if (layoutManager instanceof LinearLayoutManager) {
                if (parent.getChildLayoutPosition(view) == 0) {
                    outRect.top = space;
                    outRect.bottom = space;
                } else {
                    outRect.bottom = space;
                }
            }
        }
    }
}