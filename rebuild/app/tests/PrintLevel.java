import com.raazdar.arrowpuzzlegame.Game;

public class PrintLevel {
    public static void main(String[] args) {
        for (int n = 1; n <= 2; n++) {
            Game.Level l = Game.generate(n);
            System.out.println("LEVEL " + n + " " + new Game(l).stateString());
            for (int idx : l.removalOrder) {
                Game.Arrow ar = l.arrows.get(idx);
                int cell = ar.y * l.width + ar.x;
                System.out.println("tap cell " + cell + " (x=" + ar.x + ", y=" + ar.y + ", dir=" + ar.dir + ")");
            }
        }
    }
}
