import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ShootingEnemy extends Enemy {
    private int moveDirection = 1; // 1 = right, -1 = left
    private int health = 2;         // Takes 2 hits to destroy
    Random random = new Random();
    ArrayList<EnemyBullet> enemyBullets;

    public ShootingEnemy(int x, int y, ArrayList<EnemyBullet> enemyBullets) {
        super(x, y);
        this.enemyBullets = enemyBullets;
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
        g.setColor(Color.RED); // Color is red
        g.fillRect(x, y, 40, 40);
    }

    public void takeDamage() {
        health--;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }
}
