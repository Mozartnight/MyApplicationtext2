package com.example.myapplicationtext;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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
    private TextView tvTextSizeValue;
    private TextView tvLineHeightValue;
    private TextView tvExtraLineSpacingValue;
    // 新增：字体内边距状态展示控件
    private TextView tvFontPaddingStatus;
    private float density;
    private float scaledDensity;
    // 记录 includeFontPadding 状态
    private boolean isFontPaddingEnabled = false;

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

        // 绑定控件
        bindViews();

        // 初始化监听器
        initListeners();

        // 初始测量展示
        updateTextMeasurements();
    }

    private void bindViews() {
        textView = findViewById(R.id.textView);
        tvTextSize = findViewById(R.id.tvTextSize);
        tvMeasuredHeight = findViewById(R.id.tvMeasuredHeight);
        tvActualHeight = findViewById(R.id.tvActualHeight);
        tvTotalHeight = findViewById(R.id.tvTotalHeight);
        tvLayoutHeight = findViewById(R.id.tvLayoutHeight);
        tvTextSizeValue = findViewById(R.id.tvTextSizeValue);
        tvLineHeightValue = findViewById(R.id.tvLineHeightValue);
        tvExtraLineSpacingValue = findViewById(R.id.tvExtraLineSpacingValue);
        // 绑定新增的字体内边距状态展示控件
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
                tvTextSizeValue.setText(newSize + "sp");
                textView.setTextSize(newSize); // 默认为sp单位
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 字重控制监听器
        RadioGroup rgFontWeight = findViewById(R.id.rgFontWeight);
        rgFontWeight.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBold) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }
            updateTextMeasurements();
        });

        // 行高倍数控制监听器
        SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
        sbLineHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float multiplier = progress / 10f; // 0.0-3.0倍
                tvLineHeightValue.setText(String.format("%.1fx", multiplier));
                textView.setLineSpacing(textView.getLineSpacingExtra(), multiplier);
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 额外行间距控制监听器
        SeekBar sbExtraSpacing = findViewById(R.id.sbExtraLineSpacing);
        sbExtraSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float extraSpacingDp = progress;
                float extraSpacingPx = extraSpacingDp * density; // 转换为px
                tvExtraLineSpacingValue.setText(extraSpacingDp + "dp");
                textView.setLineSpacing(extraSpacingPx, textView.getLineSpacingMultiplier());
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 新增：includeFontPadding 控制监听器
        RadioGroup rgFontPadding = findViewById(R.id.rgFontPadding);
        rgFontPadding.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPaddingOn) {
                isFontPaddingEnabled = true;
                textView.setIncludeFontPadding(true);
            } else {
                isFontPaddingEnabled = false;
                textView.setIncludeFontPadding(false);
            }
            // 更新展示状态
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
            // 确保 includeFontPadding 状态展示同步
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