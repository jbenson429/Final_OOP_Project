import java.awt.*;

public abstract class Enemy {
    int x, y;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update();
    public abstract void draw(Graphics g);
    public Rectangle getBounds() {
        return new Rectangle(x,y, 40, 40);
    };
}
