package com.raazdar.arrowpuzzlegame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Host activity for Raazdar: Arrow Puzzle Game.
 *
 * Design notes (documented decisions, from the audited spec):
 * - Single-activity, single custom-view board; zero network permissions.
 * - Hearts pool fixed at 3 with "Try again" revive that preserves cleared arrows.
 * - No coins/SKUs/ads: economy values in the original analysis were UNVERIFIED,
 *   so the rebuild ships a distraction-free free game instead (documented).
 * - Undo is unlimited (booster-inventory economies were unverified).
 * - Launcher display name is "Arrow Puzzle"; Raazdar brand appears in About.
 */
public class MainActivity extends Activity {

    private TextView levelView, heartsView;
    private GameView board;
    private SharedPreferences prefs;

    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("raazdar_arrowpuzzle", MODE_PRIVATE);
        currentLevel = prefs.getInt("currentLevel", 1);
        setContentView(buildUi());
        showLevel(currentLevel);
    }

    private View buildUi() {
        FrameLayout root = new FrameLayout(this);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        root.addView(column, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ---- Top HUD ----
        LinearLayout hud = new LinearLayout(this);
        hud.setOrientation(LinearLayout.HORIZONTAL);
        hud.setPadding(px(12), px(12), px(12), px(12));
        column.addView(hud, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        levelView = new TextView(this);
        levelView.setTextSize(18);
        levelView.setContentDescription(getString(R.string.app_name) + " level");
        levelView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        hud.addView(levelView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        heartsView = new TextView(this);
        heartsView.setTextSize(18);
        heartsView.setGravity(Gravity.END);
        heartsView.setContentDescription("Moves remaining");
        hud.addView(heartsView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // ---- Board ----
        board = new GameView(this, this);
        column.addView(board, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---- Bottom controls ----
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(px(8), px(8), px(8), px(16));
        column.addView(controls, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addButton(controls, getString(R.string.undo), v -> onUndo());
        addButton(controls, getString(R.string.hint), v -> onHint());
        addButton(controls, getString(R.string.reset), v -> onReset());
        addButton(controls, getString(R.string.settings), v -> onSettings());
        return root;
    }

    private void addButton(LinearLayout parent, String label, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(label);
        b.setContentDescription(label);
        b.setAllCaps(false);
        b.setMinimumHeight(px(48));
        b.setOnClickListener(l);
        parent.addView(b, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    void showLevel(int n) {
        currentLevel = n;
        prefs.edit().putInt("currentLevel", n).apply();
        Game.Level lv = Game.generate(n);
        board.load(lv);
        setBoardState(board.game().stateString());
        updateHud();
    }

    void updateHud() {
        levelView.setText(String.format(getString(R.string.level_label), currentLevel));
        heartsView.setText("♥ " + board.game().hearts());
    }

    void haptic(int effectId) {
        if (!prefs.getBoolean("haptics", true)) return;
        Vibrator v = getSystemService(Vibrator.class);
        if (v == null) return;
        v.vibrate(VibrationEffect.createPredefined(effectId));
    }

    private void onUndo() {
        if (board.game().undo()) {
            board.announceUndo();
            setBoardState(board.game().stateString());
            updateHud();
        } else {
            board.announce(getString(R.string.undo) + ": nothing to undo");
        }
    }

    private void onHint() {
        int cell = board.game().findHintCell();
        if (cell >= 0) board.showHint(cell);
        else board.announce("No hint available");
    }

    private void onReset() {
        board.game().reset();
        board.invalidate();
        setBoardState(board.game().stateString());
        updateHud();
    }

    private void onSettings() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = px(24);
        box.setPadding(pad, pad, pad, pad);

        TextView about = new TextView(this);
        about.setText("Raazdar: Arrow Puzzle Game\n\nAn arrow logic puzzle by Raazdar — https://raazdar.xyz\n\n"
                + "Raazdar is an independent developer. This app is not affiliated with any other puzzle game.");
        about.setTextSize(15);
        box.addView(about);

        android.widget.CheckBox haptics = new android.widget.CheckBox(this);
        haptics.setText(getString(R.string.haptics));
        haptics.setChecked(prefs.getBoolean("haptics", true));
        haptics.setOnCheckedChangeListener((b, on) -> prefs.edit().putBoolean("haptics", on).apply());
        box.addView(haptics);

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings)
                .setView(box)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    void onWon() {
        prefs.edit().putInt("unlockedLevel", Math.max(currentLevel + 1, prefs.getInt("unlockedLevel", 1))).apply();
        haptic(VibrationEffect.EFFECT_DOUBLE_CLICK);
        new AlertDialog.Builder(this)
                .setTitle(R.string.win_title)
                .setMessage("Level " + currentLevel + " cleared! Every arrow escaped the board.")
                .setPositiveButton(R.string.next_level, (d, w) -> showLevel(currentLevel + 1))
                .setNegativeButton(R.string.reset, (d, w) -> { board.game().reset(); board.invalidate(); updateHud(); })
                .setCancelable(false)
                .show();
        board.announce(getString(R.string.win_title));
        board.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    void onFailed() {
        haptic(VibrationEffect.EFFECT_HEAVY_CLICK);
        new AlertDialog.Builder(this)
                .setTitle(R.string.fail_title)
                .setMessage("No hearts left. Restart the level or keep your cleared arrows and try again.")
                .setPositiveButton(R.string.retry, (d, w) -> { board.game().revive(); board.invalidate(); updateHud(); })
                .setNegativeButton(R.string.reset, (d, w) -> { board.game().reset(); board.invalidate(); updateHud(); })
                .setCancelable(false)
                .show();
    }

    /** Publish board state as the board's content description for accessibility & adb tests. */
    void setBoardState(String state) {
        board.setContentDescription("Board state " + state);
    }

    int px(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
