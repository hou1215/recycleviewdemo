package com.hx.recyclerviewdemo.recyclerview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aspsine.irecyclerview.RefreshTrigger;
import com.hx.recyclerviewdemo.R;
import com.hx.recyclerviewdemo.utils.AnimationUtil;

/**
 * Created by aspsine on 16/4/7.
 */
public class HeaderRefreshView extends FrameLayout implements RefreshTrigger {

    protected ImageView img_refresh_logo;

    protected TextView tv_refresh_hint;
    protected TextView tv_refresh_title;

    protected int mHeight;
    protected int progress;

    //    protected Animation animation;
    protected boolean refresh = false;
    private ObjectAnimator rotationAnimation;
    private CircleView circleView;

    public HeaderRefreshView(Context context) {
        this(context, null);
    }

    public HeaderRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        View view = LayoutInflater.from(context).inflate( R.layout.layout_refresh_header, null);
        this.addView(view);

        img_refresh_logo = (ImageView) view.findViewById(R.id.img_refresh_logo);
        circleView = (CircleView) view.findViewById(R.id.img_refresh_progress);
        tv_refresh_hint = (TextView) view.findViewById(R.id.tv_refresh_hint);
        tv_refresh_title = (TextView) view.findViewById(R.id.tv_refresh_title);

    }

    @Override
    public void onStart(boolean automatic, int headerHeight, int finalHeight) {
        mHeight = headerHeight;
        progress = 90;
    }

    @Override
    public void onMove(boolean finished, boolean automatic, int moved) {
        if (!finished) {
            int p = (int) (moved / (float) mHeight * 360);

            if (!refresh && p <= 340) {
                img_refresh_logo.setVisibility(VISIBLE);
                circleView.setProgress(p);
                tv_refresh_hint.setText("下拉刷新");
            } else {
                tv_refresh_hint.setText("松开刷新");
                img_refresh_logo.setVisibility(GONE);
            }

        }

    }

    @Override
    public void onRefresh() {

        if (rotationAnimation == null) {
            rotationAnimation = AnimationUtil.getRoteSelfObjectAnimation(circleView, 1000, -1);
        }

        startAnim();
        tv_refresh_hint.setText("正在刷新");
    }

    private void startAnim() {

        if (rotationAnimation != null) {
            rotationAnimation.start();
            refresh = true;
        }
    }

    @Override
    public void onRelease() {

    }

    @Override
    public void onComplete() {
        refresh = false;
        if (rotationAnimation != null){
            rotationAnimation.cancel();
        }
    }

    @Override
    public void onReset() {
        circleView.setProgress(progress);
        img_refresh_logo.setVisibility(VISIBLE);
        tv_refresh_hint.setText("下拉刷新");
    }

    public ImageView getImg_refresh_logo() {
        return img_refresh_logo;
    }

    public TextView getTv_refresh_hint() {
        return tv_refresh_hint;
    }

    public CircleView getCircleView() {
        return circleView;
    }

    public int getmHeight() {
        return mHeight;
    }

    public int getProgress() {
        return progress;
    }

    public TextView getTv_refresh_title() {
        return tv_refresh_title;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public ObjectAnimator getRotationAnimation() {
        return rotationAnimation;
    }

    public void setRotationAnimation(ObjectAnimator rotationAnimation) {
        this.rotationAnimation = rotationAnimation;
    }

}
