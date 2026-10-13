import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;

public class SellPanel extends JPanel {
    private static final String[] OPTIONS = {
        "I want to sell my inventory",
        "I want to sell this",
        "How much is this worth?",
        "Nevermind"
    };

    public SellPanel(IntConsumer chooseOption) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        JPanel dialogue = new JPanel();
        dialogue.setOpaque(false);
        dialogue.setLayout(new BoxLayout(dialogue, BoxLayout.Y_AXIS));
        JLabel question = new JLabel("Got anything to sell?", SwingConstants.CENTER);
        question.setAlignmentX(Component.CENTER_ALIGNMENT);
        question.setForeground(Color.WHITE);
        question.setFont(new Font("Dialog", Font.BOLD, 29));
        question.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        dialogue.add(question);
        for (int i = 0; i < OPTIONS.length; i++) {
            final int option = i + 1;
            AnimatedChoice button = new AnimatedChoice("#" + option + "   [\"" + OPTIONS[i] + "\"]");
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(e -> chooseOption.accept(option));
            dialogue.add(button);
            dialogue.add(Box.createVerticalStrut(8));
        }
        add(dialogue);
    }

    private static class AnimatedChoice extends JButton {
        private float hover;
        private boolean hovered;
        private final Timer animation;

        AnimatedChoice(String text) {
            super(text);
            setFont(new Font("Dialog", Font.BOLD, 20));
            setForeground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setPreferredSize(new Dimension(560, 54));
            setMaximumSize(new Dimension(620, 54));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            animation = new Timer(16, e -> {
                float goal = hovered ? 1f : 0f;
                if (Math.abs(hover - goal) < 0.025f) {
                    hover = goal;
                    ((Timer) e.getSource()).stop();
                } else {
                    hover += (goal - hover) * 0.32f;
                }
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; animation.restart(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; animation.restart(); }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scale = 1.0 + 0.035 * hover;
            g.translate(getWidth() / 2.0, getHeight() / 2.0);
            g.scale(scale, scale);
            g.translate(-getWidth() / 2.0, -getHeight() / 2.0);
            int inset = 5;
            g.setColor(new Color(48 + (int) (96 * hover), 34 + (int) (56 * hover), 26 + (int) (20 * hover), 225));
            g.fillRoundRect(inset, 3, getWidth() - inset * 2, getHeight() - 6, 14, 14);
            g.setColor(new Color(139 + (int) (116 * hover), 93 + (int) (115 * hover), 58 + (int) (70 * hover), 245));
            g.setStroke(new BasicStroke(1.5f + 3f * hover));
            g.drawRoundRect(inset, 3, getWidth() - inset * 2, getHeight() - 6, 14, 14);

            int offset = (int) (hover * 25);
            if (hover > 0.03f) {
                g.setColor(new Color(255, 238, 166));
                int arrowX = 14 + (int) (hover * 7);
                int arrowY = getHeight() / 2;
                int[] xs = {arrowX, arrowX + 10, arrowX};
                int[] ys = {arrowY - 7, arrowY, arrowY + 7};
                g.fillPolygon(xs, ys, 3);
            }
            g.setFont(getFont().deriveFont(Font.BOLD, 20f + 2f * hover));
            FontMetrics fm = g.getFontMetrics();
            int textX = 30 + offset;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(new Color(0, 0, 0, 190));
            g.drawString(getText(), textX + 1, textY + 2);
            g.setColor(hover > 0.45f ? new Color(255, 249, 218) : Color.WHITE);
            g.drawString(getText(), textX, textY);
            g.dispose();
        }
    }
}
