package com.fkksf.keyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayDeque;

public class FkksfInputMethodService extends InputMethodService {
    private LinearLayout root, toolsRow, keysArea;
    private boolean persian = true, symbols = false, shifted = false;
    private String lastDeleted = "";
    private final ArrayDeque<String> undo = new ArrayDeque<>();
    private final ArrayDeque<String> redo = new ArrayDeque<>();
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    @Override public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(3), dp(3), dp(3), dp(4));
        root.setBackgroundColor(Color.rgb(17,24,39));

        toolsRow = new LinearLayout(this);
        toolsRow.setOrientation(LinearLayout.HORIZONTAL);
        toolsRow.setGravity(Gravity.CENTER);
        String[] labels = {"Paste","Copy","Undo","Redo","←","→","↑","↓","زبان"};
        int[] colors = {0xff0f766e,0xff2563eb,0xff7c3aed,0xff9333ea,0xff475569,0xff475569,0xff475569,0xff475569,0xffb45309};
        for (int i=0;i<labels.length;i++) {
            final String action = labels[i];
            Button b = button(labels[i], colors[i], 11);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(38), 1f);
            p.setMargins(dp(1),0,dp(1),dp(3));
            toolsRow.addView(b,p);
            b.setOnClickListener(v -> tool(action));
        }
        root.addView(toolsRow, new LinearLayout.LayoutParams(-1, dp(41)));
        keysArea = new LinearLayout(this);
        keysArea.setOrientation(LinearLayout.VERTICAL);
        root.addView(keysArea, new LinearLayout.LayoutParams(-1, -2));
        renderKeys();
        return root;
    }

    private Button button(String label, int color, int size) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(size); b.setTextColor(Color.WHITE);
        b.setAllCaps(false); b.setPadding(0,0,0,0); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setMinHeight(0); b.setMinimumHeight(0); b.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(color); bg.setCornerRadius(dp(5));
        b.setBackground(bg);
        return b;
    }

    private void renderKeys() {
        keysArea.removeAllViews();
        String[][] rows;
        if (symbols) {
            rows = new String[][] {
                {"1","2","3","4","5","6","7","8","9","0"},
                {"@","#","$","%","&","-","+","(",")","/"},
                {"=","*","\"","'",":",";","!","?","⌫"},
                {"ABC",",","🌐","space",".","↵"}
            };
        } else if (persian) {
            rows = new String[][] {
                {"ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"},
                {"ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"},
                {"⇧","ظ","ط","ز","ر","ذ","د","پ","و","⌫"},
                {"?123",",","🌐","space",".","↵"}
            };
        } else {
            rows = new String[][] {
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"⇧","z","x","c","v","b","n","m","⌫"},
                {"?123",",","🌐","space",".","↵"}
            };
        }
        for (int r=0;r<rows.length;r++) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(46));
            lp.setMargins(0,dp(1),0,dp(1));
            keysArea.addView(line, lp);
            for (String key : rows[r]) {
                int weight = key.equals("space") ? 4 : (key.equals("↵") || key.equals("⌫") || key.equals("?123") || key.equals("⇧") || key.equals("🌐") ? 1.35f : 1f);
                int col = key.equals("↵") ? 0xff0f766e : (key.equals("⌫") || key.equals("⇧") || key.equals("?123") || key.equals("🌐") ? 0xff475569 : 0xff334155);
                String shown = key.equals("space") ? "فاصله" : key;
                Button b = button(shown, col, key.equals("space") ? 12 : 16);
                LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(0, -1, weight);
                kp.setMargins(dp(1),dp(1),dp(1),dp(1));
                line.addView(b,kp);
                b.setOnClickListener(v -> press(key));
            }
        }
    }

    private void tool(String action) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        switch (action) {
            case "Paste": {
                ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null && cm.hasPrimaryClip()) {
                    ClipData d = cm.getPrimaryClip();
                    if (d != null && d.getItemCount()>0) {
                        CharSequence t = d.getItemAt(0).coerceToText(this);
                        if (t != null) ic.commitText(t,1);
                    }
                } break;
            }
            case "Copy": {
                CharSequence s = ic.getSelectedText(0);
                if (s != null) {
                    ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Fkksf",s));
                } break;
            }
            case "Undo":
                if (!undo.isEmpty()) { String s=undo.pop(); ic.deleteSurroundingText(s.length(),0); redo.push(s); } break;
            case "Redo":
                if (!redo.isEmpty()) { String s=redo.pop(); ic.commitText(s,1); undo.push(s); } break;
            case "←": ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_LEFT)); break;
            case "→": ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT)); break;
            case "↑": ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP)); break;
            case "↓": ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN)); break;
            case "زبان": persian=!persian; symbols=false; renderKeys(); break;
        }
    }

    private void press(String key) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        if (key.equals("🌐")) { persian=!persian; symbols=false; renderKeys(); return; }
        if (key.equals("?123")) { symbols=true; renderKeys(); return; }
        if (key.equals("ABC")) { symbols=false; renderKeys(); return; }
        if (key.equals("⇧")) { shifted=!shifted; renderKeys(); return; }
        if (key.equals("⌫")) {
            CharSequence before=ic.getTextBeforeCursor(1,0);
            if (before!=null && before.length()>0) {
                lastDeleted=before.toString(); undo.push(lastDeleted);
                ic.deleteSurroundingText(1,0);
            } else ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));
            return;
        }
        if (key.equals("↵")) {
            EditorInfo info=getCurrentInputEditorInfo();
            if (info!=null && (info.imeOptions & (EditorInfo.IME_FLAG_NO_ENTER_ACTION|EditorInfo.IME_MASK_ACTION)) != EditorInfo.IME_ACTION_NONE) {
                ic.performEditorAction(info.imeOptions & EditorInfo.IME_MASK_ACTION);
            } else ic.commitText("\\n",1);
            return;
        }
        if (key.equals("space")) { ic.commitText(" ",1); shifted=false; return; }
        if (key.equals("," ) || key.equals(".")) { ic.commitText(key,1); shifted=false; return; }
        String out=key;
        if (shifted && key.length()==1 && key.matches("[a-z]")) out=key.toUpperCase();
        ic.commitText(out,1);
        shifted=false;
    }
}
