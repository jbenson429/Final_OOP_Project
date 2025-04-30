import javax.swing.*;
import java.awt.*;

public class CapturingEnemy extends Enemy {
    int originalX, originalY; // Original spawn position to return to after swoop
    boolean swooping = false; // Whether the enemy is currently swooping
    int swoopStage = 0;       // Current stage of swoop: 0 = idle, 1 = down, 2 = firing, 3 = return
    Hero hero;                // Reference to the hero, used to calculate swoop path
    boolean active = true;    // Flag to indicate if the enemy is still alive (not shot)
    int damage = 0;           // Tracking damage to the enemy
    long fireStartTime;       // Time when the beam starts firing
    private static final int FIRE_DURATION = 2000; // milliseconds
    private static final int REQUIRED_HITS = 4;   // Number of hits to destroy
    private long lastActionTime;
    private static final int REPEAT_DELAY = 7000; // Delay before repeating the process
    Image image;
    private final int WIDTH = 40;
    private final int HEIGHT = 40;

    public CapturingEnemy(int x, int y, Hero hero) {
        super(x, y); // Calls the parent Enemy constructor
        this.originalX = x;
        this.originalY = y;
        this.hero = hero;
        this.lastActionTime = System.currentTimeMillis(); // Set initial action time
        ImageIcon heroIcon = new ImageIcon("Sprites/Galaga.png");
        if (heroIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            image = heroIcon.getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        } else {
            System.err.println("Image not found!");
        }
    }

    @Override
    public void update() {
        long currentTime = System.currentTimeMillis();

        // Check if we need to repeat the process every 7 seconds
        if (currentTime - lastActionTime >= REPEAT_DELAY && !swooping) {
            swooping = true;  // Start swooping after the delay
            swoopStage = 1;   // Start with stage 1 (downward movement)
            lastActionTime = currentTime; // Reset the action time for the next repeat
        }

        if (swooping) {
            switch (swoopStage) {
                case 1: // Stage 1: Move downward toward hero
                    int heroBottom = hero.getY() + hero.getHeight(); // Bottom of hero
                    int desiredBeamEndY = heroBottom;
                    int beamHeight = 80; // New taller beam
                    int stopY = desiredBeamEndY - beamHeight; // Position to stop

                    if (y < stopY) {
                        y += 4; // Move down toward the hero
                    } else {
                        y = stopY; // Stop at the firing position
                        swoopStage = 2; // Proceed to firing stage
                        fireStartTime = currentTime; // Start the fire timer
                    }
                    break;

                case 2: // Stage 2: Fire the beam at the hero
                    if (currentTime - fireStartTime <= FIRE_DURATION) {
                        // The enemy fires the beam for a set duration
                        // Beam already exists in the draw method
                    } else {
                        swoopStage = 3; // Move to return stage once the beam stops
                    }
                    break;

                case 3: // Stage 3: Return to original position
                    x = Math.max(x - 4, originalX);
                    y = Math.max(y - 4, originalY);

                    // Once back to original position, reset swooping state
                    if (x == originalX && y == originalY) {
                        swooping = false;
                        swoopStage = 0; // Reset the swooping state
                    }
                    break;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (active) {
            if (image != null) {
                g.drawImage(image, x, y, null); // Draw the loaded image
            } else {
                // Fallback for if image fails to load
                // Get color based on the damage level
                g.setColor(getColorBasedOnDamage());
                g.fillRect(x, y, 40, 40); // 40x40 size for visual appearance
            }
        }

        // Stage 2: Drawing the beam when firing
        if (swoopStage == 2) {
            g.setColor(Color.CYAN); // Set the beam color
            int[] xPoints = {x + 20, x, x + 40}; // X coordinates of the triangle
            int[] yPoints = {y + 40, y + 80, y + 80}; // Y coordinates of the triangle
            g.fillPolygon(xPoints, yPoints, 3); // Drawing the beam as a triangle
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40); // Collision detection
    }

    public void destroy() {
        active = false; // Mark this enemy as inactive (used when shot)
    }

    public boolean isActive() {
        return active; // Check if the enemy is active
    }

    public boolean isSwooping() {
        return swooping; // Check if the enemy is currently swooping
    }

    public boolean beamHitsHero() {
        if (swoopStage != 2) return false;

        // Define the beam shape (triangle)
        Polygon beam = new Polygon(
                new int[]{x + 20, x, x + 40}, // Beam points
                new int[]{y + 40, y + 80, y + 80}, // Beam height
                3
        );

        return beam.intersects(hero.getBounds()); // Check if the beam hits the hero
    }

    public boolean isCapturing() {
        return swooping; // Return true if the enemy is swooping and in the process of capturing
    }

    // Take damage method
    public void takeDamage() {
        damage++; // Increment damage taken by 1
    }

    // Check if the enemy is destroyed
    public boolean isDestroyed() {
        return damage >= REQUIRED_HITS; // If damage is equal to or greater than the required hits, the enemy is destroyed
    }

    // Get color based on damage
    private Color getColorBasedOnDamage() {
        int red = 200;
        int green = 150;
        int blue = 200 - damage * 40; // Darkens with each hit
        blue = Math.max(blue, 50); // Ensure the blue value doesn't go too low

        return new Color(red, green, blue);
    }
}
