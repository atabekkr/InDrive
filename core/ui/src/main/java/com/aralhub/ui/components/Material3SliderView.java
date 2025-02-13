package com.aralhub.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class Material3SliderView extends View {
    private static final int ANIMATION_DURATION = 200;
    private static final int DEFAULT_HEIGHT_DP = 48;
    private static final int TRACK_HEIGHT_DP = 16;
    private static final int THUMB_SIZE_DP = 20;
    private static final int HALO_SIZE_DP = 40;

    private Paint trackInactivePaint;
    private Paint trackActivePaint;
    private Paint thumbPaint;
    private Paint haloPaint;
    private Paint labelPaint;
    private Paint tickPaint;

    private float minValue = 1f;
    private float maxValue = 5f;
    private float stepSize = 0.5f;
    private float currentValue = 1f;
    private float thumbX;
    private float trackY;
    private boolean isDragging = false;
    private ValueAnimator pressedAnimator;
    private float haloScale = 0f;
    private RectF trackRect = new RectF();
    private OnValueChangeListener listener;
    private List<Float> validValues;

    private final float density;
    private final float thumbRadius;
    private final float haloRadius;
    private final float trackHeight;
    private final float tickRadius;

    public interface OnValueChangeListener {
        void onValueChanged(float value);
    }

    public Material3SliderView(Context context) {
        this(context, null);
    }

    public Material3SliderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Material3SliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        thumbRadius = THUMB_SIZE_DP * density / 2;
        haloRadius = HALO_SIZE_DP * density / 2;
        trackHeight = TRACK_HEIGHT_DP * density;
        tickRadius = 2 * density;
        initValidValues();
        init();
    }

    private void initValidValues() {
        validValues = new ArrayList<>();
        for (float value = minValue; value <= maxValue; value += stepSize) {
            validValues.add(value);
        }
    }

    private void init() {
        trackInactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackInactivePaint.setColor(Color.parseColor("#E0E0E0"));
        trackInactivePaint.setStyle(Paint.Style.FILL);

        trackActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackActivePaint.setColor(Color.parseColor("#09417D"));
        trackActivePaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setColor(Color.parseColor("#09417D"));
        thumbPaint.setStyle(Paint.Style.FILL);

        haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        haloPaint.setColor(Color.parseColor("#1F6750A4"));
        haloPaint.setStyle(Paint.Style.FILL);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(14 * density);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#09417D"));
        tickPaint.setStyle(Paint.Style.FILL);

        pressedAnimator = ValueAnimator.ofFloat(0f, 1f);
        pressedAnimator.setDuration(ANIMATION_DURATION);
        pressedAnimator.addUpdateListener(animation -> {
            haloScale = (float) animation.getAnimatedValue();
            invalidate();
        });

        updateThumbPosition();
    }

    private float getXPositionForValue(float value) {
        float availableWidth = getWidth() - 2 * thumbRadius;
        float percentage = (value - minValue) / (maxValue - minValue);
        return thumbRadius + (availableWidth * percentage);
    }

    private void updateThumbPosition() {
        thumbX = getXPositionForValue(currentValue);
        trackRect.set(thumbRadius, trackY - trackHeight / 2,
                getWidth() - thumbRadius, trackY + trackHeight / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw track
        canvas.drawRoundRect(trackRect, trackHeight / 2, trackHeight / 2, trackInactivePaint);
        canvas.drawRoundRect(
                thumbRadius, trackY - trackHeight / 2,
                thumbX, trackY + trackHeight / 2,
                trackHeight / 2, trackHeight / 2,
                trackActivePaint
        );

        // Draw tick marks
        for (float value : validValues) {
            float tickX = getXPositionForValue(value);
            canvas.drawCircle(tickX, trackY, tickRadius,
                    value <= currentValue ? trackActivePaint : trackInactivePaint);
        }

        // Draw halo and thumb
        if (haloScale > 0) {
            canvas.drawCircle(thumbX, trackY, haloRadius * haloScale, haloPaint);
        }
        canvas.drawCircle(thumbX, trackY, thumbRadius, thumbPaint);

        // Draw value label
       /* String valueText = String.format(Locale.getDefault(), "%.1f km", currentValue);
        canvas.drawText(valueText, thumbX, trackY - thumbRadius - 10, labelPaint);*/
    }

    private float getNearestValidValue(float value) {
        float nearestValue = validValues.get(0);
        float minDifference = Math.abs(value - nearestValue);

        for (float validValue : validValues) {
            float difference = Math.abs(value - validValue);
            if (difference < minDifference) {
                minDifference = difference;
                nearestValue = validValue;
            }
        }
        return nearestValue;
    }

    private void updateValueFromX(float x) {
        float availableWidth = getWidth() - 2 * thumbRadius;
        x = Math.min(Math.max(x, thumbRadius), getWidth() - thumbRadius);
        float percentage = (x - thumbRadius) / availableWidth;
        float rawValue = minValue + percentage * (maxValue - minValue);
        setCurrentValue(getNearestValidValue(rawValue));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInThumbRange(x)) {
                    isDragging = true;
                    animatePressed(true);
                    updateValueFromX(x);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    updateValueFromX(x);
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    isDragging = false;
                    animatePressed(false);
                    updateValueFromX(x);
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private boolean isInThumbRange(float x) {
        return Math.abs(x - thumbX) <= haloRadius;
    }

    private void animatePressed(boolean pressed) {
        pressedAnimator.cancel();
        pressedAnimator.setFloatValues(haloScale, pressed ? 1f : 0f);
        pressedAnimator.start();
    }

    public void setCurrentValue(float value) {
        value = getNearestValidValue(value);
        if (currentValue != value) {
            currentValue = value;
            updateThumbPosition();
            if (listener != null) {
                listener.onValueChanged(currentValue);
            }
            invalidate();
        }
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minHeight = (int) (DEFAULT_HEIGHT_DP * density);
        int height = Math.max(minHeight, MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        trackY = h / 2f;
        updateThumbPosition();
    }
}