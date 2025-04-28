import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class PlayGame extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Hero hero;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Laser> lasers = new ArrayList<>();
    private ArrayList<EnemyBullet> enemyBullets = new ArrayList<>();
    private Random rand = new Random();
    private JTextField textField = new JTextField();
    private JButton startButton = new JButton("Start");

    private int lives = 3;
    private boolean gameOver = false;
    private boolean gameStart = false;
    private int score = 0;
    private int wave = 1;
    private long lastSwoopTime = 0;
    private String message = "";
    private long messageTimer = 0;
    private String playerName = "";

    public static final int BOARD_WIDTH = 800;
    public static final int BOARD_HEIGHT = 600;

    private String[] highScores = {"AAA00000", "BBB00000", "CCC00000"}; // default values

    public PlayGame() {
        setFocusable(true);
        setLayout(null);
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);

        hero = new Hero(375, 500);
        spawnWave(wave);

        // Set the textField for the name
        textField.setBounds(320, 350, 200,30);
        textField.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        this.add(textField);

        // Add the start button
        startButton.setBounds(375, 300, 100,30);
        startButton.addActionListener(e -> {  playerName = textField.getText().toUpperCase();
            // get the first three characters
            if (playerName.length() > 3) playerName = playerName.substring(0, 3);
            // Remove the textField and startButton when the game starts
            textField.setVisible(false);
            textField.setFocusable(false);
            startButton.setFocusable(false);
            startButton.setVisible(false);

            // Start the game
            gameStart = true;
            timer = new Timer(20, this);
            timer.start();});

        this.add(startButton);

    }

    private void spawnWave(int wave) {
        enemies.clear();
        EnemyFactory factory = new EnemyFactory();
        System.out.println("Spawning enemies for wave: " + wave);

        boolean capturingEnemySpawned = false;

        for (int i = 0; i < wave + 1; i++) {
            int swoopX = 100 + i * 150;
            int swoopY = 220;
            int shootX = 100 + (i - 1) * 150;
            int shootY = 120;

            if (i % 2 == 0 && swoopX < BOARD_WIDTH - 50) {
                System.out.println(" - SwoopEnemy at x=" + swoopX + ", y=" + swoopY);
                enemies.add(factory.createEnemy("swooping", swoopX, swoopY, hero, enemyBullets));
            } else if (i % 2 == 1 && shootX < BOARD_WIDTH - 50) {
                System.out.println(" - ShootEnemy at x=" + shootX + ", y=" + shootY);
                enemies.add(factory.createEnemy("shooting", shootX, shootY, hero, enemyBullets));
            }

            if (!capturingEnemySpawned) {
                int captureX = BOARD_WIDTH / 2 - 20;
                int captureY = 50;
                System.out.println(" - CapturingEnemy at x=" + captureX + ", y=" + captureY);
                enemies.add(factory.createEnemy("capturing", captureX, captureY, hero, enemyBullets));
                capturingEnemySpawned = true;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        hero.draw(g);

        for (Enemy enemy : enemies) enemy.draw(g);
        for (Laser laser : lasers) laser.draw(g);
        for (EnemyBullet bullet : enemyBullets) bullet.draw(g);

        g.setColor(Color.WHITE);
        g.drawString("Lives: " + lives, 10, 20);
        g.drawString("Wave: " + wave, 700, 20);
        g.drawString("Score: " + score, 400, 20);

        if(!gameStart) {
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Name:",250,375);
        }

        if (!message.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(message, 300, 280);
        }

        if (gameOver) {
            // Display text for the end of the game
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", 300, 300);
            g.drawString("Score: " + score, 300, 350);
            g.setFont(new Font("Arial", Font.PLAIN, 28));
            g.drawString("Press Enter to continue", 280, 400);

            g.setFont(new Font("Arial", Font.PLAIN, 18));
            for (int i = 0; i < highScores.length; i++) {
                g.drawString(highScores[i], 100, 200 + (i * 50));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        hero.update();
        for (Laser laser : lasers) laser.update();
        long currentTime = System.currentTimeMillis();

        boolean capturingEnemyActive = false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof CapturingEnemy ce && ce.isCapturing()) {
                capturingEnemyActive = true;
                break;
            }
        }

        if (!capturingEnemyActive && currentTime - lastSwoopTime > 6000) {
            ArrayList<SwoopingEnemy> eligible = new ArrayList<>();
            for (Enemy enemy : enemies) {
                if (enemy instanceof SwoopingEnemy se && se.isActive() && !se.isSwooping()) {
                    eligible.add(se);
                }
            }
            if (!eligible.isEmpty()) {
                eligible.get(rand.nextInt(eligible.size())).startSwoop();
                lastSwoopTime = currentTime;
            }
        }

        if (!message.isEmpty() && currentTime - messageTimer > 2000) {
            message = "";
        }

        for (Enemy enemy : enemies) enemy.update();

        checkCollisions();

        lasers.removeIf(laser -> laser.y < 0);
        enemyBullets.removeIf(bullet -> bullet.y > BOARD_HEIGHT);

        if (enemies.isEmpty()) {
            wave++;
            spawnWave(wave);
        }

        repaint();
    }

    private void checkCollisions() {
        long now = System.currentTimeMillis();

        for (Enemy enemy : enemies) {
            if (enemy instanceof SwoopingEnemy && hero.getBounds().intersects(enemy.getBounds())) {
                handleHeroHit(now);
            } else if (enemy instanceof CapturingEnemy ce && ce.beamHitsHero()) {
                handleHeroHit(now);
            }
        }

        for (int i = 0; i < enemyBullets.size(); i++) {
            EnemyBullet bullet = enemyBullets.get(i);
            bullet.update();
            if (bullet.getBounds().intersects(hero.getBounds())) {
                enemyBullets.remove(i);
                handleHeroHit(now);
                break;
            }
        }

        for (int i = 0; i < lasers.size(); i++) {
            Laser laser = lasers.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                if (laser.getBounds().intersects(enemy.getBounds())) {
                    lasers.remove(i);
                    i--;

                    if (enemy instanceof ShootingEnemy se) {
                        se.takeDamage();
                        if (se.isDestroyed()) {
                            enemies.remove(j);
                            score += 100;
                        }
                    } else if (enemy instanceof CapturingEnemy ce) {
                        ce.takeDamage();
                        if (ce.isDestroyed()) {
                            enemies.remove(j);
                            score += 150;
                        }
                    } else {
                        enemies.remove(j);
                        score += 50;
                    }
                    break;
                }
            }
        }
    }

    private void handleHeroHit(long now) {
        if (now - hero.getLastHitTime() > 1000) {
            hero.takeHit();
            lives--;
            hero.setLastHitTime(now);
            if (lives <= 0) {
                gameOver = true;
                timer.stop();
                repaint();
                try {
                    highScores = afterGame();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) hero.left = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) hero.right = true;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) lasers.add(new Laser(hero.x + 23, hero.y));
        if (e.getKeyCode() == KeyEvent.VK_ENTER && gameOver) resetGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) hero.left = false;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) hero.right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void resetGame() {
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

    private String[] afterGame() throws IOException {
        Path highScoresPath = Path.of("HighScores.txt");

        // Step 1: Check if file exists
        if (!Files.exists(highScoresPath)) {
            // If not, create it with 3 default scores
            Files.writeString(highScoresPath, "AAA000\nAAA000\nAAA000\n");
        }

        // Step 2: Read contents
        String contents = Files.readString(highScoresPath);
        String[] lines = contents.split("\n");
        String[] names = new String[3];
        String[] scores = new String[3];

        for (int i = 0; i < 3; i++) {
            names[i] = lines[i].substring(0, 3);
            scores[i] = lines[i].substring(3);
        }

        // Step 3: Update scores
        int tempScore = score;
        String tempName = playerName;
        for (int i = 0; i < 3; i++) {
            if (tempScore > Integer.parseInt(scores[i])) {
                int scoreSwap = Integer.parseInt(scores[i]);
                String nameSwap = names[i];
                scores[i] = Integer.toString(tempScore);
                names[i] = tempName;
                tempScore = scoreSwap;
                tempName = nameSwap;
            }
        }

        // Step 4: Write back new high scores
        String[] result = new String[3];
        for (int i = 0; i < 3; i++) {
            result[i] = names[i] + scores[i];
        }

        try (FileWriter writer = new FileWriter("HighScores.txt")) {
            for (String s : result) {
                writer.write(s + "\n");
            }
        }

        return result;
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Galaga Game");
        PlayGame game = new PlayGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
