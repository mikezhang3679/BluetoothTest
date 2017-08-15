package com.guangyao.bluetoothtest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.guangyao.bluetoothtest.bean.PointBean;

import java.util.List;

/**
 * Created by liuqiong on 2017/5/10.
 */

public class PathView2 extends View {
    private int width;
    private int height;
    private Paint paint;
    private float mul_y = -0.45f;//纵坐标放大倍数
    private float plus_y = -80;//纵坐标向上移


    private List<PointBean> pointList;
    private List<PointBean> pointList2;
    private Canvas canvas;

    public PathView2(Context context) {
        super(context);
    }

    public PathView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();

    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        CornerPathEffect cornerPathEffect = new CornerPathEffect(10);
        paint.setPathEffect(cornerPathEffect);

    }

    public PathView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas=canvas;
        Log.i("zgy", "invalidate");
        canvas.translate(0, height);//将坐标系移到屏幕左下角



//todo  画第一个集合
        if (pointList != null) {
            Path path = new Path();
            for (int i = 0; i < pointList.size(); i++) {
                path.lineTo(pointList.get(i).getX(), (float) (pointList.get(i).getY()) * mul_y + plus_y);
            }
            canvas.drawPath(path, paint);
        }

//todo 画第二个集合
        if (pointList2 != null) {
            Path path = new Path();
            //将下次轨迹起始点移到集合第一个点
            if (pointList2.size()>0){
            int x = pointList2.get(0).getX();
            int y = pointList2.get(0).getY();
            path.moveTo(x, y * mul_y + plus_y);

            }

            for (int i = 0; i < pointList2.size(); i++) {
                path.lineTo(pointList2.get(i).getX(), pointList2.get(i).getY() * mul_y + plus_y);//
            }
            canvas.drawPath(path, paint);

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

    }


    public void setData(List<PointBean> pointList, List<PointBean> pointList2) {
        this.pointList = pointList;
        this.pointList2 = pointList2;

        Log.d("test", "第一个集合:" + pointList.size());
        Log.d("test", "第二个集合:" + pointList2.size());
        invalidate();
    }

}
