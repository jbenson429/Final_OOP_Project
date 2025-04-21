import java.awt.*;

public class Laser {
    private int x;
    private int y;

    public Laser(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void paint(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, 5, 10);
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        paint(g);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
