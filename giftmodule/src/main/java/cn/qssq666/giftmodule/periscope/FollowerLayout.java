package cn.qssq666.giftmodule.periscope;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;


/**
 * Created by yifeiyuan on 15/6/19.
 * <p>
 * http://www.jianshu.com/p/03fdcfd3ae9c
 */
public class FollowerLayout extends RelativeLayout {

    private static final String TAG = "FollowerLayout";

    private Interpolator line = new LinearInterpolator();//线性
    private Interpolator acc = new AccelerateInterpolator();//加速
    private Interpolator dce = new DecelerateInterpolator();//减速
    private Interpolator accdec = new AccelerateDecelerateInterpolator();//先加速后减速
    private Interpolator[] interpolators;

    private int mHeight;
    private int mWidth;

    private LayoutParams lp;

    public FollowerLayout(Context context) {
        super(context);
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        init(context);
    }

    public FollowerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        init(context);
    }


    private Random random = new Random();

    private int dHeight;
    private int dWidth;

    private void init(Context context) {
//         Drawable red ;
//        //初始化显示的图片
//        red = ContextCompat.getDrawable(context,R.drawable.img_redsmall);

//        red = getResources().getDrawable(R.drawable.red);
//        yellow = getResources().getDrawable(R.drawable.yellow);
//        blue = getResources().getDrawable(R.drawable.blue);

//        drawables[3] = ContextCompat.getDrawable(context,R.drawable.ico_flower_purple);
        //获取图的宽高 用于后面的计算
        //注意 我这里3张图片的大小都是一样的,所以我只取了一个
//        dHeight = red.getIntrinsicHeight();
//        dWidth = red.getIntrinsicWidth();

//        //底部 并且 水平居中
//        lp = new LayoutParams(dWidth, dHeight);
//        lp.addRule(CENTER_HORIZONTAL, TRUE);//这里的TRUE 要注意 不是true
//        lp.addRule(ALIGN_PARENT_BOTTOM, TRUE);

        // 初始化插补器
        interpolators = new Interpolator[4];
        interpolators[0] = line;
        interpolators[1] = acc;
        interpolators[2] = dce;
        interpolators[3] = accdec;

    }

    public LayoutParams getContentLayoutParam() {
        return lp;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取本身的宽高 这里要注意,测量之后才有宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        Log.i(TAG, "mWidth" + mWidth + ",mHeight:" + mHeight);
        if (mHeight <= 0) {
            mHeight = 250;
            Log.e(TAG, "无法获取高度,你是否设置了底部居中参数");
        }
        if (mWidth <= 0) {
            mWidth = 250;
            Log.e(TAG, "无法获取宽度,你是否设置了某些居中对齐的参数,请用代码实现!");
        }
    }


    public Pair<ImageView, Animator> getImageViewPair() {
        ImageView imageView = new ImageView(getContext());
        //随机选一个
        imageView.setLayoutParams(lp);
        addView(imageView, 0);
        Log.v(TAG, "add后子view数:" + getChildCount());
        Animator set = getAnimator(imageView);
        set.addListener(new AnimEndListener(imageView));
        return Pair.create(imageView, set);
    }

    private Animator getAnimator(View target) {
        AnimatorSet set = getEnterAnimtor(target);

        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playSequentially(set);
        finalSet.playSequentially(set, bezierValueAnimator);
        finalSet.setInterpolator(interpolators[random.nextInt(4)]);
        finalSet.setTarget(target);
        return finalSet;
    }

    private AnimatorSet getEnterAnimtor(final View target) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, View.ALPHA, 0.2f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.1f, 1f);
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(500);
        enter.setInterpolator(new LinearInterpolator());
        enter.playTogether(alpha, scaleX, scaleY);
        enter.setTarget(target);
        return enter;
    }

    private ValueAnimator getBezierValueAnimator(View target) {

        //初始化一个贝塞尔计算器- - 传入
        BezierEvaluator evaluator = new BezierEvaluator(getPointF(2), getPointF(1));

        //这里最好画个图 理解一下 传入了起点 和 终点
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, new PointF((mWidth - dWidth) / 2, mHeight - dHeight), new PointF(random.nextInt(getWidth()), 0));
        animator.addUpdateListener(new BezierListenr(target));
        animator.setTarget(target);
        animator.setDuration(3000);
        return animator;
    }

    /**
     * 获取中间的两个 点
     *
     * @param scale
     */
    private PointF getPointF(int scale) {

        PointF pointF = new PointF();
        pointF.x = random.nextInt((mWidth - 100));//减去100 是为了控制 x轴活动范围,看效果 随意~~
        //再Y轴上 为了确保第二个点 在第一个点之上,我把Y分成了上下两半 这样动画效果好一些  也可以用其他方法
        pointF.y = random.nextInt((mHeight - 100)) / scale;
        return pointF;
    }

    private class BezierListenr implements ValueAnimator.AnimatorUpdateListener {

        private View target;

        public BezierListenr(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //这里获取到贝塞尔曲线计算出来的的x y值 赋值给view 这样就能让爱心随着曲线走啦
            PointF pointF = (PointF) animation.getAnimatedValue();
            target.setX(pointF.x);
            target.setY(pointF.y);
            // 这里顺便做一个alpha动画
            target.setAlpha(1 - animation.getAnimatedFraction());
        }
    }


    private class AnimEndListener extends AnimatorListenerAdapter {
        private View target;

        public AnimEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            //因为不停的add 导致子view数量只增不减,所以在view动画结束后remove掉
            removeView((target));
            Log.v(TAG, "removeView后子view数:" + getChildCount());
        }
    }
}
