import java.awt.*;

public class Hero {
    int x, y;
    boolean left, right;
    boolean hit = false;
    long hitTimer = 0;
    int rectWidth = 50;
    int rectHeight = 20;
    private long lastHitTime = 0;

    public Hero(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        if (left && x > 0) x -= 5;
        if (right && x < 750) x += 5;
    }

    public void draw(Graphics g) {
        if (hit && System.currentTimeMillis() - hitTimer < 300) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.GREEN); // or whatever normal color you want
        }
        g.fillRect(x, y, rectWidth, rectHeight);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 50, 20);
    }

    public long getLastHitTime() {
        return lastHitTime;
    }

    public void setLastHitTime(long time) {
        this.lastHitTime = time;
    }

    public void takeHit() {
        hit = true;
        hitTimer = System.currentTimeMillis();
    }

    // Add these two methods to access y and height
    public int getY() {
        return y;
    }

    public int getHeight() {
        return rectHeight;
    }
}
