import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel {

    private Hero hero;
    private int slide = 10;

    public GamePanel() {
        hero = new Hero();

        this.setBackground(Color.WHITE);
        this.setPreferredSize(new Dimension(800, 800));
        this.setFocusable(true);
        this.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        hero.setXLocation(hero.getXLocation() + slide);
                        break;
                    case KeyEvent.VK_LEFT:
                        hero.setXLocation(hero.getXLocation() - slide);
                        break;
                    case KeyEvent.VK_SPACE:
                        hero.shoot(hero.getXLocation() + 25, hero.getYLocation());
                       Thread loopThread = new Thread(() -> { while(hero.getLaser().getY() != 0) {
                           hero.getLaser().setY(hero.getLaser().getY() - 1);
                           repaint();
                           revalidate();
                           try {
                               Thread.sleep(1);

                           } catch (InterruptedException ex) {
                               throw new RuntimeException(ex);
                           }


                       }});

                       loopThread.start();

                        break;
                }
                repaint();
                revalidate();
            }

            public void keyReleased(KeyEvent e) {}
            public void keyTyped(KeyEvent e) {}

        });

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        hero.paintIcon(this, g, hero.getXLocation(), hero.getYLocation());
        hero.getLaser().paintIcon(this, g, hero.getLaser().getX(), hero.getLaser().getY());
    }


}