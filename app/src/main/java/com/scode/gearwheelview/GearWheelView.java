package com.scode.gearwheelview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by ZWX on 2019/4/23.
 */
//自定义齿轮View
public class GearWheelView extends View {
    float Base_R = dp2px(20);//dp   绘制齿轮基准半径
    Paint insiderRingPaint;//内部环形绘制画笔
    Paint outsiderRingPaint;//外部环形绘制画笔
    Paint gearPaint;//齿轮绘制画笔
    int gearColor = 0xccFFD08C;//齿轮颜色
    int gearOutCount = 8;//齿轮外部的齿合个数
    float gearOutBottomRatio = 0.8f;//齿合的底部占已分大小的比例
    float gearOutTopRatio = 0.4f;//齿合的顶部占已分大小的比例
    float insideRingWidthRatio = 1F;//内环的宽度
    float outsideRingWidthRatio = 1F;//外环的宽度
    float gearWidthRatio = 0.8F;//外部齿合的高度
    Point gearCenter;//齿轮中心
    float startAngle = 0;//齿轮的开始角度
    int animCircleTime = 1000;//动画一周期时长
    boolean isClockWise = true;//是否为顺时针

    Context mContext;

    public GearWheelView(Context context) {
        super(context);
        initView(context);
    }

    public GearWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttr(attrs, context);
        initView(context);
    }

    public GearWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttr(attrs, context);
        initView(context);
    }


    private void getAttr(AttributeSet attrs, Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GearWheelView);
        Base_R = typedArray.getDimensionPixelOffset(R.styleable.GearWheelView_GearWheelViewBaseR, dp2px(20));
        animCircleTime = typedArray.getInt(R.styleable.GearWheelView_GearWheelViewCycleTime, animCircleTime);
        isClockWise = typedArray.getBoolean(R.styleable.GearWheelView_GearWheelViewClockWise, true);
        typedArray.recycle();
    }

    private void initView(Context context) {
        mContext = context;
        gearPaint = new Paint();
        gearPaint.setAntiAlias(true);
        gearPaint.setDither(true);
        gearPaint.setStyle(Paint.Style.FILL);
        gearPaint.setColor(gearColor);
        gearPaint.setStrokeJoin(Paint.Join.ROUND);

        insiderRingPaint = new Paint();
        insiderRingPaint.setAntiAlias(true);
        insiderRingPaint.setDither(true);
        insiderRingPaint.setStrokeWidth(insideRingWidthRatio*Base_R);
        insiderRingPaint.setStyle(Paint.Style.STROKE);
        insiderRingPaint.setColor(gearColor);

        outsiderRingPaint = new Paint();
        outsiderRingPaint.setAntiAlias(true);
        outsiderRingPaint.setDither(true);
        outsiderRingPaint.setStrokeWidth(outsideRingWidthRatio*Base_R);
        outsiderRingPaint.setStyle(Paint.Style.STROKE);
        outsiderRingPaint.setColor(gearColor);

        startAnim();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawInsideRing(canvas);
        drawOutsideRing(canvas);
        drawGear(canvas);
    }


    private void startAnim() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, isClockWise?360:-360);
        animator.setDuration(animCircleTime);
        animator.setRepeatCount(Integer.MAX_VALUE);//表示无限
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        animator.start();
    }


    //画内环
    private void drawInsideRing(Canvas canvas) {
        canvas.drawCircle(gearCenter.x, gearCenter.y, 3 * Base_R / 2, insiderRingPaint);
    }

    //画外环
    private void drawOutsideRing(Canvas canvas) {
        canvas.drawCircle(gearCenter.x, gearCenter.y, 3 * Base_R, outsiderRingPaint);
    }

    //画外部齿合
    private void drawGear(Canvas canvas) {
        Path path = initGearPath();
        canvas.drawPath(path, gearPaint);
    }

    private Path initGearPath() {
        float insideR = (3 * Base_R + outsideRingWidthRatio * Base_R / 2);
        float outsideR = (insideR + Base_R * gearWidthRatio);

        Path path = new Path();
        Path insidePath = new Path();
        Path outPath = new Path();
        RectF insideRectF = new RectF(gearCenter.x - insideR, gearCenter.y - insideR, gearCenter.x + insideR, gearCenter.y + insideR);
        insidePath.addArc(insideRectF, startAngle, 359.9F);

        RectF outSideRectF = new RectF(gearCenter.x - outsideR, gearCenter.y - outsideR, gearCenter.x + outsideR, gearCenter.y + outsideR);
        outPath.addArc(outSideRectF, startAngle, 359.9F);

        PathMeasure insidePathMeasure = new PathMeasure(insidePath, true);
        PathMeasure outsidePathMeasure = new PathMeasure(outPath, true);
        float insideItemLength = insidePathMeasure.getLength() / gearOutCount;
        float outsideItemLength = outsidePathMeasure.getLength() / gearOutCount;
        float insideD = (1 - gearOutBottomRatio) / 2;//获取内部差值
        float outsideD = (1 - gearOutTopRatio) / 2;//获取外部差值

        for (int i = 0; i < gearOutCount; i++) {
            Path itemPath = new Path();
            float[] pos = new float[2];
            insidePathMeasure.getPosTan(i * insideItemLength + insideD * insideItemLength, pos, null);//获取点信息
            itemPath.moveTo(pos[0], pos[1]);
            outsidePathMeasure.getPosTan(i * outsideItemLength + outsideD * outsideItemLength, pos, null);//获取点信息
            itemPath.lineTo(pos[0], pos[1]);

            outsidePathMeasure.getPosTan((i + 1) * outsideItemLength - outsideD * outsideItemLength, pos, null);//获取点信息
            itemPath.lineTo(pos[0], pos[1]);

            insidePathMeasure.getPosTan((i + 1) * insideItemLength - insideD * insideItemLength, pos, null);//获取点信息
            itemPath.lineTo(pos[0], pos[1]);
            itemPath.close();
            path.addPath(itemPath);
        }
        return path;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gearCenter = new Point(w / 2, h / 2);
    }

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }
}
