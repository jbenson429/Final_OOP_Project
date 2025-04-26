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
    int score = 0;
    int wave = 1;                           // Current enemy wave
    long lastSwoopTime = 0;                 // Last time an enemy swooped
    boolean capturingEnemySwooping = false; // Flag to prevent multiple simultaneous swoops
    String message = "";                    // In-game message (like "Wave Complete")
    long messageTimer = 0;                  // Timer for how long message is shown
    final static int BOARD_WIDTH = 800;
    final static int BOARD_HEIGHT = 600;
    /**
     * Constructor to set up the game panel and initialize game objects.
     */
    public PlayGame() {
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
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
            int ShootSpawnX = 100 + (i-1) * 150;
            int ShootSpawnY = 120;
            if (i % 2 == 0) {
                if(SwoopSpawnX < BOARD_WIDTH - 50)
                {
                    System.out.println(" - SwoopEnemy at x=" + SwoopSpawnX + ", y=" + SwoopSpawnY);
                    enemies.add(factory.createEnemy("swooping", SwoopSpawnX, SwoopSpawnY, hero, enemyBullets));
                }

            } else if (i % 2 == 1)
            {
                if(SwoopSpawnX < BOARD_WIDTH - 50)
                {
                    System.out.println(" - ShootEnemy at x= " + ShootSpawnX + ", y= " + ShootSpawnY);
                    enemies.add(EnemyFactory.createEnemy("shooting", ShootSpawnX, ShootSpawnY, hero, enemyBullets));
                }
            }
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

        // UI: lives and wave number and Score
        g.setColor(Color.WHITE);
        g.drawString("Lives: " + lives, 10, 20);
        g.drawString("Wave: " + wave, 700, 20);
        g.drawString("Score: " + score, 400, 20);

        // Display temporary message
        if (!message.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(message, 300, 280);
        }

        // Show game over screen
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", 300, 300);
            g.drawString("Score: " + score, 300, 350);
            g.setFont(new Font("Arial", Font.PLAIN, 28));
            g.drawString("Press Enter to continue", 280, 400);
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

        // Check collision between hero and swooping enemies
        for (Enemy enemy : enemies) {
            if (enemy instanceof SwoopingEnemy) {
                if (hero.getBounds().intersects(enemy.getBounds())) {
                    long now = System.currentTimeMillis();
                    if (now - hero.getLastHitTime() > 1000) { // 1000 ms = 1 second cooldown
                        hero.takeHit(); // hero flashes or handles being hit
                        lives--;
                        hero.setLastHitTime(now);
                        if (lives <= 0) {
                            gameOver = true;
                            timer.stop();
                        }
                    }
                }
            }
        }

        // Check collision of bullets with hero
        for (int i = 0; i < enemyBullets.size(); i++) {
            EnemyBullet bullet = enemyBullets.get(i);
            bullet.update();
            if (bullet.getBounds().intersects(hero.getBounds())) {
                enemyBullets.remove(i);
                i--;
                long now = System.currentTimeMillis();
                if (now - hero.getLastHitTime() > 1000) { // 1000 ms = 1 second cooldown
                    hero.takeHit(); // hero flashes or handles being hit
                    lives--;
                    hero.setLastHitTime(now);
                    if (lives <= 0) {
                        gameOver = true;
                        timer.stop();
                    }
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
                    i--;

                    if (enemy instanceof ShootingEnemy) {
                        ShootingEnemy shootingEnemy = (ShootingEnemy) enemy;
                        shootingEnemy.takeDamage();
                        if (shootingEnemy.isDestroyed()) {
                            enemies.remove(j);
                            j--;
                            score += 100; // 100 points for shooting enemies
                        }
                    } else {
                        enemies.remove(j);
                        j--;
                        score += 50; // 50 points for swooping enemies
                    }

                    break; // laser can only hit one enemy
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
            lasers.add(new Laser(hero.x + 20, hero.y));// Fire laser from hero's position
        if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) resetGame(); // Restart the game
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
     * Method that resets everything to their base initialization
     * Restarts the game
     */
    public void resetGame() {
        hero = new Hero(375, 500);
        enemies.clear();
        lasers.clear();
        enemyBullets.clear();
        lives = 3;
        wave = 1;
        gameOver = false;
        score = 0;
        message = "";
        hero.setLastHitTime(System.currentTimeMillis());

        spawnWave(wave);
        timer.start();
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
