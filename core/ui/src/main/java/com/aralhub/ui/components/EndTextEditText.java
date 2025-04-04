package com.aralhub.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.aralhub.ui.R;

public class EndTextEditText extends LinearLayout {

    private OnClickListener endTextClickListener;
    private boolean isProgrammaticChange = false; // Flag to track programmatic changes

    public EndTextEditText(Context context) {
        super(context);
        init(context, null);
    }

    public EndTextEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EndTextEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        setClickable(true);
        setFocusable(true);
        setBackground(ContextCompat.getDrawable(context, R.drawable.selector_edit_text_auth));

        // Create start icon container
        LinearLayout startIconContainer = new LinearLayout(context);
        startIconContainer.setOrientation(HORIZONTAL);
        startIconContainer.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams startContainerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
        );
        startContainerParams.setMargins(dpToPx(16), 0, 0, 0);
        addView(startIconContainer, startContainerParams);

        // Create start icon ImageView
        ImageView startIconView = new ImageView(context);
        LayoutParams startIconParams = new LayoutParams(dpToPx(24), dpToPx(24));
        startIconParams.gravity = Gravity.CENTER_VERTICAL;
        startIconContainer.addView(startIconView, startIconParams);

        // Create EditText
        AppCompatEditText editText = new AppCompatEditText(context, attrs);
        editText.setMaxLines(1);
        editText.setSingleLine(true);
        editText.setEllipsize(TextUtils.TruncateAt.END);
        editText.setHorizontallyScrolling(false);
        editText.setTextSize(14f);
        editText.setTextColor(ContextCompat.getColor(context, R.color.color_content_secondary));
        editText.setBackground(null);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS); // No suggestions/autocorrect
        editText.setTextIsSelectable(true);
        editText.setOnFocusChangeListener((v, hasFocus) -> setActivated(hasFocus));
        LayoutParams editTextParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        editTextParams.setMargins(0, 0, 0, 0);
        addView(editText, editTextParams);

        // Create end text container
        LinearLayout endTextContainer = new LinearLayout(context);
        endTextContainer.setOrientation(HORIZONTAL);
        endTextContainer.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams containerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
        );
        containerParams.setMargins(dpToPx(16), 0, dpToPx(16), 0);
        addView(endTextContainer, containerParams);

        // Divider
        View dividerView = new View(context);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                dpToPx(1),
                dpToPx(24)
        );
        dividerParams.setMarginEnd(dpToPx(16));
        dividerView.setBackgroundColor(Color.LTGRAY);
        endTextContainer.addView(dividerView, dividerParams);

        // End TextView
        TextView endTextView = new TextView(context);
        LayoutParams endTextViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        endTextViewParams.gravity = Gravity.CENTER_VERTICAL;
        endTextView.setClickable(true);
        endTextView.setFocusable(true);
        endTextView.setTextColor(ContextCompat.getColor(context, R.color.color_content_secondary));
        endTextView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ripple_text));
        endTextContainer.addView(endTextView, endTextViewParams);

        endTextView.setOnClickListener(v -> {
            if (endTextClickListener != null) {
                endTextClickListener.onClick(v);
            }
        });

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EndTextEditText);

            // Handle start icon drawable
            Drawable startIcon = a.getDrawable(R.styleable.EndTextEditText_startIconDrawable);
            if (startIcon != null) {
                startIconView.setImageDrawable(startIcon);
                startIconContainer.setVisibility(VISIBLE);
            } else {
                startIconContainer.setVisibility(INVISIBLE);
            }

            String endText = a.getString(R.styleable.EndTextEditText_endText);
            String hint = a.getString(R.styleable.EndTextEditText_android_hint);
            editText.setHint(hint);
            editText.setHintTextColor(a.getColor(R.styleable.EndTextEditText_hintTextColor, Color.GRAY));

            // Показывать endTextContainer только если активен input
            endTextContainer.setVisibility(GONE);

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                setActivated(hasFocus);
                endTextContainer.setVisibility(hasFocus ? VISIBLE : GONE);
            });

            endTextView.setText(endText);
            a.recycle();
        }

    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    public void setEndTextClickListener(OnClickListener listener) {
        this.endTextClickListener = listener;
    }

    public String getText() {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(1);
        return editText.getText().toString();
    }

    public void setText(String text) {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(1);
        isProgrammaticChange = true; // Set flag before programmatic change
        editText.setText(text);
        isProgrammaticChange = false; // Reset flag after change
    }

    public void setHint(String hint) {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(1);
        editText.setHint(hint);
    }

    public void setStartIconDrawable(Drawable drawable) {
        LinearLayout startIconContainer = (LinearLayout) getChildAt(0);
        ImageView startIconView = (ImageView) startIconContainer.getChildAt(0);
        if (drawable != null) {
            startIconView.setImageDrawable(drawable);
            startIconContainer.setVisibility(VISIBLE);
        } else {
            startIconContainer.setVisibility(GONE);
        }
    }

    public void setStartIconTint(int color) {
        LinearLayout startIconContainer = (LinearLayout) getChildAt(0);
        ImageView startIconView = (ImageView) startIconContainer.getChildAt(0);
        startIconView.setColorFilter(color);
    }

    public void setEndTextVisible(boolean visible) {
        LinearLayout endTextContainer = (LinearLayout) getChildAt(2);
        endTextContainer.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setStartIconVisible(boolean visible) {
        LinearLayout startIconContainer = (LinearLayout) getChildAt(0);
        startIconContainer.setVisibility(visible ? VISIBLE : GONE);
    }

    // Interface for activation listener
    public interface OnActivatedListener {
        void onActivated(boolean activated);
    }

    public void setOnActivatedListener(final OnActivatedListener listener) {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(1);
        editText.setOnFocusChangeListener((v, hasFocus) -> listener.onActivated(hasFocus));
    }

    // Interface for text changed listener
    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    public void setOnTextChangedListener(final OnTextChangedListener listener) {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(1);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isProgrammaticChange) { // Only trigger for manual changes
                    listener.onTextChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });
    }
}