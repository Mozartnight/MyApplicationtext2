package com.example.myapplicationtext;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
        // 字号调整监听器
        SeekBar sbTextSize = findViewById(R.id.sbTextSize);
        sbTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 8) progress = 8; // 最小字号限制
                float newSize = progress;
                textView.setTextSize(newSize); // 默认为sp单位
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 字重控制监听器（修正核心：使用Spinner选中位置作为索引）
        Spinner fontWeightSpinner = findViewById(R.id.fontWeightSpinner);
        fontWeightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 确保索引在数组范围内（0-3）
                if (position >= 0 && position < fontWeights.length) {
                    int selectedWeight = fontWeights[position];
                    // 使用正确的方式设置字重（避免Typeface.style参数错误）
                    Typeface typeface = Typeface.create(null, selectedWeight, false);
                    textView.setTypeface(typeface);
                    updateTextMeasurements();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 行高倍数控制监听器
        SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
        sbLineHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float multiplier = progress / 10f; // 0.0-3.0倍
                textView.setLineSpacing(textView.getLineSpacingExtra(), multiplier);
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 额外行间距距控制监听器
        SeekBar sbExtraSpacing = findViewById(R.id.sbExtraLineSpacing);
        sbExtraSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float extraSpacingDp = progress;
                float extraSpacingPx = extraSpacingDp * density; // 转换为px
                textView.setLineSpacing(extraSpacingPx, textView.getLineSpacingMultiplier());
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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

        // 应用按钮点击事件（处理输入框设置）
        Button btnApply = findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> applyTextSettings());
    }

    // 处理输入框中的文本设置（字号、行高、额外行间距）
    private void applyTextSettings() {
        // 处理字号输入
        EditText etTextSize = findViewById(R.id.etTextSize);
        try {
            float textSize = Float.parseFloat(etTextSize.getText().toString());
            if (textSize >= 8) { // 最小限制
                textView.setTextSize(textSize);
            }
        } catch (NumberFormatException e) {
            // 输入无效时忽略
        }

        // 处理行高倍数输入
        EditText etLineMultiplier = findViewById(R.id.etLineMultiplier);
        try {
            float multiplier = Float.parseFloat(etLineMultiplier.getText().toString());
            textView.setLineSpacing(textView.getLineSpacingExtra(), multiplier);
        } catch (NumberFormatException e) {
            // 输入无效时忽略
        }

        // 处理额外行间距输入
        EditText etExtraSpacing = findViewById(R.id.etExtraSpacing);
        try {
            float extraSpacingDp = Float.parseFloat(etExtraSpacing.getText().toString());
            float extraSpacingPx = extraSpacingDp * density;
            textView.setLineSpacing(extraSpacingPx, textView.getLineSpacingMultiplier());
        } catch (NumberFormatException e) {
            // 输入无效时忽略
        }

        updateTextMeasurements();
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