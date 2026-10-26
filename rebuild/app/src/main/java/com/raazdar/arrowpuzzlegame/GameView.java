package com.raazdar.arrowpuzzlegame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Board renderer + touch controller. Draws the puzzle grid and arrows, animates
 * escapes, shakes on blocked taps, and displays hint highlights.
 */
public final class GameView extends View {

    private final MainActivity host;
    private Game game;

    private float cell = 64f;
    private float boardLeft, boardTop;

    private static final long EXIT_MS = 220;
    private final List<int[]> exiting = new ArrayList<>(); // {x, y, dir, startMs}
    private int shakeCell = -1;
    private long shakeStartMs;
    private int hintCell = -1;
    private long hintStartMs;

    private final Paint cellBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cellLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowBody = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowHead = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GameView(Context c, MainActivity activity) {
        super(c);
        this.host = activity;
        cellBg.setColor(0xFF2E3949);
        cellLine.setColor(0xFFFFFFFF);
        arrowHead.setColor(0xFFE07429);
        arrowBody.setColor(0xFFE07429);
        hintPaint.setColor(0x664CAF50);
        hintPaint.setStyle(Paint.Style.FILL);
        shakePaint.setColor(0x55E53935);
        shakePaint.setStyle(Paint.Style.FILL);
        setBackgroundColor(0xFFF4EFE6);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    void load(Game.Level level) {
        game = new Game(level);
        exiting.clear();
        hintCell = -1;
        shakeCell = -1;
        requestLayout();
        invalidate();
        announce("Level " + boardDescription());
    }

    Game game() { return game; }

    private String boardDescription() {
        return game.arrowCount() + " arrows, " + game.width() + " by " + game.height() + " grid";
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (game == null) return;
        cell = Math.max(40f, Math.min((w - 48f) / game.width(), (h - 200f) / game.height()));
        boardLeft = (w - cell * game.width()) / 2f;
        boardTop = Math.max(24f, (h - cell * game.height()) / 2f - 40f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (game == null) return;
        RectF r = new RectF();
        // grid cells
        for (int y = 0; y < game.height(); y++) {
            for (int x = 0; x < game.width(); x++) {
                float cx = boardLeft + x * cell, cy = boardTop + y * cell;
                r.set(cx + 2, cy + 2, cx + cell - 2, cy + cell - 2);
                canvas.drawRoundRect(r, 10, 10, cellBg);

                int hash = y * game.width() + x;
                long now = android.os.SystemClock.uptimeMillis();
                if (hash == hintCell && now - hintStartMs < 2500) {
                    canvas.drawRoundRect(r, 10, 10, hintPaint);
                }
                if (hash == shakeCell && now - shakeStartMs < 260) {
                    canvas.drawRoundRect(r, 10, 10, shakePaint);
                }

                int dir = exitingDirFor(hash, now);
                float off = exitingOffsetFor(hash, now);
                if (dir >= 0 && off > 0f) {
                    if (off < 1f) drawArrow(canvas, cx, cy, dir, off);
                } else {
                    int d = game.arrowDirAt(hash);
                    if (d >= 0) drawArrow(canvas, cx, cy, d, 0f);
                }
            }
        }
        if (!exiting.isEmpty()) postInvalidateOnAnimation();
        // clear expired
        exiting.removeIf(e -> android.os.SystemClock.uptimeMillis() - e[3] > EXIT_MS + 16);
    }

    private int exitingDirFor(int hash, long now) {
        for (int[] e : exiting) if (e[0] == hash) return e[2];
        return -1;
    }

    private float exitingOffsetFor(int hash, long now) {
        for (int[] e : exiting) if (e[0] == hash) {
            float t = (now - e[3]) / (float) EXIT_MS;
            return Math.min(1f, t);
        }
        return -1f;
    }

    private void drawArrow(Canvas c, float cx, float cy, int dir, float off) {
        // simple arrow glyph: shaft + head, moved outward when escaping
        float s = cell * 0.34f, ox = 0, oy = 0;
        switch (dir) {
            case Game.DIR_UP:    ox = 0;  oy = -off * cell * 1.6f; break;
            case Game.DIR_DOWN:  ox = 0;  oy = off * cell * 1.6f;  break;
            case Game.DIR_LEFT:  ox = -off * cell * 1.6f; oy = 0; break;
            default:             ox = off * cell * 1.6f;  oy = 0; break;
        }
        cx += ox; cy += oy;
        android.graphics.Path p = new android.graphics.Path();
        switch (dir) {
            case Game.DIR_UP:
                p.moveTo(cx, cy - s); p.lineTo(cx + s, cy + s); p.lineTo(cx - s, cy + s); p.close(); break;
            case Game.DIR_DOWN:
                p.moveTo(cx, cy + s); p.lineTo(cx + s, cy - s); p.lineTo(cx - s, cy - s); p.close(); break;
            case Game.DIR_LEFT:
                p.moveTo(cx - s, cy); p.lineTo(cx + s, cy + s); p.lineTo(cx + s, cy - s); p.close(); break;
            default:
                p.moveTo(cx + s, cy); p.lineTo(cx - s, cy + s); p.lineTo(cx - s, cy - s); p.close(); break;
        }
        arrowHead.setStyle(Paint.Style.FILL);
        c.drawPath(p, arrowHead);
        c.save();
        c.restore();
    }

    @Override
    public boolean performClick() { return super.performClick(); }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() != android.view.MotionEvent.ACTION_DOWN) return false;
        if (game == null) return true;
        float x = (ev.getX() - boardLeft) / cell;
        float y = (ev.getY() - boardTop) / cell;
        int gx = (int) x, gy = (int) y;
        int cellHash = gy * game.width() + gx;
        int dir = game.arrowDirAt(cellHash);
        if (dir < 0) return true;

        // ignore multi-touch race conditions per spec
        if (ev.getPointerCount() > 1) return true;

        Game.TapResult res = game.tap(gx, gy);
        switch (res) {
            case ESCAPED:
                exiting.add(new int[]{cellHash, 0, dir, (int) android.os.SystemClock.uptimeMillis()});
                host.haptic(android.os.VibrationEffect.EFFECT_CLICK);
                performClick();
                host.updateHud();
                if (game.isWon()) host.onWon();
                host.setBoardState(game.stateString());
                invalidate();
                announce(boardDescription());
                break;
            case BLOCKED:
            case GAME_OVER:
                shakeCell = cellHash; shakeStartMs = android.os.SystemClock.uptimeMillis();
                host.haptic(android.os.VibrationEffect.EFFECT_HEAVY_CLICK);
                host.updateHud();
                if (game.isFailed()) host.onFailed();
                host.setBoardState(game.stateString());
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    void showHint(int cell) {
        hintCell = cell;
        hintStartMs = android.os.SystemClock.uptimeMillis();
        invalidate();
        announce("Hint: the highlighted arrow has a clear path");
    }

    void announceUndo() {
        invalidate();
        announce("Undo applied");
    }

    void announce(String text) {
        AccessibilityManager am = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null && am.isEnabled()) {
            android.view.accessibility.AccessibilityEvent e = android.view.accessibility.AccessibilityEvent.obtain(
                    android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT);
            e.getText().add(text);
            am.sendAccessibilityEvent(e);
        }
    }
}
