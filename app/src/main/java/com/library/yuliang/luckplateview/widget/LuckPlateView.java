package com.library.yuliang.luckplateview.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.library.yuliang.luckplateview.R;

/**
 * 抽奖盘
 *
 * 绘制步骤
 * 1、设定正方形的宽高。
 * 2、绘制背景。
 * 3、绘制抽奖区域。
 * 4、绘制文字。
 * 5、绘制图片。
 *
 * 先绘制出所有轮廓，后续可自定义的属性再抽取出来，这样可以快速测试，也可以知道哪些属性可以自定义。
 * 可自定义的属性包含：边框的圈圈颜色；每个区域的背景色、图片、文字大小、文字颜色
 * 盘快区域数量暂不能自定义，固定为6个
 */
public class LuckPlateView extends View {

    private int mWidth;
    /**
     * 圆盘半径
     */
    private int radius;

    /**
     * 绘制区域
     */
    private RectF drawRect;

    /**
     * 中心点的坐标位置
     */
    private int centerX,centerY;
    private Paint bgPaint,imgPaint,textPaint;

    /**
     *  一些自定义的属性：外环颜色；文字颜色；文字大小
     */
    private int outerBorderColor,textColor;
    private float textSize;
    private ValueAnimator rotateAnimator;
    /**
     * 目标角度：旋转到的角度，即要制定的位置
     */
    private float destAngle=360;

    /**
     * 每个区域的角度
     */
    private float singleZoneAngle;


    /**
     * 开始绘制的角度，默认为0；它来控制动画
     */
    private float startAngle=0;

    private String[] texts={"iPad","谢谢参与","4K高清电视","iPad","谢谢参与","4K高清电视"};
    private int[] colors={Color.YELLOW,Color.GRAY,Color.BLUE,Color.GREEN,Color.CYAN,Color.MAGENTA};
    private int imgIds[]={R.drawable.ic_lunk1,R.drawable.ic_lunk2,R.drawable.ic_lunk1,
            R.drawable.ic_lunk2,R.drawable.ic_lunk1,R.drawable.ic_lunk2};
    private Bitmap[] bitmaps=new Bitmap[imgIds.length];


    public LuckPlateView(Context context) {
        this(context,null);
    }

    public LuckPlateView(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }



    public LuckPlateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array=context.obtainStyledAttributes(attrs,R.styleable.LuckPlateView);
        outerBorderColor =array.getColor(R.styleable.LuckPlateView_outerBorderColor,Color.RED);
        textColor=array.getColor(R.styleable.LuckPlateView_textColor,Color.WHITE);
        textSize=array.getDimension(R.styleable.LuckPlateView_textSize,dp2px(14));
        array.recycle();
        initPaint();
        initBitmaps();
        initAnimator();
    }

    private void initAnimator() {
        rotateAnimator=ValueAnimator.ofFloat(startAngle,destAngle);
        rotateAnimator.setDuration(3000);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle= (float) animation.getAnimatedValue();
                startAngle=angle;
                invalidate();
            }
        });
    }


    /**
     * 初始化画笔
     */
    private void initPaint() {
        bgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(outerBorderColor);       //可自定义
        bgPaint.setStrokeWidth(dp2px(5));
        bgPaint.setStyle(Paint.Style.STROKE);
        imgPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStrokeWidth(dp2px(1));
        textPaint.setTextSize(textSize);   //可自定义
        textPaint.setColor(textColor);  //可自定义
    }


    /**
     * 获取轮盘区域图片
     */
    private void initBitmaps() {
        //图片优化从三个方面进行：存储、内存、
        for (int i=0;i<imgIds.length;i++){
            bitmaps[i]=BitmapFactory.decodeResource(getResources(),imgIds[i]);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=Math.min(w,h);
        float strokeWidth= bgPaint.getStrokeWidth();
        radius= (int) ((mWidth-getPaddingLeft()-strokeWidth)/2);
        singleZoneAngle=360/imgIds.length;
        centerX=mWidth/2;
        centerY=centerX;
        drawRect=new RectF(getPaddingLeft()+strokeWidth,getPaddingTop()+strokeWidth,
                mWidth-getPaddingRight()-strokeWidth,mWidth-getPaddingBottom()-strokeWidth);
        setMeasuredDimension(mWidth,mWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //绘制背景
        drawBackground(canvas);
        //绘制其他
        float tmpAngle=startAngle;
        for (int i=0;i<imgIds.length;i++){
            //绘制单个扇形区域颜色
            drawSingleZoneBg(canvas,tmpAngle,i);
            //绘制单个扇形文字
            drawSingleZoneText(canvas,tmpAngle,i);
            //绘制单个扇形图片
            drawSingleZoneImg(canvas,tmpAngle,i);
            if (tmpAngle>360){
                tmpAngle=(tmpAngle+singleZoneAngle)%360;
            }else {
                tmpAngle=tmpAngle+singleZoneAngle;
            }
            Log.e("test","开始的tmpAngle="+tmpAngle);
        }
    }




    /**
     * 绘制区域背景
     * @param tmpAngle
     * @param zoneIndex
     */
    private void drawSingleZoneBg(Canvas canvas,float tmpAngle, int zoneIndex) {
        imgPaint.setColor(colors[zoneIndex]);
        Log.e("test","zoneIndex="+zoneIndex);
        Log.e("test","startAngle="+startAngle);
        canvas.drawArc(drawRect,tmpAngle,singleZoneAngle,true,imgPaint);
    }


    /**
     * 绘制区域文字
     * @param tmpAngle
     * @param zoneIndex
     */
    private void drawSingleZoneText(Canvas canvas,float tmpAngle, int zoneIndex) {
        Path path=new Path();
        path.addArc(drawRect,tmpAngle,singleZoneAngle);
        float textWidth=textPaint.measureText(texts[zoneIndex]);
        float hOffset= (2*radius*(float)Math.PI/(float)imgIds.length);
        canvas.drawTextOnPath(texts[zoneIndex],path,(hOffset-textWidth)/2,dp2px(25),textPaint);
    }

    /**
     * 绘制区域图片
     * @param tmpAngle
     * @param zoneIndex
     */
    private void drawSingleZoneImg(Canvas canvas,float tmpAngle, int zoneIndex) {
        int imgWidth=radius/3;
        float angle= (float) ((tmpAngle+singleZoneAngle/2)*Math.PI/180);
        float imgCenterX= centerX+(float) (Math.cos(angle)*radius*4/7);
        float imgCenterY= centerY+(float) (Math.sin(angle)*radius*4/7);
        canvas.drawBitmap(bitmaps[zoneIndex],null,new RectF(imgCenterX-imgWidth/2,imgCenterY-imgWidth/2,
                imgCenterX+imgWidth/2,imgCenterY+imgWidth/2),null);
    }

    /**
     * 绘制背景及外面圈圈
     */
    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.WHITE);         //可自定义
        canvas.drawCircle(centerX,centerY,radius,bgPaint);
    }


    /**
     * 开始转动
     * @param index 要转到的指定位置
     */
    public void startRotate(int index){
        if (rotateAnimator.isRunning()||rotateAnimator.isStarted()){
            return;
        }
        startAngle=startAngle%360;
        float minAngle=(3-index)*singleZoneAngle+singleZoneAngle/2+8;
        float maxAngle=(4-index)*singleZoneAngle+singleZoneAngle/2-8;

        //生成随机数 Math.random()表示生成[0,1]之间的任意小数
        //生成任意非从0开始的小数区间[d1,d2)范围的随机数字(其中d1不等于0)，
        // 则只需要首先生成[0,d2-d1)区间的随机数字，然后将生成的随机数字区间加上d1即可。r.nextDouble() * (d2-d1) + 1;
        destAngle= (float) (Math.random()*(maxAngle-minAngle)+minAngle)+360*2;
        initAnimator();
        rotateAnimator.start();
    }


    public void setTexts(String... texts) {
        this.texts = texts;
    }

    public void setColors(int... colors) {
        this.colors = colors;
    }

    public void setImgIds(int... imgIds) {
        this.imgIds = imgIds;
        initBitmaps();
    }

    public void setBitmaps(Bitmap... bitmaps) {
        this.bitmaps = bitmaps;
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }


}
