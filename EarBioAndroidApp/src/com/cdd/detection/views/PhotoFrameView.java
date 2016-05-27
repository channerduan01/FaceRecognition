package com.cdd.detection.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.cdd.detection.detect.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by channerduan on 5/5/16.
 */
public class PhotoFrameView extends View {

    private List<Rect> mRects = new ArrayList<>();

    private int imageWidth;
    private int imageHeight;

    public PhotoFrameView(Context context) {
        super(context);
    }

    public void clear() {
        mRects.clear();
    }

    public void addFrame(Rect rect) {
        mRects.add(rect);
    }

    public void update(List<Rect> rectList, int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
        clear();
        mRects.addAll(rectList);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;

        Paint paint1 = new Paint();
        paint1.setColor(Color.GREEN);
        paint1.setTextSize(30);
        Paint paint2 = new Paint();
        paint2.setColor(Color.GRAY);

        String text = "This side up!";

        switch (mRects.size()) {
            case 0:
                text += "\nNo target detected";
                break;
            case 1:
                break;
            default:
                text += "\n" + mRects.size() + " targets detected!\nOnly focus the green one!";
        }

        float textWidth = paint1.measureText(text);
        canvas.drawText(text, (getWidth() - textWidth) / 2, 40, paint1);

        if (mRects != null) {
            paint1.setStrokeWidth(4);
            paint1.setStyle(Paint.Style.STROKE);
            paint2.setStrokeWidth(4);
            paint2.setStyle(Paint.Style.STROKE);
            Rect rect;
            for (int i = 0; i < mRects.size(); i++) {
                rect = mRects.get(i);
                if (i == 0) {
                    canvas.drawRect(rect.s_x * scaleX, rect.s_y * scaleY,
                            (rect.s_x + rect.w) * scaleX, (rect.s_y + rect.h) * scaleY, paint1);
                } else {
                    canvas.drawRect(rect.s_x * scaleX, rect.s_y * scaleY,
                            (rect.s_x + rect.w) * scaleX, (rect.s_y + rect.h) * scaleY, paint2);
                }
            }
        }
    }
}
