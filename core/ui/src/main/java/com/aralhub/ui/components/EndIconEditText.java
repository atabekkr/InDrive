package com.aralhub.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.aralhub.ui.R;

public class EndIconEditText extends LinearLayout {

    private OnClickListener _endIconOnClickListener;

    public EndIconEditText(Context context) {
        super(context);
        init(context, null);
    }

    public EndIconEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EndIconEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public EndIconEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        AppCompatEditText editText = new AppCompatEditText(context, attrs);
        editText.setBackground(null);
        LayoutParams editTextParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        editTextParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editText.post(() -> {
                    editText.setSelection(editText.getText().length());
                });
            }
        });

        addView(editText, editTextParams);

        FrameLayout endIconContainer = new FrameLayout(context);
        View icon = new View(context);
        LayoutParams iconParams = new LayoutParams(dpToPx(24), dpToPx(24));
        iconParams.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        endIconContainer.setClickable(true);
        endIconContainer.setFocusable(true);
        endIconContainer.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ripple_round));
        endIconContainer.addView(icon, iconParams);
        endIconContainer.setOnClickListener(v -> {
            if (_endIconOnClickListener != null) {
                _endIconOnClickListener.onClick(v);
            }
        });
        addView(endIconContainer);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EndIconEditText);
            icon.setBackground(ContextCompat.getDrawable(getContext(), a.getResourceId(R.styleable.EndIconEditText_icon, 0)));
            icon.setBackgroundTintList(a.getColorStateList(R.styleable.EndIconEditText_iconTint));
            editText.setHint(a.getString(R.styleable.EndIconEditText_hint));
            a.recycle();
        }
    }

    public void setTextAndMoveCursorToEnd(String text) {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(0);
        editText.setText(text);
        editText.setSelection(editText.getText().length());
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    public void setEndIconOnClickListener(OnClickListener endIconOnClickListener) {
        _endIconOnClickListener = endIconOnClickListener;
    }

    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    public void setOnTextChangedListener(final OnTextChangedListener onTextChangedListener) {
        AppCompatEditText editText = (AppCompatEditText) getChildAt(0);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                onTextChangedListener.onTextChanged(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
