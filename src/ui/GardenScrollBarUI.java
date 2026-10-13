import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GardenScrollBarUI extends BasicScrollBarUI {
    private static final Color TRACK = new Color(44, 27, 20);
    private static final Color THUMB = new Color(167, 112, 69);
    private static final Color HOVER = new Color(210, 158, 98);

    @Override protected void configureScrollBarColors() {
        trackColor = TRACK;
        thumbColor = THUMB;
    }

    @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
    @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

    private JButton zeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        g.setColor(TRACK);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(isDragging || isThumbRollover() ? HOVER : THUMB);
        g2.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 10, 10);
        g2.setColor(new Color(92, 55, 33));
        g2.drawRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 10, 10);
        g2.dispose();
    }
}
