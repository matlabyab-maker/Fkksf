package com.fkksf.keyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;

public class MainActivity extends Activity {
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(20));
        root.setBackgroundColor(Color.rgb(15, 23, 42));

        TextView title = new TextView(this);
        title.setText("Fkksf  •  Fast Keyboard");
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(10), 0, dp(20));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView desc = new TextView(this);
        desc.setText("کیبورد فارسی و انگلیسی با ردیف ابزار سریع، جهت‌نما، کلیپ‌بورد و کلیدهای ویرایش.");
        desc.setTextColor(Color.rgb(203,213,225));
        desc.setTextSize(16);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(dp(8), dp(8), dp(8), dp(22));
        root.addView(desc, new LinearLayout.LayoutParams(-1, -2));

        Button enable = makeButton("۱. فعال‌کردن کیبورد");
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        root.addView(enable, buttonParams());

        Button choose = makeButton("۲. انتخاب Fkksf");
        choose.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });
        root.addView(choose, buttonParams());

        Button test = makeButton("آزمایش نوشتن");
        test.setOnClickListener(v -> {
            android.widget.EditText edit = new android.widget.EditText(this);
            edit.setHint("اینجا بنویسید…");
            edit.setTextColor(Color.WHITE);
            edit.setHintTextColor(Color.LTGRAY);
            edit.setSingleLine(false);
            root.removeView(desc);
            root.addView(edit, 1, new LinearLayout.LayoutParams(-1, dp(120)));
            edit.requestFocus();
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
        });
        root.addView(test, buttonParams());

        TextView foot = new TextView(this);
        foot.setText("پس از فعال‌سازی، از فهرست تغییر کیبورد، Fkksf را انتخاب کنید.");
        foot.setTextColor(Color.rgb(148,163,184));
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(dp(8), dp(24), dp(8), 0);
        root.addView(foot, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }

    private Button makeButton(String s) {
        Button b = new Button(this); b.setText(s); b.setTextSize(16);
        b.setAllCaps(false); b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(37,99,235)));
        return b;
    }
    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54)); p.bottomMargin = dp(12); return p;
    }
}
