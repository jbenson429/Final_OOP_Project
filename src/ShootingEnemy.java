import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ShootingEnemy extends Enemy {
    private int moveDirection = 1; // 1 = right, -1 = left
    private int health = 2;         // Takes 2 hits to destroy
    private Color currentColor;     // The color of the enemy
    Random random = new Random();
    ArrayList<EnemyBullet> enemyBullets;
    Image image;
    private final int WIDTH = 40;
    private final int HEIGHT = 40;

    public ShootingEnemy(int x, int y, ArrayList<EnemyBullet> enemyBullets) {
        super(x, y);
        this.enemyBullets = enemyBullets;
        this.currentColor = Color.RED; // Initial color is red
        ImageIcon heroIcon = new ImageIcon("Sprites/Galaxian.png");
        if (heroIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            image = heroIcon.getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        } else {
            System.err.println("Image not found!");
        }
    }

    @Override
    public void update() {
        // Optional: make them wiggle a little bit
        x += moveDirection * 2;

        // Bounce back if reaching screen sides
        if (x <= 0 || x >= 750) {
            moveDirection *= -1;
        }

        // Random chance to shoot
        if (random.nextInt(100) < 2) { // ~2% chance every frame
            enemyBullets.add(new EnemyBullet(x + 20, y + 20));
        }
    }

    @Override
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, null); // Draw the loaded image
        } else {
            // Fallback for if image fails to load
            g.setColor(currentColor); // Use the current color
            g.fillRect(x, y, 40, 40);
        }
    }

    public void takeDamage() {
        health--;
        if (health == 1) {
            currentColor = new Color(128, 0, 0); // Deep maroon color after 1 shot
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }
}
