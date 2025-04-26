import java.awt.*;

public class EnemyBullet {
    int x, y;

    public EnemyBullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += 5;
    }

    public void draw(Graphics g) {
        g.setColor(Color.PINK);
        g.fillRect(x, y, 5, 10);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 5, 10);
    }


}
