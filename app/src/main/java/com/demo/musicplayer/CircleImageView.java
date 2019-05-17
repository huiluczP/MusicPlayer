package com.demo.musicplayer;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class CircleImageView extends View {

    private Paint mPaint;
    private Drawable mBack;
    private Drawable mDrawable;//显示的图片
    private BitmapShader mBitmapShader;
    private int mWidth;
    private int mHeight;

    //获取src属性，初始化图片对象
    private void initAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = null;
            try {
                //获取src属性，设置显示图片
                array = getContext().obtainStyledAttributes(attrs, R.styleable.CircleImageView);
                mDrawable = array.getDrawable(R.styleable.CircleImageView_src);
                mBack= array.getDrawable(R.styleable.CircleImageView_back);
                if (mDrawable == null) {
                    throw new NullPointerException("drawable is not null");
                }
                //初始化宽高
                mWidth = mDrawable.getIntrinsicWidth();
                mHeight = mDrawable.getIntrinsicHeight();
            } finally {
                //使用完obtainStyledAttributes后进行回收
                if (array != null) {
                    array.recycle();
                }
            }
        }
    }

    public CircleImageView(Context context,AttributeSet attrs) {
        super(context, attrs);
        //初始化画笔对象
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//设置抗锯齿
        initAttrs(attrs);//初始化各项属性
    }

    @Override
    //根据定义的width和height类型来设置长宽
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量view尺寸，并根据宽高类型进行设置
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    //通过shader着色器进行圆形效果绘制
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //将图片对象转化为bitmap原图类型后传入调色器，画笔设置调色器
        mBitmapShader = new BitmapShader(drawableToBitmap(mDrawable), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(mBitmapShader);

        //使用原图的中心点作为新圆形中心，同时半径为原宽度一半
        canvas.drawCircle(width/2 , height/2 ,width/2 , mPaint);

        Paint p = new Paint();
        p.setAntiAlias(true);//取消锯齿
        p.setStyle(Paint.Style.STROKE);//设置画圆弧的画笔的属性为空心
        p.setStrokeWidth(5);
        p.setColor(Color.BLACK);
        RectF oval = new RectF( 5, (height-width)/2, width-5, (height+width)/2);
        canvas.drawArc(oval , 0 ,360,false , p);
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            //判断传入布局要求，若为实际显示类型，将其宽度设为当前真实大小
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                mWidth = widthSize;
                break;
        }
        return mWidth;
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //判断传入布局要求，若为实际显示类型，将其高度设为当前真实大小
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
            case MeasureSpec.EXACTLY:
                mHeight = heightSize;
                break;
        }
        return mHeight;
    }

    //工具方法，将图片对象转化为bit位图对象
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, mWidth, mHeight);
        drawable.draw(canvas);
        return bitmap;
    }

}
