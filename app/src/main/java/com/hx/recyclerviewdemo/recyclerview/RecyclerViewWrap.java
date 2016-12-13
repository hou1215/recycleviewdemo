package com.hx.recyclerviewdemo.recyclerview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.OnLoadMoreListener;
import com.aspsine.irecyclerview.OnLoadMoreScrollListener;
import com.aspsine.irecyclerview.OnRefreshListener;
import com.aspsine.irecyclerview.RefreshHeaderLayout;
import com.aspsine.irecyclerview.RefreshTrigger;
import com.aspsine.irecyclerview.SimpleAnimatorListener;
import com.aspsine.irecyclerview.WrapperAdapter;

/**
 * 封装的上拉刷新下拉加载 并可添加头尾部的 RecyclerView
 * Created by dzl on 16/3/3.
 */
public class RecyclerViewWrap extends IRecyclerView {

    int header_count = 0;
    int footer_count = 0;

    @Override
    public void addHeaderView(View headerView) {
        header_count++;
        getHeaderContainer().addView(headerView);
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void addFooterView(View footerView) {
        int count = getFooterContainer().getChildCount();
        if (count <= 0){
            footer_count = 0;
        }
        footer_count++;
        getFooterContainer().addView(footerView);
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public int getHeaderViewCount(){
        int count = getHeaderContainer().getChildCount();
        if (count <= 0){
            footer_count = 0;
        }
        return header_count;
    }

    public int getFooterViewCount(){
        int count = getFooterContainer().getChildCount();
        if (count <= 0){
            footer_count = 0;
        }
        return footer_count;
    }

    private static final String TAG = RecyclerViewWrap.class.getSimpleName();
    private static final int STATUS_DEFAULT = 0;
    private static final int STATUS_SWIPING_TO_REFRESH = 1;
    private static final int STATUS_RELEASE_TO_REFRESH = 2;
    private static final int STATUS_REFRESHING = 3;
    private static final boolean DEBUG = false;
    private int mStatus;
    private boolean mIsAutoRefreshing;
    private boolean mRefreshEnabled;
    private boolean mLoadMoreEnabled;
    private int mRefreshFinalMoveOffset;
    private OnRefreshListener mOnRefreshListener;
    private OnLoadMoreListener mOnLoadMoreListener;
    private OnLoadMoreScrollListener mOnLoadMoreScrollListener;
    private RefreshHeaderLayout mRefreshHeaderContainer;
    private FrameLayout mLoadMoreFooterContainer;
    private LinearLayout mHeaderViewContainer;
    private LinearLayout mFooterViewContainer;
    private View mRefreshHeaderView;
    private View mLoadMoreFooterView;
    private int mActivePointerId;
    private int mLastTouchX;
    private int mLastTouchY;
    ValueAnimator mScrollAnimator;
    ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener;
    Animator.AnimatorListener mAnimationListener;
    RefreshTrigger mRefreshTrigger;

    public RecyclerViewWrap(Context context) {
        this(context, (AttributeSet)null);
    }

    public RecyclerViewWrap(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewWrap(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mActivePointerId = -1;
        this.mLastTouchX = 0;
        this.mLastTouchY = 0;
        this.mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = ((Integer)animation.getAnimatedValue()).intValue();
                RecyclerViewWrap.this.setRefreshHeaderContainerHeight(height);
                switch(RecyclerViewWrap.this.mStatus) {
                    case 1:
                        RecyclerViewWrap.this.mRefreshTrigger.onMove(false, true, height);
                        break;
                    case 2:
                        RecyclerViewWrap.this.mRefreshTrigger.onMove(false, true, height);
                        break;
                    case 3:
                        RecyclerViewWrap.this.mRefreshTrigger.onMove(true, true, height);
                }

            }
        };
        this.mAnimationListener = new SimpleAnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                int lastStatus = RecyclerViewWrap.this.mStatus;
                switch(RecyclerViewWrap.this.mStatus) {
                    case 1:
                        if(RecyclerViewWrap.this.mIsAutoRefreshing) {
                            RecyclerViewWrap.this.mRefreshHeaderContainer.getLayoutParams().height = RecyclerViewWrap.this.mRefreshHeaderView.getMeasuredHeight();
                            RecyclerViewWrap.this.mRefreshHeaderContainer.requestLayout();
                            RecyclerViewWrap.this.setStatus(3);
                            if(RecyclerViewWrap.this.mOnRefreshListener != null) {
                                RecyclerViewWrap.this.mOnRefreshListener.onRefresh();
                                RecyclerViewWrap.this.mRefreshTrigger.onRefresh();
                            }
                        } else {
                            RecyclerViewWrap.this.mRefreshHeaderContainer.getLayoutParams().height = 0;
                            RecyclerViewWrap.this.mRefreshHeaderContainer.requestLayout();
                            RecyclerViewWrap.this.setStatus(0);
                        }
                        break;
                    case 2:
                        RecyclerViewWrap.this.mRefreshHeaderContainer.getLayoutParams().height = RecyclerViewWrap.this.mRefreshHeaderView.getMeasuredHeight();
                        RecyclerViewWrap.this.mRefreshHeaderContainer.requestLayout();
                        RecyclerViewWrap.this.setStatus(3);
                        if(RecyclerViewWrap.this.mOnRefreshListener != null) {
                            RecyclerViewWrap.this.mOnRefreshListener.onRefresh();
                            RecyclerViewWrap.this.mRefreshTrigger.onRefresh();
                        }
                        break;
                    case 3:
                        RecyclerViewWrap.this.mIsAutoRefreshing = false;
                        RecyclerViewWrap.this.mRefreshHeaderContainer.getLayoutParams().height = 0;
                        RecyclerViewWrap.this.mRefreshHeaderContainer.requestLayout();
                        RecyclerViewWrap.this.setStatus(0);
                        RecyclerViewWrap.this.mRefreshTrigger.onReset();
                }

            }
        };
        this.mRefreshTrigger = new RefreshTrigger() {
            public void onStart(boolean automatic, int headerHeight, int finalHeight) {
                if(RecyclerViewWrap.this.mRefreshHeaderView != null && RecyclerViewWrap.this.mRefreshHeaderView instanceof RefreshTrigger) {
                    RefreshTrigger trigger = (RefreshTrigger)RecyclerViewWrap.this.mRefreshHeaderView;
                    trigger.onStart(automatic, headerHeight, finalHeight);
                }

            }

            public void onMove(boolean finished, boolean automatic, int moved) {
                if(RecyclerViewWrap.this.mRefreshHeaderView != null && RecyclerViewWrap.this.mRefreshHeaderView instanceof RefreshTrigger) {
                    RefreshTrigger trigger = (RefreshTrigger)RecyclerViewWrap.this.mRefreshHeaderView;
                    trigger.onMove(finished, automatic, moved);
                }

            }

            public void onRefresh() {
                if(RecyclerViewWrap.this.mRefreshHeaderView != null && RecyclerViewWrap.this.mRefreshHeaderView instanceof RefreshTrigger) {
                    RefreshTrigger trigger = (RefreshTrigger)RecyclerViewWrap.this.mRefreshHeaderView;
                    trigger.onRefresh();
                }

            }

            public void onRelease() {
                if(RecyclerViewWrap.this.mRefreshHeaderView != null && RecyclerViewWrap.this.mRefreshHeaderView instanceof RefreshTrigger) {
                    RefreshTrigger trigger = (RefreshTrigger)RecyclerViewWrap.this.mRefreshHeaderView;
                    trigger.onRelease();
                }

            }

            public void onComplete() {
                if(RecyclerViewWrap.this.mRefreshHeaderView != null && RecyclerViewWrap.this.mRefreshHeaderView instanceof RefreshTrigger) {
                    RefreshTrigger trigger = (RefreshTrigger)RecyclerViewWrap.this.mRefreshHeaderView;
                    trigger.onComplete();
                }

            }

            public void onReset() {
                if(RecyclerViewWrap.this.mRefreshHeaderView != null && RecyclerViewWrap.this.mRefreshHeaderView instanceof RefreshTrigger) {
                    RefreshTrigger trigger = (RefreshTrigger)RecyclerViewWrap.this.mRefreshHeaderView;
                    trigger.onReset();
                }

            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, com.aspsine.irecyclerview.R.styleable.IRecyclerView, defStyle, 0);
        boolean refreshHeaderLayoutRes = true;
        boolean loadMoreFooterLayoutRes = true;
        boolean refreshFinalMoveOffset = true;

        boolean refreshEnabled;
        boolean loadMoreEnabled;
        int refreshHeaderLayoutRes1;
        int loadMoreFooterLayoutRes1;
        int refreshFinalMoveOffset1;
        try {
            refreshEnabled = a.getBoolean(com.aspsine.irecyclerview.R.styleable.IRecyclerView_refreshEnabled, false);
            loadMoreEnabled = a.getBoolean(com.aspsine.irecyclerview.R.styleable.IRecyclerView_loadMoreEnabled, false);
            refreshHeaderLayoutRes1 = a.getResourceId(com.aspsine.irecyclerview.R.styleable.IRecyclerView_refreshHeaderLayout, -1);
            loadMoreFooterLayoutRes1 = a.getResourceId(com.aspsine.irecyclerview.R.styleable.IRecyclerView_loadMoreFooterLayout, -1);
            refreshFinalMoveOffset1 = a.getDimensionPixelOffset(com.aspsine.irecyclerview.R.styleable.IRecyclerView_refreshFinalMoveOffset, -1);
        } finally {
            a.recycle();
        }

        this.setRefreshEnabled(refreshEnabled);
        this.setLoadMoreEnabled(loadMoreEnabled);
        if(refreshHeaderLayoutRes1 != -1) {
            this.setRefreshHeaderView(refreshHeaderLayoutRes1);
        }

        if(loadMoreFooterLayoutRes1 != -1) {
            this.setLoadMoreFooterView(loadMoreFooterLayoutRes1);
        }

        if(refreshFinalMoveOffset1 != -1) {
            this.setRefreshFinalMoveOffset(refreshFinalMoveOffset1);
        }

        this.setStatus(0);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if(this.mRefreshHeaderView != null && this.mRefreshHeaderView.getMeasuredHeight() > this.mRefreshFinalMoveOffset) {
            this.mRefreshFinalMoveOffset = 0;
        }

    }

    public void setRefreshEnabled(boolean enabled) {
        this.mRefreshEnabled = enabled;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        this.mLoadMoreEnabled = enabled;
        if(this.mLoadMoreEnabled) {
            if(this.mOnLoadMoreScrollListener == null) {
                this.mOnLoadMoreScrollListener = new OnLoadMoreScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                        int visibleItemCount = layoutManager.getChildCount();
                        boolean triggerCondition = visibleItemCount > 0 && newState == 0 && this.canTriggerLoadMore(recyclerView);
                        if(triggerCondition) {
                            this.onLoadMore(recyclerView);
                        }

                    }

                    public boolean canTriggerLoadMore(RecyclerView recyclerView) {
                        View lastChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
                        int position = recyclerView.getChildLayoutPosition(lastChild);
                        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                        int totalItemCount = layoutManager.getItemCount();

                        if (getFooterViewCount() == 1){
                            if (lastChild instanceof ViewGroup && ((ViewGroup)lastChild).getChildCount() > 0){
                                if (((ViewGroup)lastChild).getChildAt(0) instanceof FooterStatusView){
                                    return totalItemCount - 2 == position || totalItemCount - 1 == position;
                                }
                            }
                        }
                        return totalItemCount - 1 == position;
                    }

                    public void onLoadMore(RecyclerView recyclerView) {
                        if(RecyclerViewWrap.this.mOnLoadMoreListener != null && RecyclerViewWrap.this.mStatus == 0) {
                            RecyclerViewWrap.this.mOnLoadMoreListener.onLoadMore(RecyclerViewWrap.this.mLoadMoreFooterView);
                        }

                    }
                };
            } else {
                this.removeOnScrollListener(this.mOnLoadMoreScrollListener);
            }

            this.addOnScrollListener(this.mOnLoadMoreScrollListener);
        } else if(this.mOnLoadMoreScrollListener != null) {
            this.removeOnScrollListener(this.mOnLoadMoreScrollListener);
        }

    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if(this.mStatus == 0 && refreshing) {
            this.mIsAutoRefreshing = true;
            this.setStatus(1);
            this.startScrollDefaultStatusToRefreshingStatus();
        } else if(this.mStatus == 3 && !refreshing) {
            this.mIsAutoRefreshing = false;
            this.startScrollRefreshingStatusToDefaultStatus();
        } else {
            this.mIsAutoRefreshing = false;
            Log.e(TAG, "isRefresh = " + refreshing + " current status = " + this.mStatus);
        }

    }

    public void setRefreshFinalMoveOffset(int refreshFinalMoveOffset) {
        this.mRefreshFinalMoveOffset = refreshFinalMoveOffset;
    }

    public void setRefreshHeaderView(View refreshHeaderView) {
        if(!this.isRefreshTrigger(refreshHeaderView)) {
            throw new ClassCastException("Refresh header view must be an implement of RefreshTrigger");
        } else {
            if(this.mRefreshHeaderView != null) {
                this.removeRefreshHeaderView();
            }

            if(this.mRefreshHeaderView != refreshHeaderView) {
                this.mRefreshHeaderView = refreshHeaderView;
                this.ensureRefreshHeaderContainer();
                this.mRefreshHeaderContainer.addView(refreshHeaderView);
            }

        }
    }

    public void setRefreshHeaderView(@LayoutRes int refreshHeaderLayoutRes) {
        this.ensureRefreshHeaderContainer();
        View refreshHeader = LayoutInflater.from(this.getContext()).inflate(refreshHeaderLayoutRes, this.mRefreshHeaderContainer, false);
        if(refreshHeader != null) {
            this.setRefreshHeaderView(refreshHeader);
        }

    }

    public void setLoadMoreFooterView(View loadMoreFooterView) {
        if(this.mLoadMoreFooterView != null) {
            this.removeLoadMoreFooterView();
        }

        if(this.mLoadMoreFooterView != loadMoreFooterView) {
            this.mLoadMoreFooterView = loadMoreFooterView;
            this.ensureLoadMoreFooterContainer();
            this.mLoadMoreFooterContainer.addView(loadMoreFooterView);
        }

    }

    public void setLoadMoreFooterView(@LayoutRes int loadMoreFooterLayoutRes) {
        this.ensureLoadMoreFooterContainer();
        View loadMoreFooter = LayoutInflater.from(this.getContext()).inflate(loadMoreFooterLayoutRes, this.mLoadMoreFooterContainer, false);
        if(loadMoreFooter != null) {
            this.setLoadMoreFooterView(loadMoreFooter);
        }

    }

    public View getRefreshHeaderView() {
        return this.mRefreshHeaderView;
    }

    public View getLoadMoreFooterView() {
        return this.mLoadMoreFooterView;
    }

    public LinearLayout getHeaderContainer() {
        this.ensureHeaderViewContainer();
        return this.mHeaderViewContainer;
    }

    public LinearLayout getFooterContainer() {
        this.ensureFooterViewContainer();
        return this.mFooterViewContainer;
    }

    public RecyclerView.Adapter getIAdapter() {
        WrapperAdapter wrapperAdapter = (WrapperAdapter)this.getAdapter();
        return wrapperAdapter.getAdapter();
    }

    public void setIAdapter(RecyclerView.Adapter adapter) {
        this.ensureRefreshHeaderContainer();
        this.ensureHeaderViewContainer();
        this.ensureFooterViewContainer();
        this.ensureLoadMoreFooterContainer();
        this.setAdapter(new WrapperAdapter(adapter, this.mRefreshHeaderContainer, this.mHeaderViewContainer, this.mFooterViewContainer, this.mLoadMoreFooterContainer));
    }

    private void ensureRefreshHeaderContainer() {
        if(this.mRefreshHeaderContainer == null) {
            this.mRefreshHeaderContainer = new RefreshHeaderLayout(this.getContext());
            this.mRefreshHeaderContainer.setLayoutParams(new RecyclerView.LayoutParams(-1, 0));
        }

    }

    private void ensureLoadMoreFooterContainer() {
        if(this.mLoadMoreFooterContainer == null) {
            this.mLoadMoreFooterContainer = new FrameLayout(this.getContext());
            this.mLoadMoreFooterContainer.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        }

    }

    private void ensureHeaderViewContainer() {
        if(this.mHeaderViewContainer == null) {
            this.mHeaderViewContainer = new LinearLayout(this.getContext());
            this.mHeaderViewContainer.setOrientation(LinearLayout.VERTICAL);
            this.mHeaderViewContainer.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        }

    }

    private void ensureFooterViewContainer() {
        if(this.mFooterViewContainer == null) {
            this.mFooterViewContainer = new LinearLayout(this.getContext());
            this.mFooterViewContainer.setOrientation(LinearLayout.VERTICAL);
            this.mFooterViewContainer.setLayoutParams(new RecyclerView.LayoutParams(-1, -2));
        }

    }

    private boolean isRefreshTrigger(View refreshHeaderView) {
        return refreshHeaderView instanceof RefreshTrigger;
    }

    private void removeRefreshHeaderView() {
        if(this.mRefreshHeaderContainer != null) {
            this.mRefreshHeaderContainer.removeView(this.mRefreshHeaderView);
        }

    }

    private void removeLoadMoreFooterView() {
        if(this.mLoadMoreFooterContainer != null) {
            this.mLoadMoreFooterContainer.removeView(this.mLoadMoreFooterView);
        }

    }

    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = MotionEventCompat.getActionMasked(e);
        int actionIndex = MotionEventCompat.getActionIndex(e);
        switch(action) {
            case 0:
                this.mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                this.mLastTouchX = (int)(MotionEventCompat.getX(e, actionIndex) + 0.5F);
                this.mLastTouchY = (int)(MotionEventCompat.getY(e, actionIndex) + 0.5F);
                break;
            case 5:
                this.mActivePointerId = MotionEventCompat.getPointerId(e, actionIndex);
                this.mLastTouchX = (int)(MotionEventCompat.getX(e, actionIndex) + 0.5F);
                this.mLastTouchY = (int)(MotionEventCompat.getY(e, actionIndex) + 0.5F);
                break;
            case 6:
                this.onPointerUp(e);
        }

        return super.onInterceptTouchEvent(e);
    }

    public boolean onTouchEvent(MotionEvent e) {
        int action = MotionEventCompat.getActionMasked(e);
        int index;
        switch(action) {
            case 0:
                index = MotionEventCompat.getActionIndex(e);
                this.mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                this.mLastTouchX = this.getMotionEventX(e, index);
                this.mLastTouchY = this.getMotionEventY(e, index);
                break;
            case 1:
                this.onFingerUpStartAnimating();
                break;
            case 2:
                index = MotionEventCompat.findPointerIndex(e, this.mActivePointerId);
                if(index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " + index + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                int x = this.getMotionEventX(e, index);
                int y = this.getMotionEventY(e, index);
                int var10000 = x - this.mLastTouchX;
                int dy = y - this.mLastTouchY;
                this.mLastTouchX = x;
                this.mLastTouchY = y;
                boolean triggerCondition = this.isEnabled() && this.mRefreshEnabled && this.mRefreshHeaderView != null && this.isFingerDragging() && this.canTriggerRefresh();
                if(!triggerCondition) {
                    break;
                }

                int refreshHeaderContainerHeight = this.mRefreshHeaderContainer.getMeasuredHeight();
                int refreshHeaderViewHeight = this.mRefreshHeaderView.getMeasuredHeight();
                if(dy > 0 && this.mStatus == 0) {
                    this.setStatus(1);
                    this.mRefreshTrigger.onStart(false, refreshHeaderViewHeight, this.mRefreshFinalMoveOffset);
                } else if(dy < 0) {
                    if(this.mStatus == 1 && refreshHeaderContainerHeight <= 0) {
                        this.setStatus(0);
                    }

                    if(this.mStatus == 0) {
                        break;
                    }
                }

                if(this.mStatus != 1 && this.mStatus != 2) {
                    break;
                }

                if(refreshHeaderContainerHeight >= refreshHeaderViewHeight) {
                    this.setStatus(2);
                } else {
                    this.setStatus(1);
                }

                this.fingerMove(dy);
                return true;
            case 3:
                this.onFingerUpStartAnimating();
            case 4:
            default:
                break;
            case 5:
                index = MotionEventCompat.getActionIndex(e);
                this.mActivePointerId = MotionEventCompat.getPointerId(e, index);
                this.mLastTouchX = this.getMotionEventX(e, index);
                this.mLastTouchY = this.getMotionEventY(e, index);
                break;
            case 6:
                this.onPointerUp(e);
        }

        return super.onTouchEvent(e);
    }

    private boolean isFingerDragging() {
        return this.getScrollState() == 1;
    }

    public boolean canTriggerRefresh() {
        RecyclerView.Adapter adapter = this.getAdapter();
        if(adapter != null && adapter.getItemCount() > 0) {
            View firstChild = this.getChildAt(0);
            int position = this.getChildLayoutPosition(firstChild);
            return position == 0 && firstChild.getTop() == this.mRefreshHeaderContainer.getTop();
        } else {
            return true;
        }
    }

    private int getMotionEventX(MotionEvent e, int pointerIndex) {
        return (int)(MotionEventCompat.getX(e, pointerIndex) + 0.5F);
    }

    private int getMotionEventY(MotionEvent e, int pointerIndex) {
        return (int)(MotionEventCompat.getY(e, pointerIndex) + 0.5F);
    }

    private void fingerMove(int dy) {
        int ratioDy = (int)((double)((float)dy * 0.5F) + 0.5D);
        int offset = this.mRefreshHeaderContainer.getMeasuredHeight();
        int finalDragOffset = this.mRefreshFinalMoveOffset;
        int nextOffset = offset + ratioDy;
        if(finalDragOffset > 0 && nextOffset > finalDragOffset) {
            ratioDy = finalDragOffset - offset;
        }

        if(nextOffset < 0) {
            ratioDy = -offset;
        }

        this.move(ratioDy);
    }

    private void move(int dy) {
        if(dy != 0) {
            int height = this.mRefreshHeaderContainer.getMeasuredHeight() + dy;
            this.setRefreshHeaderContainerHeight(height);
            this.mRefreshTrigger.onMove(false, false, height);
        }

    }

    private void setRefreshHeaderContainerHeight(int height) {
        this.mRefreshHeaderContainer.getLayoutParams().height = height;
        this.mRefreshHeaderContainer.requestLayout();
    }

    private void startScrollDefaultStatusToRefreshingStatus() {
        this.mRefreshTrigger.onStart(true, this.mRefreshHeaderView.getMeasuredHeight(), this.mRefreshFinalMoveOffset);
        int targetHeight = this.mRefreshHeaderView.getMeasuredHeight();
        int currentHeight = this.mRefreshHeaderContainer.getMeasuredHeight();
        this.startScrollAnimation(400, new AccelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollSwipingToRefreshStatusToDefaultStatus() {
        boolean targetHeight = false;
        int currentHeight = this.mRefreshHeaderContainer.getMeasuredHeight();
        this.startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, 0);
    }

    private void startScrollReleaseStatusToRefreshingStatus() {
        this.mRefreshTrigger.onRelease();
        int targetHeight = this.mRefreshHeaderView.getMeasuredHeight();
        int currentHeight = this.mRefreshHeaderContainer.getMeasuredHeight();
        this.startScrollAnimation(300, new DecelerateInterpolator(), currentHeight, targetHeight);
    }

    private void startScrollRefreshingStatusToDefaultStatus() {
        this.mRefreshTrigger.onComplete();
        boolean targetHeight = false;
        int currentHeight = this.mRefreshHeaderContainer.getMeasuredHeight();
        this.startScrollAnimation(400, new DecelerateInterpolator(), currentHeight, 0);
    }

    private void startScrollAnimation(int time, Interpolator interpolator, int value, int toValue) {
        if(this.mScrollAnimator == null) {
            this.mScrollAnimator = new ValueAnimator();
        }

        this.mScrollAnimator.removeAllUpdateListeners();
        this.mScrollAnimator.removeAllListeners();
        this.mScrollAnimator.cancel();
        this.mScrollAnimator.setIntValues(new int[]{value, toValue});
        this.mScrollAnimator.setDuration((long)time);
        this.mScrollAnimator.setInterpolator(interpolator);
        this.mScrollAnimator.addUpdateListener(this.mAnimatorUpdateListener);
        this.mScrollAnimator.addListener(this.mAnimationListener);
        this.mScrollAnimator.start();
    }

    private void onFingerUpStartAnimating() {
        if(this.mStatus == 2) {
            this.startScrollReleaseStatusToRefreshingStatus();
        } else if(this.mStatus == 1) {
            this.startScrollSwipingToRefreshStatusToDefaultStatus();
        }

    }

    private void onPointerUp(MotionEvent e) {
        int actionIndex = MotionEventCompat.getActionIndex(e);
        if(MotionEventCompat.getPointerId(e, actionIndex) == this.mActivePointerId) {
            int newIndex = actionIndex == 0?1:0;
            this.mActivePointerId = MotionEventCompat.getPointerId(e, newIndex);
            this.mLastTouchX = this.getMotionEventX(e, newIndex);
            this.mLastTouchY = this.getMotionEventY(e, newIndex);
        }

    }

    private void setStatus(int status) {
        this.mStatus = status;
    }

    private void printStatusLog() {
        Log.i(TAG, this.getStatusLog(this.mStatus));
    }

    private String getStatusLog(int status) {
        String statusLog;
        switch(status) {
            case 0:
                statusLog = "status_default";
                break;
            case 1:
                statusLog = "status_swiping_to_refresh";
                break;
            case 2:
                statusLog = "status_release_to_refresh";
                break;
            case 3:
                statusLog = "status_refreshing";
                break;
            default:
                statusLog = "status_illegal!";
        }

        return statusLog;
    }

}
