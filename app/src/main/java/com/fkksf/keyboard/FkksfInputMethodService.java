package com.fkksf.keyboard;

import android.inputmethodservice.InputMethodService;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayDeque;
import java.util.Deque;

public class FkksfInputMethodService extends InputMethodService {

    private boolean persian = true;
    private LinearLayout keyboard;

    private final Deque<Edit> undoStack = new ArrayDeque<>();
    private final Deque<Edit> redoStack = new ArrayDeque<>();

    private static class Edit {
        String before;
        String after;

        Edit(String b, String a) {
            before = b;
            after = a;
        }
    }

    @Override
    public View onCreateInputView() {
        return createKeyboard();
    }

    private View createKeyboard() {
        keyboard = new LinearLayout(this);
        keyboard.setOrientation(LinearLayout.VERTICAL);
        keyboard.setPadding(2, 3, 2, 3);
        keyboard.setBackgroundColor(Color.rgb(225, 230, 238));

        addRow(new String[]{
            "Paste", "Copy", "Undo", "Redo", "زبان", "⌫"
        });

        if (persian) {
            addRow(new String[]{
                "ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"
            });
            addRow(new String[]{
                "ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"
            });
            addRow(new String[]{
                "ظ","ط","ز","ر","ذ","د","پ","و","چ","ژ"
            });
        } else {
            addRow(new String[]{
                "q","w","e","r","t","y","u","i","o","p"
            });
            addRow(new String[]{
                "a","s","d","f","g","h","j","k","l"
            });
            addRow(new String[]{
                "z","x","c","v","b","n","m"
            });
        }

        addRow(new String[]{
            "←","↑","↓","→","،",".","؟","Space","Enter"
        });

        return keyboard;
    }

    private void addRow(String[] keys) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        for (String key : keys) {
            Button button = new Button(this);
            button.setText(key);
            button.setAllCaps(false);
            button.setTextSize(key.length() > 3 ? 10 : 15);
            button.setPadding(0, 0, 0, 0);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.WHITE);
            bg.setCornerRadius(6);
            bg.setStroke(1, Color.LTGRAY);
            button.setBackground(bg);

            LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, 43, 1);

            lp.setMargins(1, 2, 1, 2);
            row.addView(button, lp);
            button.setOnClickListener(v -> pressKey(key));
        }

        keyboard.addView(row);
    }

    private void recordEdit(String before, String after) {
        undoStack.push(new Edit(before, after));

        while (undoStack.size() > 100) {
            undoStack.removeLast();
        }

        redoStack.clear();
    }

    private void insertText(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        CharSequence selected = ic.getSelectedText(0);
        String before = selected == null ? "" : selected.toString();

        recordEdit(before, text);
        ic.commitText(text, 1);
    }

    private void undo() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || undoStack.isEmpty()) return;

        Edit edit = undoStack.pop();

        if (edit.after.length() > 0) {
            ic.deleteSurroundingText(edit.after.length(), 0);
        }

        if (edit.before.length() > 0) {
            ic.commitText(edit.before, 1);
        }

        redoStack.push(edit);
    }

    private void redo() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || redoStack.isEmpty()) return;

        Edit edit = redoStack.pop();

        if (edit.before.length() > 0) {
            ic.deleteSurroundingText(edit.before.length(), 0);
        }

        if (edit.after.length() > 0) {
            ic.commitText(edit.after, 1);
        }

        undoStack.push(edit);
    }

    private void pressKey(String key) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (key) {
            case "زبان":
                persian = !persian;
                setInputView(createKeyboard());
                return;

            case "Undo":
                undo();
                return;

            case "Redo":
                redo();
                return;

            case "⌫":
                ic.deleteSurroundingText(1, 0);
                return;

            case "Space":
                insertText(" ");
                return;

            case "Enter":
                ic.sendKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                return;

            case "←":
                ic.sendKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                return;

            case "→":
                ic.sendKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                return;

            case "↑":
                ic.sendKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
                return;

            case "↓":
                ic.sendKeyEvent(new KeyEvent(
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
                return;

            case "Paste":
                ClipboardManager cm =
                    (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE);

                if (cm != null && cm.hasPrimaryClip()) {
                    ClipData clip = cm.getPrimaryClip();

                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence text =
                            clip.getItemAt(0).coerceToText(this);

                        if (text != null) insertText(text.toString());
                    }
                }
                return;

            case "Copy":
                CharSequence selected = ic.getSelectedText(0);

                if (selected != null && selected.length() > 0) {
                    ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(
                            Context.CLIPBOARD_SERVICE);

                    if (clipboard != null) {
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Fkksf", selected));
                    }
                }
                return;

            default:
                insertText(key);
                return;
        }
    }
}
