import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class SeedShopPanel extends JPanel {
    private final GameData gameData;
    private final Consumer<String> purchase;
    private final Runnable onRefresh;
    private final JPanel rows;

    public SeedShopPanel(GameData gameData, Consumer<String> purchase, Runnable onRefresh) {
        this.gameData = gameData;
        this.purchase = purchase;
        this.onRefresh = onRefresh;
        setLayout(new BorderLayout());
        setOpaque(false);
        rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(rows);
        scroll.setPreferredSize(new Dimension(495, 420));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(64, 34, 23), 3));
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new GardenScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        rows.removeAll();
        for (Seed seed : gameData.getSeeds()) {
            rows.add(new SeedCard(seed, purchase, onRefresh));
            rows.add(Box.createVerticalStrut(5));
        }
        rows.revalidate();
        rows.repaint();
    }
}
