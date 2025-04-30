import javax.swing.*;
import java.awt.*;

public class Hero {
    int x, y;
    boolean left, right;
    boolean hit = false;
    long hitTimer = 0;
    Image image;
    private final int WIDTH = 50;
    private final int HEIGHT = 53;
    private long lastHitTime = 0;

    public Hero(int x, int y) {
        this.x = x;
        this.y = y;
        ImageIcon heroIcon = new ImageIcon("Sprites/Player.png");
        if (heroIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            image = heroIcon.getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        } else {
            System.err.println("Image not found!");
        }
    }

    public void update() {
        if (left && x > 0) x -= 5;
        if (right && x < 750) x += 5;
    }

    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, null); // Draw the loaded image
        } else {
            // Fallback for if image fails to load
            if (hit && System.currentTimeMillis() - hitTimer < 300) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GREEN); // or whatever normal color you want
            }
            g.fillRect(x, y, 50, 20);
        }
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
        if(image != null) {
            return HEIGHT;
        }
        else{
            return 20;
        }
    }
}
