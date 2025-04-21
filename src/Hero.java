import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Hero extends JComponent implements Weapon {
    private int xLocation;
    private int yLocation;
    private int size;
    private Laser laser = new Laser(0,0);

    public Hero() {
        xLocation = 350;
        yLocation = 450;
        size = 50;
    }

   public void paint(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(xLocation, yLocation, size, size);
   }

   public void paintIcon(Component c, Graphics g, int x, int y) {
        paint(g);
   }

   public int getXLocation() {
        return xLocation;
   }

   public int getYLocation() {
        return yLocation;
   }

   public void setXLocation(int xLocation) {
        this.xLocation = xLocation;
   }

   public Laser getLaser() {
        return laser;
   }

   public void shoot(int xLocation, int yLocation) {
        this.laser = new Laser(xLocation, yLocation);



        }
   }

