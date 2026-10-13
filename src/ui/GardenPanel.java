import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GardenPanel extends JPanel {
    private static final double[][] PLOTS = {
        {0.14, 0.135, 0.30, 0.14}, {0.14, 0.287, 0.30, 0.145},
        {0.14, 0.450, 0.30, 0.145}, {0.14, 0.612, 0.30, 0.15},
        {0.57, 0.135, 0.29, 0.14}, {0.57, 0.287, 0.29, 0.145},
        {0.57, 0.450, 0.29, 0.145}, {0.57, 0.612, 0.29, 0.15}
    };

    private final GameData gameData;
    private final Supplier<String> selectedSeed;
    private final Runnable onUpdate;
    private final Consumer<String> message;
    private final Image background = AssetUtil.image("assets/background/garden_background.jpg");
    private final Map<String, Image> cropImages = new HashMap<>();
    private int hoveredPlot = -1;

    public GardenPanel(GameData gameData, Supplier<String> selectedSeed, Runnable onUpdate, Consumer<String> message) {
        this.gameData = gameData;
        this.selectedSeed = selectedSeed;
        this.onUpdate = onUpdate;
        this.message = message;
        setOpaque(true);
        setPreferredSize(new Dimension(850, 540));
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int next = plotAt(e.getX(), e.getY());
                if (next != hoveredPlot) { hoveredPlot = next; repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) { hoveredPlot = -1; repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                int index = plotAt(e.getX(), e.getY());
                if (index < 0) {
                    message.accept("You can only plant in the brown garden beds.");
                    return;
                }
                GardenPlot plot = gameData.getGardenPlots()[index];
                long now = System.currentTimeMillis();
                if (plot.isPlanted()) {
                    GameData.ActionResult result = gameData.harvestPlot(index, now);
                    message.accept(result.getMessage());
                } else {
                    String seed = selectedSeed.get();
                    GameData.ActionResult result = gameData.plantSeed(index, seed, now);
                    message.accept(result.getMessage());
                }
                onUpdate.run();
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private int plotAt(int x, int y) {
        for (int i = 0; i < PLOTS.length; i++) {
            Rectangle bounds = plotBounds(i);
            if (bounds.contains(x, y)) return i;
        }
        return -1;
    }

    private Rectangle plotBounds(int index) {
        double[] plot = PLOTS[index];
        return new Rectangle((int) (plot[0] * getWidth()), (int) (plot[1] * getHeight()),
                (int) (plot[2] * getWidth()), (int) (plot[3] * getHeight()));
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (background != null) g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        else { g.setColor(new Color(97, 160, 74)); g.fillRect(0, 0, getWidth(), getHeight()); }
        GardenPlot[] plots = gameData.getGardenPlots();
        long now = System.currentTimeMillis();
        for (int i = 0; i < PLOTS.length; i++) {
            Rectangle bounds = plotBounds(i);
            GardenPlot plot = plots[i];
            if (i == hoveredPlot) {
                g.setColor(new Color(255, 242, 152, 100));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
                g.setColor(new Color(255, 243, 154, 220));
                g.setStroke(new BasicStroke(3));
                g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
            }
            if (plot.isPlanted()) paintPlant(g, plot, bounds, now);
        }
        g.dispose();
    }

    private void paintPlant(Graphics2D g, GardenPlot plot, Rectangle bounds, long now) {
        Image image = cropImages.computeIfAbsent(plot.getCropImagePath(), AssetUtil::image);
        if (image != null) {
            int size = Math.min(bounds.height - 8, 72);
            g.drawImage(image, bounds.x + (bounds.width - size) / 2, bounds.y + (bounds.height - size) / 2, size, size, this);
        }
        String text;
        if (plot.isReady(now)) text = "READY — click to harvest";
        else text = plot.getCropName() + " · " + Math.max(1, (plot.getReadyAtMillis() - now + 999) / 1000) + "s";
        Font oldFont = g.getFont();
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        FontMetrics metrics = g.getFontMetrics();
        int tx = bounds.x + (bounds.width - metrics.stringWidth(text)) / 2;
        int ty = bounds.y + bounds.height - 7;
        g.setColor(new Color(30, 23, 14, 210));
        g.fillRoundRect(tx - 5, ty - metrics.getAscent(), metrics.stringWidth(text) + 10, metrics.getHeight(), 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(text, tx, ty);
        g.setFont(oldFont);
    }
}
