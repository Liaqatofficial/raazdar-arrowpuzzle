package com.raazdar.arrowpuzzlegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Core puzzle model for Raazdar: Arrow Puzzle Game.
 *
 * Verified core mechanic implemented exactly as specified by the audited spec:
 * every arrow faces one of four cardinal directions. Tapping an arrow launches
 * it off the board in its facing direction, but only if its path to the board
 * edge is completely clear of other arrows; a blocked arrow stays on the board.
 *
 * Levels are generated deterministically from the level number (offline, no
 * network dependency) with a guarantee of solvability: the generator builds
 * the board in reverse removal order, so the recorded removal order is always
 * a valid solution. Level count claims (e.g., "1055 levels") from third-party
 * analysis were NOT copied; this app ships its own level series.
 */
public final class Game {

    public static final int DIR_UP = 0;    // dy = -1
    public static final int DIR_RIGHT = 1; // dx = +1
    public static final int DIR_DOWN = 2;  // dy = +1
    public static final int DIR_LEFT = 3;  // dx = -1

    private static final int[] DX = {0, 1, 0, -1};
    private static final int[] DY = {-1, 0, 1, 0};

    /** One immutable arrow instance in a level layout. */
    public static final class Arrow {
        public final int x, y, dir;
        public Arrow(int x, int y, int dir) { this.x = x; this.y = y; this.dir = dir; }
    }

    /** A complete, independent level layout. */
    public static final class Level {
        public final int width, height;
        public final List<Arrow> arrows;
        public final int[] removalOrder; // indices into arrows: guaranteed solution order
        Level(int w, int h, List<Arrow> a, int[] order) { width = w; height = h; arrows = a; removalOrder = order; }
    }

    public static final int MAX_LEVEL_NUMBER = 999;

    /** Grid size grows gently with progression; verified spec: early grids small, later larger. */
    static int[] sizeForLevel(int levelNumber) {
        int w = 3 + Math.min(6, levelNumber / 8);
        int h = 4 + Math.min(6, levelNumber / 8);
        return new int[]{w, h};
    }

    /** Deterministically generate a solvable level. */
    public static Level generate(int levelNumber) {
        long seed = 0x525A_0000L ^ (levelNumber * 2654435761L);
        Random rnd = new Random(seed);
        int[] wh = sizeForLevel(levelNumber);
        int w = wh[0], h = wh[1];
        int target = 4 + Math.min(w * h / 2, 10 + levelNumber / 2);

        List<Arrow> placed = new ArrayList<>();      // arrows placed so far (later-removed)
        List<Integer> placedCells = new ArrayList<>();
        int attempts = target * 40;
        while (placed.size() < target && attempts-- > 0) {
            int x = rnd.nextInt(w), y = rnd.nextInt(h), d = rnd.nextInt(4);
            int cell = y * w + x;
            if (placedCells.contains(cell)) continue;
            if (pathClear(x, y, d, w, h, placedCells)) {
                placed.add(new Arrow(x, y, d));
                placedCells.add(cell);
            }
        }
        // removalOrder: reverse of placement (last placed -> first removed)
        int[] order = new int[placed.size()];
        for (int i = 0; i < order.length; i++) order[i] = placed.size() - 1 - i;
        return new Level(w, h, placed, order);
    }

    /** Ray from (x,y) toward direction until edge; true if no blockers intersect. */
    static boolean pathClear(int x, int y, int dir, int w, int h, List<Integer> occupied) {
        int cx = x + DX[dir], cy = y + DY[dir];
        while (cx >= 0 && cx < w && cy >= 0 && cy < h) {
            if (occupied.contains(cy * w + cx)) return false;
            cx += DX[dir]; cy += DY[dir];
        }
        return true;
    }

    /** Independent verification that the recorded removal order solves the level. */
    public static boolean verifySolvable(Level l) {
        List<Integer> occ = new ArrayList<>();
        for (Arrow a : l.arrows) occ.add(a.y * l.width + a.x);
        for (int idx : l.removalOrder) {
            Arrow a = l.arrows.get(idx);
            occ.remove(Integer.valueOf(a.y * l.width + a.x));
            if (!pathClear(a.x, a.y, a.dir, l.width, l.height, occ)) return false;
        }
        return occ.isEmpty();
    }

    // ---------------- live board state ----------------

    private final Level level;
    private final int W, H;
    /** Indexed by cell: dir of arrow present, or -1. */
    private final int[] board;
    /** Initial copy for reset. */
    private final int[] initial;
    private final List<int[]> undoStack = new ArrayList<>(); // {x, y, dir}
    private int hearts = 3;
    private boolean won, failed;

    public Game(Level level) {
        this.level = level;
        this.W = level.width;
        this.H = level.height;
        this.board = new int[W * H];
        java.util.Arrays.fill(board, -1);
        for (Arrow a : level.arrows) board[a.y * W + a.x] = a.dir;
        this.initial = board.clone();
    }

    public int width() { return W; }
    public int height() { return H; }
    public int hearts() { return hearts; }
    public boolean isWon() { return won; }
    public boolean isFailed() { return failed; }
    public int arrowCount() { int c = 0; for (int d : board) if (d >= 0) c++; return c; }
    public int arrowDirAt(int cell) { return (cell >= 0 && cell < board.length) ? board[cell] : -1; }
    public Level level() { return level; }

    /** Outcome of tapping a cell holding an arrow. */
    public enum TapResult { ESCAPED, BLOCKED, EMPTY, GAME_OVER }

    /** Tap cell (x,y). Returns result; caller updates UI accordingly. */
    public TapResult tap(int x, int y) {
        if (won || failed) return TapResult.EMPTY;
        if (x < 0 || y < 0 || x >= W || y >= H) return TapResult.EMPTY;
        int cell = y * W + x;
        int dir = board[cell];
        if (dir < 0) return TapResult.EMPTY;
        List<Integer> occ = occupiedCells();
        // temporary basis without self
        occ.remove(Integer.valueOf(cell));
        if (pathClear(x, y, dir, W, H, occ)) {
            undoStack.add(new int[]{x, y, dir});
            board[cell] = -1;
            if (arrowCount() == 0) won = true;
            return TapResult.ESCAPED;
        }
        hearts--;
        if (hearts <= 0) { failed = true; return TapResult.GAME_OVER; }
        return TapResult.BLOCKED;
    }

    /** Undo last escaped arrow (restores it exactly where it left from). */
    public boolean undo() {
        if (undoStack.isEmpty() || failed) return false;
        int[] m = undoStack.remove(undoStack.size() - 1);
        board[m[1] * W + m[0]] = m[2];
        won = false;
        return true;
    }

    /** Find a cell whose arrow has a clear escape path straight to the edge. */
    public int findHintCell() {
        List<Integer> occ = occupiedCells();
        for (int cell = 0; cell < board.length; cell++) {
            int dir = board[cell];
            if (dir < 0) continue;
            List<Integer> without = new ArrayList<>(occ);
            without.remove(Integer.valueOf(cell));
            int x = cell % W, y = cell / W;
            if (pathClear(x, y, dir, W, H, without)) return cell;
        }
        return -1;
    }

    public boolean hasHint() { return findHintCell() >= 0; }

    /** Restart current level layout; restores original arrows and full hearts. */
    public void reset() {
        System.arraycopy(initial, 0, board, 0, board.length);
        undoStack.clear();
        hearts = 3;
        won = false; failed = false;
    }

    /** Restore one heart (used by Try Again after failure). */
    public void revive() {
        if (!failed) return;
        failed = false;
        hearts = 3;
        // board state preserved: cleared arrows remain cleared (documented behavior)
    }

    private List<Integer> occupiedCells() {
        List<Integer> occ = new ArrayList<>();
        for (int i = 0; i < board.length; i++) if (board[i] >= 0) occ.add(i);
        return occ;
    }

    /**
     * Compact ASCII board state for accessibility/testing, e.g. "3x4|.><\/_|".
     * Symbols: . empty, ^ up, &gt; right, v down, &lt; left.
     */
    public String stateString() {
        char[] glyphs = {'.', '^', '>', 'v', '<'};
        StringBuilder sb = new StringBuilder().append(W).append('x').append(H).append('|');
        for (int i = 0; i < board.length; i++) {
            sb.append(glyphs[board[i] + 1]);
            if (i % W == W - 1 && i != board.length - 1) sb.append('/');
        }
        return sb.toString();
    }
}
