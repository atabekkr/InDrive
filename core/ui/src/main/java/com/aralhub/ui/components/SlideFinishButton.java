package com.aralhub.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.aralhub.ui.R;

public class SlideFinishButton extends View {

    private final Paint chevronBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF chevronCircleRect = new RectF();  // For drawing the circle
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path chevronPath = new Path();
    private final RectF buttonRect = new RectF();
    private final RectF progressRect = new RectF();

    private float slideProgress = 0f;
    private float lastTouchX;
    private boolean isSliding = false;
    private OnSlideCompleteListener listener;
    private ValueAnimator resetAnimator;
    private ValueAnimator completeAnimator;

    private static final int BUTTON_COLOR = 0xFFBCCDB3;
    private static final int PROGRESS_COLOR =0xFF28A745;
    private static String BUTTON_TEXT = "Finish";
    private static final float THRESHOLD = 0.6f; // 60% threshold

    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int COMPLETED_TEXT_COLOR = Color.WHITE;  // Or any color you like

    public interface OnSlideCompleteListener {
        void onSlideComplete();
    }

    public SlideFinishButton(Context context) {
        super(context);
        init(context);
    }

    public SlideFinishButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        backgroundPaint.setColor(BUTTON_COLOR);
        progressPaint.setColor(PROGRESS_COLOR);
        textPaint.setColor(DEFAULT_TEXT_COLOR);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        iconPaint.setColor(PROGRESS_COLOR);
        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setStrokeWidth(4f);
        chevronBackgroundPaint.setColor(PROGRESS_COLOR);
        chevronBackgroundPaint.setStyle(Paint.Style.FILL);

        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.SlideFinishButton);
        if (typedArray.getString(R.styleable.SlideFinishButton_slideFinishButtonText) != null) {
            BUTTON_TEXT = typedArray.getString(R.styleable.SlideFinishButton_slideFinishButtonText);
            invalidate();
        }

        typedArray.recycle();

        setupAnimations();
    }

    private void setupAnimations() {
        resetAnimator = new ValueAnimator();
        resetAnimator.setDuration(300);
        resetAnimator.setInterpolator(new DecelerateInterpolator());
        resetAnimator.addUpdateListener(animation -> {
            slideProgress = (float) animation.getAnimatedValue();
            invalidate();
        });

        completeAnimator = new ValueAnimator();
        completeAnimator.setDuration(200);
        completeAnimator.setInterpolator(new LinearInterpolator());
        completeAnimator.addUpdateListener(animation -> {
            slideProgress = (float) animation.getAnimatedValue();
            invalidate();
            if (slideProgress >= 1f && listener != null) {
                listener.onSlideComplete();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        buttonRect.set(0, 0, w, h);
        updateChevronPath();
    }

    private void updateChevronPath() {
        float centerY = getHeight() / 2f;
        float chevronWidth = getHeight() / 4f;
        float chevronHeight = getHeight() / 3f;
        float startX = chevronWidth;

        chevronPath.reset();
        chevronPath.moveTo(startX, centerY - chevronHeight / 2);
        chevronPath.lineTo(startX + chevronWidth / 2, centerY);
        chevronPath.lineTo(startX, centerY + chevronHeight / 2);
        chevronPath.moveTo(startX + chevronWidth / 2, centerY - chevronHeight / 2);
        chevronPath.lineTo(startX + chevronWidth, centerY);
        chevronPath.lineTo(startX + chevronWidth / 2, centerY + chevronHeight / 2);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float cornerRadius = getHeight() / 2f;
        canvas.drawRoundRect(buttonRect, cornerRadius, cornerRadius, backgroundPaint);

        progressRect.set(buttonRect);
        progressRect.right = buttonRect.left + (buttonRect.width() * slideProgress);
        canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint);

        // Draw chevron with circular background
        canvas.save();
        float translation = slideProgress * (getWidth() - getHeight());  // Modified this
        canvas.translate(translation, 0);

        // Draw the circular background to match full height
        float circleRadius = getHeight() / 2f;
        chevronCircleRect.set(
                0,                  // Left matches view edge
                0,                  // Top matches view edge
                getHeight(),        // Width equals height for perfect circle
                getHeight()         // Full height of view
        );
        chevronBackgroundPaint.setColor(PROGRESS_COLOR);
        canvas.drawOval(chevronCircleRect, chevronBackgroundPaint);

        // Draw the chevron in white
        iconPaint.setColor(Color.WHITE);
        canvas.drawPath(chevronPath, iconPaint);
        canvas.restore();

        // Draw text
        textPaint.setColor(getTextColorBasedOnProgress(slideProgress));
        float textX = getWidth() / 2f;
        float textY = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(BUTTON_TEXT, textX, textY, textPaint);
    }

    // Helper method to calculate text color based on progress
    private int getTextColorBasedOnProgress(float progress) {
        // Interpolates between DEFAULT_TEXT_COLOR (black) and COMPLETED_TEXT_COLOR (green)
        return ColorUtils.blendARGB(DEFAULT_TEXT_COLOR, COMPLETED_TEXT_COLOR, progress);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                isSliding = true;
                if (resetAnimator.isRunning()) resetAnimator.cancel();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isSliding) {
                    float delta = event.getX() - lastTouchX;
                    slideProgress = Math.max(0f, Math.min(1f,
                            slideProgress + (delta / (getWidth() - getHeight() / 2))));
                    lastTouchX = event.getX();
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isSliding = false;
                if (slideProgress >= THRESHOLD) {
                    // Animate to complete from the current progress
                    completeAnimator.setFloatValues(slideProgress, 1f);
                    completeAnimator.start();
                } else {
                    // Animate to reset from the current progress
                    resetAnimator.setFloatValues(slideProgress, 0f);
                    resetAnimator.start();
                }
                return true;
        }
        return false;
    }

    public void setOnSlideCompleteListener(OnSlideCompleteListener listener) {
        this.listener = listener;
    }

    public void reset() {
        slideProgress = 0f;
        invalidate();
    }
}