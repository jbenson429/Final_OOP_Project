import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

/**
 * Main game panel for a Galaga-style shooter game.
 * Implements game logic, rendering, and user input.
 */
public class PlayGame extends JPanel implements ActionListener, KeyListener{
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
    private boolean capturingEnemyActive = false; // tracks state of capturingEnemy
    String[] highScores = new String[3];
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

        boolean capturingEnemySpawned = false; // Only spawn one CapturingEnemy

        for (int i = 0; i < wave + 1; i++) {
            int SwoopSpawnX = 100 + i * 150;
            int SwoopSpawnY = 220;
            int ShootSpawnX = 100 + (i - 1) * 150;
            int ShootSpawnY = 120;

            if (i % 2 == 0) {
                if (SwoopSpawnX < BOARD_WIDTH - 50) {
                    System.out.println(" - SwoopEnemy at x=" + SwoopSpawnX + ", y=" + SwoopSpawnY);
                    enemies.add(factory.createEnemy("swooping", SwoopSpawnX, SwoopSpawnY, hero, enemyBullets));
                }
            } else if (i % 2 == 1) {
                if (ShootSpawnX < BOARD_WIDTH - 50) {
                    System.out.println(" - ShootEnemy at x= " + ShootSpawnX + ", y= " + ShootSpawnY);
                    enemies.add(factory.createEnemy("shooting", ShootSpawnX, ShootSpawnY, hero, enemyBullets));
                }
            }

            // Spawn one capturing enemy at the start of each wave
            if (!capturingEnemySpawned) {
                int captureX = BOARD_WIDTH / 2 - 20; // Centered
                int captureY = 50;
                System.out.println(" - CapturingEnemy at x=" + captureX + ", y=" + captureY);
                enemies.add(factory.createEnemy("capturing", captureX, captureY, hero, enemyBullets));
                capturingEnemySpawned = true;
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

        // UI: lives and wave number
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
            //Draw the high scores
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString(highScores[0], 100, 200);
            g.drawString(highScores[1], 100, 250);
            g.drawString(highScores[2], 100, 300);
        }
    }

    /**
     * Game loop logic executed on each timer tick.
     */
    public void actionPerformed(ActionEvent e){
        if (gameOver) return;

        hero.update();  // Move hero based on input
        for (Laser laser : lasers) laser.update(); // Move lasers

        long currentTime = System.currentTimeMillis();

        // --- Capturing Enemy Priority ---
        boolean capturingEnemyActive = false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof CapturingEnemy ce) {
                if (ce.isCapturing()) {
                    capturingEnemyActive = true;
                    break;
                }
            }
        }

        // --- Swooping behavior (only if no capturing enemy is active) ---
        if (!capturingEnemyActive && currentTime - lastSwoopTime > 6000) {
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

        // Update all enemies
        for (Enemy enemy : enemies) enemy.update();

        // --- Check collision between hero and swooping enemies ---
        for (Enemy enemy : enemies) {
            if (enemy instanceof SwoopingEnemy) {
                if (hero.getBounds().intersects(enemy.getBounds())) {
                    long now = System.currentTimeMillis();
                    if (now - hero.getLastHitTime() > 1000) { // 1 second cooldown
                        hero.takeHit();
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

        // --- Check collision between hero and capturing enemy beams ---
        for (Enemy enemy : enemies) {
            if (enemy instanceof CapturingEnemy ce) {
                if (ce.beamHitsHero()) {
                    long now = System.currentTimeMillis();
                    if (now - hero.getLastHitTime() > 1000) { // Same 1 second cooldown
                        hero.takeHit();
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

        // --- Check collision of enemy bullets with hero ---
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
                        try {
                            highScores = afterGame();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        timer.stop();

                    }
                }
            }
        }

        // --- Remove off-screen lasers and bullets ---
        lasers.removeIf(laser -> laser.y < 0);
        enemyBullets.removeIf(bullet -> bullet.y > 600);

        // --- Check collision between hero lasers and enemies ---
        for (int i = 0; i < lasers.size(); i++) {
            Laser laser = lasers.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                if (laser.getBounds().intersects(enemy.getBounds())) {
                    lasers.remove(i);
                    i--;

                    if (enemy instanceof ShootingEnemy shootingEnemy) {
                        shootingEnemy.takeDamage();
                        if (shootingEnemy.isDestroyed()) {
                            enemies.remove(j);
                            j--;
                            score += 100; // 100 points for shooting enemies
                        }
                    } else if (enemy instanceof CapturingEnemy capturingEnemy) {
                        capturingEnemy.takeDamage();
                        if (capturingEnemy.isDestroyed()) {
                            enemies.remove(j);
                            j--;
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

        // --- Advance to next wave if all enemies are defeated ---
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


    public String[] afterGame() throws IOException {
    String contents  = Files.readString(Path.of("HighScores.txt"));
    String[] names = contents.split("\n");
    String[] scores = new String[3];
    for (int i = 0; i < scores.length; i++) {
        scores[i] = names[i].replace(names[i].substring(0,8), "");
        names[i] = names[i].replace(names[i].substring(8), "");
    }
        int temp = this.score;
    for (int i = 0; i < scores.length; i++) {

        if (temp > Integer.parseInt(scores[i])) {
            String temp2 = scores[i];
            scores[i] = Integer.toString(temp);
            temp = Integer.parseInt(temp2);
        }
    }
    String[] scoreNames = new String[scores.length];
    for (int i = 0; i < scores.length; i++) {
        scoreNames[i] = names[i] + scores[i];
    }

    FileWriter writer = new FileWriter("HighScores.txt");
    writer.write(scoreNames[0] + "\n");
    writer.write(scoreNames[1] + "\n");
    writer.write(scoreNames[2] + "\n");
    writer.close();
    return scoreNames;
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
