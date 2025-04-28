import java.awt.*;

public class Laser {
    int x, y;

    public Laser(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y -= 10;
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 4, 10);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 4, 10);
    }
}
