package com.example.myapplicationtext;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
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
    // 数值显示TextView
    private TextView tvTextSizeValue;
    private TextView tvLineMultiplierValue;
    private TextView tvExtraSpacingValue;

    private float density;
    private float scaledDensity;
    private boolean isFontPaddingEnabled = false;
    // 字体文件数组（从资源文件加载）
    private String[] fontFiles;

    // 启用控制CheckBox
    private CheckBox cbEnableLineHeight;
    private CheckBox cbEnableExtraSpacing;

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

        // 加载字体文件数组
        fontFiles = getResources().getStringArray(R.array.font_files);
        Log.d("FontDebug", "字体数组加载完成，长度：" + (fontFiles == null ? 0 : fontFiles.length));

        // 绑定控件
        bindViews();

        // 初始化CheckBox控制的控件状态
        initCheckBoxControl();

        // 初始化数值显示
        initValueDisplay();

        // 初始化监听器
        initListeners();

        // 初始测量展示
        updateTextMeasurements();
    }

    private void bindViews() {
        // 核心展示控件
        textView = findViewById(R.id.textView);
        Log.d("ViewDebug", "textView是否为空：" + (textView == null)); // 若为true，说明ID错了
        tvTextSize = findViewById(R.id.tvTextSize);
        tvMeasuredHeight = findViewById(R.id.tvMeasuredHeight);
        tvActualHeight = findViewById(R.id.tvActualHeight);
        tvTotalHeight = findViewById(R.id.tvTotalHeight);
        tvLayoutHeight = findViewById(R.id.tvLayoutHeight);
        tvFontPaddingStatus = findViewById(R.id.tvFontPaddingStatus);

        // 绑定数值显示TextView
        tvTextSizeValue = findViewById(R.id.tvTextSizeValue);
        tvLineMultiplierValue = findViewById(R.id.tvLineMultiplierValue);
        tvExtraSpacingValue = findViewById(R.id.tvExtraSpacingValue);

        // 启用控制CheckBox
        cbEnableLineHeight = findViewById(R.id.cbEnableLineHeight);
        cbEnableExtraSpacing = findViewById(R.id.cbEnableExtraSpacing);
    }

    // 初始化CheckBox对应的控件启用状态
    private void initCheckBoxControl() {
        SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
        SeekBar sbExtraSpacing = findViewById(R.id.sbExtraLineSpacing);

        sbLineHeight.setEnabled(cbEnableLineHeight.isChecked());
        sbExtraSpacing.setEnabled(cbEnableExtraSpacing.isChecked());
    }

    // 初始化数值显示
    private void initValueDisplay() {
        tvTextSizeValue.setText("12");
        tvLineMultiplierValue.setText("1.0");
        tvExtraSpacingValue.setText("0");
    }

    private void initListeners() {
        // CheckBox启用控制监听
        cbEnableLineHeight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
            sbLineHeight.setEnabled(isChecked);

            if (!isChecked) {
                sbLineHeight.setProgress(10);
                tvLineMultiplierValue.setText("1.0");
                textView.setLineSpacing(textView.getLineSpacingExtra(), 1.0f);
                updateTextMeasurements();
            }
        });

        cbEnableExtraSpacing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SeekBar sbExtraSpacing = findViewById(R.id.sbExtraLineSpacing);
            sbExtraSpacing.setEnabled(isChecked);

            if (!isChecked) {
                sbExtraSpacing.setProgress(0);
                tvExtraSpacingValue.setText("0");
                textView.setLineSpacing(0, textView.getLineSpacingMultiplier());
                updateTextMeasurements();
            }
        });

        // 文本输入实时同步
        TextInputEditText inputEditText = findViewById(R.id.inputEditText);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                textView.setText(s.toString());
                // 输入文本后强制刷新字体显示
                textView.invalidate();
                updateTextMeasurements();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 字号调整监听器
        SeekBar sbTextSize = findViewById(R.id.sbTextSize);
        sbTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 8) progress = 8;
                float newSize = progress;
                textView.setTextSize(newSize);
                tvTextSizeValue.setText(String.valueOf((int) newSize));
                updateTextMeasurements();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 字体选择监听器（核心修复：简化逻辑+解决变量未定义+强制刷新）
        Spinner fontSpinner = findViewById(R.id.fontSpinner);
        // 手动触发初始选择（确保监听执行）
        fontSpinner.setSelection(0, true);
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fontFiles == null || position < 0 || position >= fontFiles.length) {
                    Log.e("FontDebug", "字体数组为空或位置越界");
                    return;
                }

                String fontFile = fontFiles[position];
                Typeface typeface = Typeface.DEFAULT;

                // 打印日志，确认选中的位置和文件名
                Log.d("FontDebug", "选中位置：" + position + "，字体文件：" + fontFile);

                // 非默认字体时加载自定义字体（合并重复逻辑）
                if (fontFile != null && !fontFile.isEmpty()) {
                    try {
                        // 优先使用assets加载（兼容所有版本）
                        String fontPath = "font/" + fontFile;
                        // Android 8.0+ 兼容处理
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // 尝试从res/font目录加载（备选方案）
                            String fontName = fontFile.substring(0, fontFile.lastIndexOf('.'));
                            int fontResId = getResources().getIdentifier(fontName, "font", getPackageName());
                            if (fontResId != 0) {
                                typeface = getResources().getFont(fontResId);
                                Log.d("FontDebug", "从res/font加载成功：" + fontName);
                            } else {
                                // res/font不存在则从assets加载
                                typeface = Typeface.createFromAsset(getAssets(), fontPath);
                                Log.d("FontDebug", "从assets加载成功：" + fontPath);
                            }
                        } else {
                            // 低版本直接从assets加载
                            typeface = Typeface.createFromAsset(getAssets(), fontPath);
                            Log.d("FontDebug", "低版本从assets加载成功：" + fontPath);
                        }

                        // 强制清除字体缓存
                        typeface = Typeface.create(typeface, Typeface.NORMAL);

                    } catch (Exception e) {
                        Log.e("FontDebug", "字体加载失败：" + fontFile, e);
                        typeface = Typeface.DEFAULT;
                    }
                } else {
                    Log.d("FontDebug", "使用系统默认字体");
                }

                // 核心修复：设置字体+强制刷新（多维度确保生效）
                textView.setTypeface(typeface);
                textView.setText(textView.getText()); // 强制重新设置文本内容
                textView.invalidate(); // 强制重绘控件
                textView.requestLayout(); // 强制重新布局
                textView.post(() -> {
                    // 延迟刷新确保生效
                    textView.invalidate();
                    updateTextMeasurements();
                });

                Log.d("FontDebug", "字体已应用，当前字体：" + typeface.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("FontDebug", "Spinner未选中任何项");
            }
        });

        // 行高倍数控制监听器
        SeekBar sbLineHeight = findViewById(R.id.sbLineHeightMultiplier);
        sbLineHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!cbEnableLineHeight.isChecked()) return;

                float multiplier = progress / 10f;
                textView.setLineSpacing(textView.getLineSpacingExtra(), multiplier);
                tvLineMultiplierValue.setText(String.format("%.1f", multiplier));
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
                if (!cbEnableExtraSpacing.isChecked()) return;

                float extraSpacingDp = progress;
                float extraSpacingPx = extraSpacingDp * density;
                textView.setLineSpacing(extraSpacingPx, textView.getLineSpacingMultiplier());
                tvExtraSpacingValue.setText(String.valueOf((int) extraSpacingDp));
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
            isFontPaddingEnabled = checkedId == R.id.rbPaddingOn;
            textView.setIncludeFontPadding(isFontPaddingEnabled);
            tvFontPaddingStatus.setText("字体内边距（includeFontPadding）：" + isFontPaddingEnabled);
            // 切换内边距后强制刷新
            textView.invalidate();
            updateTextMeasurements();
        });

        // 布局变化监听
        textView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int heightPx = bottom - top;
            float heightDp = pxToDp(heightPx);
            tvLayoutHeight.setText("布局完成后高度（dp）：" + String.format("%.2f", heightDp));
        });
    }

    // 更新文本测量数据并展示（优化格式）
    private void updateTextMeasurements() {
        textView.post(() -> {
            int measuredHeightPx = textView.getMeasuredHeight();
            int actualHeightPx = textView.getHeight();
            int totalHeightPx = textView.getPaddingTop() + textView.getPaddingBottom() +
                    (textView.getLineCount() * textView.getLineHeight());

            float measuredHeightDp = pxToDp(measuredHeightPx);
            float actualHeightDp = pxToDp(actualHeightPx);
            float totalHeightDp = pxToDp(totalHeightPx);

            float textSizePx = textView.getTextSize();
            float textSizeSp = pxToSp(textSizePx);

            // 保留两位小数显示
            tvTextSize.setText(String.format("当前字号（sp）：%.2f", textSizeSp));
            tvMeasuredHeight.setText(String.format("测量高度（dp）：%.2f", measuredHeightDp));
            tvActualHeight.setText(String.format("实际高度（dp）：%.2f", actualHeightDp));
            tvTotalHeight.setText(String.format("总高度（含内边距，dp）：%.2f", totalHeightDp));
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