import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Represents a special type of enemy that performs a swooping maneuver toward the hero.
 */
public class SwoopingEnemy extends Enemy {
    int originalX, originalY; // Original spawn position to return to after swoop
    boolean swooping = false; // Whether the enemy is currently swooping
    int swoopStage = 0;     // Current stage of swoop: 0 = idle, 1 = down, 2 = right, 3 = return
    Hero hero;              // Reference to the hero, used to calculate swoop path
    Image image;
    boolean active = true;  // Flag to indicate if the enemy is still alive (not shot)
    private final int WIDTH = 40;
    private final int HEIGHT = 40;
    Random rand = new Random();       // Used to get the randomized enemy

    /**
     * Constructs a swooping enemy at the given position.
     * @param x Horizontal position
     * @param y Vertical position
     * @param hero Reference to the hero for targeting
     */
    public SwoopingEnemy(int x, int y, Hero hero) {
        super(x, y); // Calls the parent Enemy constructor
        this.originalX = x;
        this.originalY = y;
        this.hero = hero;

        ImageIcon swoopingIcon;
        int randNum = rand.nextInt(1, 11); // for getting a variety of enemies
        if ((randNum % 2) == 0) {
            swoopingIcon = new ImageIcon("Final_OOP_Project-master/Sprites/Wasp.png");
        }
        else {
            swoopingIcon = new ImageIcon("Final_OOP_Project-master/Sprites/Butterfly.png");
        }

        if (swoopingIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            image = swoopingIcon.getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
        } else {
            System.err.println("Image not found!");
        }
    }

    /**
     * Starts the swooping action if not already in progress.
     */
    public void startSwoop() {
        if (!swooping && active) {
            swooping = true;
            swoopStage = 1; // Begin with stage 1 (downward movement)
        }
    }

    /**
     * Updates the swooping movement based on the current swoop stage.
     * Called on each game loop update.
     */
    public void update() {
        if (swooping) {
            switch (swoopStage) {
                case 1: // Stage 1: Move downward toward hero
                    if (y < hero.y + 50)
                        y += 4;
                    else
                        swoopStage = 2; // Proceed to next stage
                    break;

                case 2: // Stage 2: Move right past the hero
                    x += 4;
                    if (x > hero.x + 100)
                        swoopStage = 3; // Proceed to return stage
                    break;

                case 3: // Stage 3: Return to original position
                    x = Math.max(x - 4, originalX);
                    y = Math.max(y - 4, originalY);

                    // Once back to original position, reset swooping state
                    if (x == originalX && y == originalY) {
                        swooping = false;
                        swoopStage = 0;
                    }
                    break;
            }
        }
    }

    /**
     * Renders the enemy as a green square.
     * @param g Graphics object for drawing
     */
    public void draw(Graphics g) {
        if (active) {
            if (image != null) {
                g.drawImage(image, x, y, null); // Draw the loaded image
            } else {
                // Fallback for if image fails to load
                g.setColor(Color.GREEN);
                g.fillRect(x, y, 40, 40);
            }
        }
    }

    /**
     * Gets the bounding box for this enemy for collision detection.
     * @return A Rectangle representing the enemy's bounds
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }

    /**
     * Marks this enemy as inactive (used when shot).
     */
    public void destroy() {
        active = false;
    }

    /**
     * Checks whether this enemy is still alive.
     * @return true if active, false if destroyed
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks whether this enemy is currently swooping.
     * @return true if currently swooping
     */
    public boolean isSwooping() {
        return swooping;
    }
}
