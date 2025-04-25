import java.awt.*;

public class Hero {
    int x, y;
    boolean left, right;

    public Hero(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        if (left && x > 0) x -= 5;
        if (right && x < 750) x += 5;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, 50, 20);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 50, 20);
    }
}
