package com.channer.ear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by channerduan on 5/5/16.
 */
public class PhotoFrameView extends View {

    private List<Rect> mRects = new ArrayList<>();

    public PhotoFrameView(Context context) {
        super(context);
    }

    public void clear() {
        mRects.clear();
    }

    public void addFrame(Rect rect) {
        mRects.add(rect);
    }

    public void addFrames(List<Rect> rectList) {
        mRects.addAll(rectList);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(30);

        String s = "FacePreview - This side up.";
        float textWidth = paint.measureText(s);
        canvas.drawText(s, (getWidth() - textWidth) / 2, 20, paint);

        if (mRects != null) {
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            for (Rect rect : mRects) {
                canvas.drawRect(rect.s_x, rect.s_y,
                        rect.s_x + rect.w, rect.s_y + rect.h, paint);
            }
        }
    }

}
