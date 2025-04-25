import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Main game panel for a Galaga-style shooter game.
 * Implements game logic, rendering, and user input.
 */
public class PlayGame extends JPanel implements ActionListener, KeyListener {
    Timer timer;                            // Game loop timer
    Hero hero;                              // Player character
    ArrayList<Enemy> enemies = new ArrayList<>();          // List of enemies
    ArrayList<Laser> lasers = new ArrayList<>();           // Lasers fired by hero
    ArrayList<EnemyBullet> enemyBullets = new ArrayList<>(); // Bullets fired by enemies
    Random rand = new Random();             // Random number generator
    int lives = 3;                          // Player's remaining lives
    boolean gameOver = false;              // Game over state
    int wave = 1;                           // Current enemy wave
    long lastSwoopTime = 0;                 // Last time an enemy swooped
    boolean capturingEnemySwooping = false; // Flag to prevent multiple simultaneous swoops
    String message = "";                    // In-game message (like "Wave Complete")
    long messageTimer = 0;                  // Timer for how long message is shown

    /**
     * Constructor to set up the game panel and initialize game objects.
     */
    public PlayGame() {
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.addKeyListener(this);

        hero = new Hero(375, 500);  // Starting position of the hero
        spawnWave(wave);            // Spawn the first wave of enemies

        timer = new Timer(20, this); // 20ms delay (~50 FPS)
        timer.start();               // Start the game loop
    }

    /**
     * Spawns a new wave of enemies.
     * @param wave Current wave number, affects difficulty
     */
    public void spawnWave(int wave) {
        enemies.clear(); // Clear previous enemies
        EnemyFactory factory = new EnemyFactory();

        System.out.println("Spawning enemies for wave: " + wave);
        for (int i = 0; i < wave + 1; i++) {
            int SwoopSpawnX = 100 + i * 150;
            int SwoopSpawnY = 220;
            System.out.println(" - Enemy at x=" + SwoopSpawnX + ", y=" + SwoopSpawnY);
            enemies.add(factory.createEnemy("swooping", SwoopSpawnX, SwoopSpawnY, hero));
            // You can add other enemy types here (e.g., shooting, capturing)
        }
    }

    /**
     * Renders the game objects and UI.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        hero.draw(g);  // Draw hero

        // Draw enemies, lasers, and bullets
        for (Enemy enemy : enemies) enemy.draw(g);
        for (Laser laser : lasers) laser.draw(g);
        for (EnemyBullet bullet : enemyBullets) bullet.draw(g);

        // UI: lives and wave number
        g.setColor(Color.WHITE);
        g.drawString("Lives: " + lives, 10, 20);
        g.drawString("Wave: " + wave, 700, 20);

        // Display temporary message
        if (!message.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(message, 300, 280);
        }

        // Show game over screen
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", 300, 300);
        }
    }

    /**
     * Game loop logic executed on each timer tick.
     */
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        hero.update();  // Move hero based on input
        for (Laser laser : lasers) laser.update(); // Move lasers

        long currentTime = System.currentTimeMillis();

        // Swooping behavior every 6 seconds
        if (currentTime - lastSwoopTime > 6000) {
            ArrayList<SwoopingEnemy> eligible = new ArrayList<>();
            for (Enemy enemy : enemies) {
                if (enemy instanceof SwoopingEnemy se && se.isActive() && !se.isSwooping()) {
                    eligible.add(se);
                }
            }
            if (!eligible.isEmpty()) {
                SwoopingEnemy chosen = eligible.get(rand.nextInt(eligible.size()));
                chosen.startSwoop();
                lastSwoopTime = currentTime;
            }
        }

        // Clear message after 2 seconds
        if (!message.isEmpty() && currentTime - messageTimer > 2000) {
            message = "";
        }

        for (Enemy enemy : enemies) enemy.update(); // Move enemies

        // Check collision of bullets with hero
        for (int i = 0; i < enemyBullets.size(); i++) {
            EnemyBullet bullet = enemyBullets.get(i);
            bullet.update();
            if (bullet.getBounds().intersects(hero.getBounds())) {
                enemyBullets.remove(i);
                i--;
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    timer.stop();
                }
            }
        }

        // Remove off-screen lasers and bullets
        lasers.removeIf(laser -> laser.y < 0);
        enemyBullets.removeIf(bullet -> bullet.y > 600);

        // Check collision between hero lasers and enemies
        for (int i = 0; i < lasers.size(); i++) {
            Laser laser = lasers.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                if (laser.getBounds().intersects(enemy.getBounds())) {
                    lasers.remove(i);
                    enemies.remove(j);
                    i--;
                    break; // One collision per laser
                }
            }
        }

        // Advance to next wave if all enemies are defeated
        if (enemies.isEmpty()) {
            wave++;
            spawnWave(wave);
        }

        repaint(); // Re-render the screen
    }

    /**
     * Handle key press events for movement and firing.
     */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) hero.left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) hero.right = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver)
            lasers.add(new Laser(hero.x + 20, hero.y)); // Fire laser from hero's position
    }

    /**
     * Handle key release events to stop movement.
     */
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) hero.left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) hero.right = false;
    }

    public void keyTyped(KeyEvent e) {
        // Not used, required by KeyListener interface
    }

    /**
     * Main method to launch the game window.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Galaga Game");
        PlayGame game = new PlayGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
