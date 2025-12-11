package com.example.myapplicationtext;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private TextView tvTextSize;
    private TextView tvMeasuredHeight;
    private TextView tvActualHeight;
    private TextView tvTotalHeight;
    private TextView tvLayoutHeight;
    private TextView tvFontPaddingStatus;
    private float density;
    private float scaledDensity;
    private boolean isFontPaddingEnabled = false;
    // 字重数组（从资源文件加载）
    private int[] fontWeights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 处理系统窗口Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 获取设备密度
        density = getResources().getDisplayMetrics().density;
        scaledDensity = getResources().getDisplayMetrics().scaledDensity;

        // 加载字重数组（长度为4，索引0-3）
        fontWeights = getResources().getIntArray(R.array.font_weight_values);

        // 绑定控件
        bindViews();

        // 初始化监听器
        initListeners();

        // 初始测量展示
        updateTextMeasurements();
    }

    private void bindViews() {
        // 只绑定布局中实际存在的ID
        textView = findViewById(R.id.textView);
        tvTextSize = findViewById(R.id.tvTextSize);
        tvMeasuredHeight = findViewById(R.id.tvMeasuredHeight);
        tvActualHeight = findViewById(R.id.tvActualHeight);
        tvTotalHeight = findViewById(R.id.tvTotalHeight);
        tvLayoutHeight = findViewById(R.id.tvLayoutHeight);
        tvFontPaddingStatus = findViewById(R.id.tvFontPaddingStatus);
    }

    private void initListeners() {
        // 文本输入实时同步
        TextInputEditText inputEditText = findViewById(R.id.inputEditText);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                textView.setText(s.toString());
                updateTextMeasurements();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 字号调整监听器（SeekBar）
        SeekBar sbTextSize = findViewById(R.id.sbTextSize);
        sbTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 8) progress = 8; // 最小字号限制
                float newSize = progress;
                textView.setTextSize(newSize); // 默认为sp单位
                // 同步更新输入框
                EditText etTextSize = findViewById(R.id.etTextSize);
                etTextSize.removeTextChangedListener((TextWatcher) etTextSize.getTag());
                etTextSize.setText(String.valueOf(newSize));
                etTextSize.setTag(null);
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 字号输入框实时更新
        EditText etTextSize = findViewById(R.id.etTextSize);
        TextWatcher textSizeWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    float textSize = Float.parseFloat(s.toString());
                    if (textSize >= 8) { // 最小限制
                        textView.setTextSize(textSize);
                        // 同步更新SeekBar
                        SeekBar sbTextSize = findViewById(R.id.sbTextSize);
                        sbTextSize.setProgress((int) textSize);
                        updateTextMeasurements();
                    }
                } catch (NumberFormatException e) {
                    // 输入无效时忽略
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etTextSize.addTextChangedListener(textSizeWatcher);
        etTextSize.setTag(textSizeWatcher);

        // 字重控制监听器
        Spinner fontWeightSpinner = findViewById(R.id.fontWeightSpinner);
        fontWeightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 确保索引在数组范围内（0-3）
                if (position >= 0 && position < fontWeights.length) {
                    int selectedWeight = fontWeights[position];
                    Typeface typeface = Typeface.create(null, selectedWeight, false);
                    textView.setTypeface(typeface);
                    updateTextMeasurements();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 行高倍数控制监听器（SeekBar）
        SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
        sbLineHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float multiplier = progress / 10f; // 0.0-3.0倍
                textView.setLineSpacing(textView.getLineSpacingExtra(), multiplier);
                // 同步更新输入框
                EditText etLineMultiplier = findViewById(R.id.etLineMultiplier);
                etLineMultiplier.removeTextChangedListener((TextWatcher) etLineMultiplier.getTag());
                etLineMultiplier.setText(String.valueOf(multiplier));
                etLineMultiplier.setTag(null);
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 行高倍数输入框实时更新
        EditText etLineMultiplier = findViewById(R.id.etLineMultiplier);
        TextWatcher lineMultiplierWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    float multiplier = Float.parseFloat(s.toString());
                    textView.setLineSpacing(textView.getLineSpacingExtra(), multiplier);
                    // 同步更新SeekBar
                    SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
                    sbLineHeight.setProgress((int) (multiplier * 10));
                    updateTextMeasurements();
                } catch (NumberFormatException e) {
                    // 输入无效时忽略
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etLineMultiplier.addTextChangedListener(lineMultiplierWatcher);
        etLineMultiplier.setTag(lineMultiplierWatcher);

        // 额外行间距控制监听器（SeekBar）
        SeekBar sbExtraSpacing = findViewById(R.id.sbExtraLineSpacing);
        sbExtraSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float extraSpacingDp = progress;
                float extraSpacingPx = extraSpacingDp * density; // 转换为px
                textView.setLineSpacing(extraSpacingPx, textView.getLineSpacingMultiplier());
                // 同步更新输入框
                EditText etExtraSpacing = findViewById(R.id.etExtraSpacing);
                etExtraSpacing.removeTextChangedListener((TextWatcher) etExtraSpacing.getTag());
                etExtraSpacing.setText(String.valueOf(extraSpacingDp));
                etExtraSpacing.setTag(null);
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 额外行间距输入框实时更新
        EditText etExtraSpacing = findViewById(R.id.etExtraSpacing);
        TextWatcher extraSpacingWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    float extraSpacingDp = Float.parseFloat(s.toString());
                    float extraSpacingPx = extraSpacingDp * density;
                    textView.setLineSpacing(extraSpacingPx, textView.getLineSpacingMultiplier());
                    // 同步更新SeekBar
                    SeekBar sbExtraSpacing = findViewById(R.id.sbExtraLineSpacing);
                    sbExtraSpacing.setProgress((int) extraSpacingDp);
                    updateTextMeasurements();
                } catch (NumberFormatException e) {
                    // 输入无效时忽略
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etExtraSpacing.addTextChangedListener(extraSpacingWatcher);
        etExtraSpacing.setTag(extraSpacingWatcher);

        // includeFontPadding 控制监听器
        RadioGroup rgFontPadding = findViewById(R.id.rgFontPadding);
        rgFontPadding.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPaddingOn) {
                isFontPaddingEnabled = true;
                textView.setIncludeFontPadding(true);
            } else {
                isFontPaddingEnabled = false;
                textView.setIncludeFontPadding(false);
            }
            tvFontPaddingStatus.setText("字体内边距（includeFontPadding）：" + isFontPaddingEnabled);
            updateTextMeasurements();
        });

        // 布局变化监听
        textView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int heightPx = bottom - top;
            float heightDp = pxToDp(heightPx);
            tvLayoutHeight.setText("布局完成后高度（dp）：" + heightDp);
        });
    }

    // 更新文本测量数据并展示
    private void updateTextMeasurements() {
        textView.post(() -> {
            // 高度测量与转换
            int measuredHeightPx = textView.getMeasuredHeight();
            int actualHeightPx = textView.getHeight();
            int totalHeightPx = textView.getPaddingTop() + textView.getPaddingBottom() +
                    (textView.getLineCount() * textView.getLineHeight());

            float measuredHeightDp = pxToDp(measuredHeightPx);
            float actualHeightDp = pxToDp(actualHeightPx);
            float totalHeightDp = pxToDp(totalHeightPx);

            // 字号测量与转换
            float textSizePx = textView.getTextSize();
            float textSizeSp = pxToSp(textSizePx);

            // 展示结果
            tvTextSize.setText("当前字号（sp）：" + textSizeSp);
            tvMeasuredHeight.setText("测量高度（dp）：" + measuredHeightDp);
            tvActualHeight.setText("实际高度（dp）：" + actualHeightDp);
            tvTotalHeight.setText("总高度（含内边距，dp）：" + totalHeightDp);
            tvFontPaddingStatus.setText("字体内边距（includeFontPadding）：" + isFontPaddingEnabled);
        });
    }

    // px转dp
    private float pxToDp(int px) {
        return (float) Math.round((px / density) * 100) / 100;
    }

    // px转sp
    private float pxToSp(float px) {
        return (float) Math.round((px / scaledDensity) * 100) / 100;
    }
}