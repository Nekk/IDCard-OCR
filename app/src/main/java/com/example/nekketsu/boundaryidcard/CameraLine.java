package com.example.nekketsu.boundaryidcard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.opencv.android.JavaCameraView;

class CameraLine extends JavaCameraView {
    private Paint linePaint;
    private Context context;
    private boolean overlapTopLeft, overlapTopRight, overlapBottomLeft, overlapBottomRight;
    private final int startUpx = 300, startUpy = 482, stopUpx = 300, stopUpy = 10;
    private final int startLeftx = 300, startLefty = 482, stopLeftx = 607, stopLefty = 482;
    private final int startRightx = 300, startRighty = 10, stopRightx = 607, stopRighty = 10;
    private final int startDownx = 607, startDowny = 482, stopDownx = 607, stopDowny = 10;

    public CameraLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        setWillNotDraw(false);
    }

    protected void init() {
        Resources r = this.getResources();
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAlpha(200);
        linePaint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        linePaint.setColor(Color.GREEN);

//            System.out.println(canvas.getWidth());//912
//            System.out.println(canvas.getHeight());//492

        //top-left
        canvas.drawLine(300, 482, 360, 482, linePaint);
        canvas.drawLine(300, 482, 300, 422, linePaint);

        //top-right
        canvas.drawLine(300, 10, 360, 10, linePaint);//line-60
        canvas.drawLine(300, 10, 300, 70, linePaint);

        //bottom-left
        canvas.drawLine(607, 482, 547, 482, linePaint);
        canvas.drawLine(607, 482, 607, 422, linePaint);

        //bottom-right
        canvas.drawLine(607, 10, 547, 10, linePaint);
        canvas.drawLine(607, 10, 607, 70, linePaint);

        if (overlapTopLeft && overlapTopRight) {
            linePaint.setAlpha(200);
            canvas.drawLine(startUpx, startUpy, stopUpx, stopUpy, linePaint);
        } else {
            linePaint.setAlpha(10);
            canvas.drawLine(startUpx, startUpy, stopUpx, stopUpy, linePaint);
        }

        if (overlapTopLeft && overlapBottomLeft) {
            linePaint.setAlpha(200);
            canvas.drawLine(startLeftx, startLefty, stopLeftx, stopLefty, linePaint);
        } else {
            linePaint.setAlpha(10);
            canvas.drawLine(startLeftx, startLefty, stopLeftx, stopLefty, linePaint);
        }

        if (overlapTopRight && overlapBottomRight) {
            linePaint.setAlpha(200);
            canvas.drawLine(startRightx, startRighty, stopRightx, stopRighty, linePaint);
        } else {
            linePaint.setAlpha(10);
            canvas.drawLine(startRightx, startRighty, stopRightx, stopRighty, linePaint);
        }

        if (overlapBottomRight && overlapBottomLeft) {
            linePaint.setAlpha(200);
            canvas.drawLine(startDownx, startDowny, stopDownx, stopDowny, linePaint);
        } else {
            linePaint.setAlpha(10);
            canvas.drawLine(startDownx, startDowny, stopDownx, stopDowny, linePaint);
        }

    }

    public void update(boolean overTL, boolean overTR, boolean overBL, boolean overBR) {
        overlapTopLeft = overTL;
        overlapTopRight = overTR;
        overlapBottomLeft = overBL;
        overlapBottomRight = overBR;

        postInvalidate();
    }
}
