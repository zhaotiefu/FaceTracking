package com.zhaotf.facetracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class FaceRectView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private String mShowInfo = null;
    private int rectLength = 20;
    private ArrayList<Rect> faceRect;

    public FaceRectView(Context context) {
        super(context);
        initPaint(context);
    }

    public FaceRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initPaint(context);
    }

    public FaceRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context);
    }

    private void initPaint(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(Color.parseColor("#ff00ff"));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStrokeWidth(4);
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.GREEN);
    }

    public void drawFaceRect(Camera.Face[] faces, View mView, int cameraPosition) {
        faceRect = new ArrayList();
        for (Camera.Face face : faces) {
            Rect faceRect = new Rect(face.rect.left, face.rect.top, face.rect.right, face.rect.bottom);
            Rect dstRect = transForm(faceRect, mView.getWidth(), mView.getHeight(), (cameraPosition == Camera.CameraInfo.CAMERA_FACING_FRONT));
            this.faceRect.add(dstRect);
        }
        postInvalidate();
    }

    public void clearRect() {
        mShowInfo = null;
        if (faceRect != null) {
            faceRect.clear();
            faceRect = null;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faceRect != null && faceRect.size() > 0) {
            for (int i = 0; i < faceRect.size(); i++) {
                Rect rect = faceRect.get(i);
                int width = rect.right - rect.left;
                rectLength = (width) / 10;
                drawRect(rect, canvas);
            }
        }
    }

    private void drawRect(Rect rect, Canvas canvas) {
        if (rect != null) {
            canvas.drawLine(rect.left, rect.top, rect.left, rect.top + rectLength, mPaint);
            canvas.drawLine(rect.left, rect.top, rect.left + rectLength, rect.top, mPaint);

            canvas.drawLine(rect.right, rect.top, rect.right - rectLength, rect.top, mPaint);
            canvas.drawLine(rect.right, rect.top, rect.right, rect.top + rectLength, mPaint);
            canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - rectLength, mPaint);
            canvas.drawLine(rect.left, rect.bottom, rect.left + rectLength, rect.bottom, mPaint);

            canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - rectLength, mPaint);
            canvas.drawLine(rect.right, rect.bottom, rect.right - rectLength, rect.bottom, mPaint);

            if (mShowInfo != null) {
                canvas.drawText(mShowInfo, rect.left, rect.top - 10, mTextPaint);
            }
        }
    }

    public Rect transForm(Rect faceRect, int sfW, int sfH, boolean mirror) {
        Matrix matrix = new Matrix();
        matrix.setScale(1f, mirror ? -1f : 1f);
        matrix.postRotate(90f);//Camera Rotation
        matrix.postScale(sfW / 2000f, sfH / 2000f);
        matrix.postTranslate(sfW / 2f, sfH / 2f);

        RectF srcRect = new RectF(faceRect);
        RectF dstRect = new RectF(0f, 0f, 0f, 0f);
        matrix.mapRect(dstRect, srcRect);

        return new Rect((int) dstRect.left, (int) dstRect.top, (int) dstRect.right, (int) dstRect.bottom);
    }

}
