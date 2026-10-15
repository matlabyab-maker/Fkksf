package com.fkksf.keyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 24);

        TextView title = new TextView(this);
        title.setText("Fkksf Keyboard");
        title.setTextSize(24);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText(
            "Android Keyboard Fkksf\\n\\n" +
            "Paste, Copy, ReDo, UnDo\\n" +
            "Four cursor arrows\\n" +
            "Persian and English layout"
        );
        info.setTextSize(16);

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(-1, -2);

        lp.topMargin = 20;
        root.addView(info, lp);

        Button settings = new Button(this);
        settings.setText("Keyboard Settings");

        settings.setOnClickListener(v ->
            startActivity(
                new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            )
        );

        root.addView(settings);

        Button picker = new Button(this);
        picker.setText("Choose Fkksf Keyboard");

        picker.setOnClickListener(v -> {
            InputMethodManager imm =
                (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });

        root.addView(picker);
        setContentView(root);
    }
}
