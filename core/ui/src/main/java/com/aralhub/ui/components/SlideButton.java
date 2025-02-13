package com.aralhub.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import com.aralhub.ui.R;

public class SlideButton extends FrameLayout {

    private TextView textView;
    private SlideBar slideBar;

    private SlideButtonListener listener;
    private OnSlideChangeListener slideChangeListener;
    private int offsetThumb;

    public SlideButton(Context context) {
        super(context);
        init(null);
    }

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public SlideButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);

    }

    public SlideButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }


    public int dpToPixels(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }

    public void init(@Nullable AttributeSet set) {


        offsetThumb = dpToPixels(16);
        textView = new TextView(getContext());
        slideBar = new SlideBar(getContext());

        LayoutParams childParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childParams.gravity = Gravity.CENTER;

        LayoutParams trackParams = new LayoutParams(0, dpToPixels(4)); // Height matches default track height
        trackParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;

        slideBar.setLayoutParams(childParams);
        textView.setLayoutParams(childParams);
        slideBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.back_slide_layer));

        textView.setGravity(Gravity.CENTER);

        if (set != null) {
            TypedArray a = getContext().obtainStyledAttributes(set, R.styleable.SlideButton, 0, 0);

            if (a.hasValue(R.styleable.SlideButton_SlideButtonText)) {
                String buttonText = a.getString(R.styleable.SlideButton_SlideButtonText);
                setText(buttonText);
            }

            if (a.hasValue(R.styleable.SlideButton_thumb)) {
                Drawable thumbDrawable;
                thumbDrawable = a.getDrawable(R.styleable.SlideButton_thumb);
                slideBar.setThumb(thumbDrawable);
            } else {
                slideBar.setThumb(ContextCompat.getDrawable(getContext(), R.drawable.back_slide_thumb));
            }

            if (a.hasValue(R.styleable.SlideButton_thumbOffset)) {
                int offset = a.getDimensionPixelSize(R.styleable.SlideButton_thumbOffset, dpToPixels(10));
                offsetThumb += offset;
            }

            if (a.hasValue(R.styleable.SlideButton_sliderBackground)) {
                setBackgroundDrawable(a.getDrawable(R.styleable.SlideButton_sliderBackground));
            } else {
                Drawable bg = ContextCompat.getDrawable(getContext(), R.drawable.back_slide_button);
                int startAlpha = 30;
                if (bg != null){
                    bg.setAlpha(startAlpha);
                    setBackgroundDrawable(bg);
                }
            }

            float unitsTextSize = a.getDimensionPixelSize(R.styleable.SlideButton_SlideButtonTextSize, dpToPixels(20));

            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, unitsTextSize);

            int color = a.getColor(R.styleable.SlideButton_SlideButtonTextColor, Color.WHITE);
            textView.setTextColor(color);
            a.recycle();
        }

        setThumbOffset(offsetThumb);
        this.addView(textView);
        this.addView(slideBar);
    }

    public TextView getTexView() {
        return textView;
    }

    public void setText(@StringRes int res) {
        textView.setText(res);
    }

    public void setText(CharSequence charSequence) {
        textView.setText(charSequence);
    }

    public void setThumb(Drawable drawable) {
        slideBar.setThumb(drawable);
    }

    public void setThumbOffset(int offset) {
        slideBar.setThumbOffset(offset);
    }

    public void setOnSlideChangeListener(OnSlideChangeListener slideChangeListener) {
        this.slideChangeListener = slideChangeListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        slideBar.setEnabled(enabled);
        textView.setEnabled(enabled);
        int color = 0;
        if (!enabled) {
            color = ContextCompat.getColor(getContext(), R.color.slide_button_disabled_filter);
            textView.setVisibility(GONE);
        } else {
            textView.setVisibility(VISIBLE);
        }
        slideBar.getThumb().setColorFilter(color, PorterDuff.Mode.XOR);
    }

    private void updateBackgroundColor(int progress) {
        Drawable background = getBackground();
        if (background != null) {
            background = background.mutate(); // Ensure drawable is mutable

            int minAlpha = 77; // 30% opacity
            int maxAlpha = 255; // 100% opacity

            // Calculate alpha based on progress
            int alpha = minAlpha + (int) ((maxAlpha - minAlpha) * (progress / (float) slideBar.getMax()));

            background.setAlpha(alpha); // Apply calculated alpha
            invalidate(); // Force redraw
        }

    }
    private int interpolateColor(int startColor, int endColor, float fraction) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        return (startA + (int) ((endA - startA) * fraction)) << 24 |
                (startR + (int) ((endR - startR) * fraction)) << 16 |
                (startG + (int) ((endG - startG) * fraction)) << 8 |
                (startB + (int) ((endB - startB) * fraction));
    }
    protected class SlideBar extends AppCompatSeekBar {

        private Drawable thumb;

        private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                onSlideChange(((float) i / getMax()));
                updateBackgroundColor(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        public SlideBar(Context context) {
            super(context);
            init();
        }


        public SlideBar(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public SlideBar(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        @Override
        public void setThumb(Drawable thumb) {
            super.setThumb(thumb);
            this.thumb = thumb;
        }

        @Override
        public Drawable getThumb() {
            return thumb;
        }

        public void init() {
            setMax(100);
            setOnSeekBarChangeListener(seekBarChangeListener);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.i("Action", "Action: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Check if the touch is within the thumb bounds
                    if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                        super.onTouchEvent(event); // Allow SeekBar to handle the touch
                    } else {
                        return false; // Ignore touches outside the thumb
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    super.onTouchEvent(event); // Allow SeekBar to handle the move
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    int targetProgress = (getProgress() > 60) ? 100 : 0;
                    ValueAnimator animator = ValueAnimator.ofInt(getProgress(), targetProgress);
                    animator.setDuration(200); // Duration of the animation in milliseconds
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                            setProgress((int) animation.getAnimatedValue());
                        }
                    });
                    animator.start();

                    if (targetProgress == 100) {
                        onSlide(); // Trigger the slide event
                    }
                    break;

                default:
                    super.onTouchEvent(event); // Handle other events
                    break;
            }
            return true;
        }

        private void onSlide() {
            if (listener != null) {
                listener.onSlide();
            }
        }

        private void onSlideChange(float position) {
            if (slideChangeListener != null) {
                slideChangeListener.onSlideChange(position);
            }
        }

    }

    public void setSlideButtonListener(SlideButtonListener listener) {
        this.listener = listener;
    }

    public interface SlideButtonListener {
        public void onSlide();
    }

    public interface OnSlideChangeListener {
        public void onSlideChange(float position);
    }

}