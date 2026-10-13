import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HotbarPanel extends JPanel {
    public static final class Item {
        private final boolean seed;
        private final String name;
        private final int quantity;
        private final String imagePath;
        Item(boolean seed, String name, int quantity, String imagePath) {
            this.seed = seed; this.name = name; this.quantity = quantity; this.imagePath = imagePath;
        }
        public boolean isSeed() { return seed; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public String getImagePath() { return imagePath; }
    }

    private final GameData gameData;
    private final Consumer<Item> onSelect;
    private final List<JButton> slots = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private String selectedKey;
    private int firstVisible;

    public HotbarPanel(GameData gameData, Consumer<Item> onSelect) {
        this.gameData = gameData;
        this.onSelect = onSelect;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
        for (int i = 0; i < 9; i++) {
            final int slotIndex = i;
            JButton slot = new JButton();
            slot.setPreferredSize(new Dimension(49, 49));
            slot.setMargin(new Insets(0, 0, 0, 0));
            slot.setHorizontalTextPosition(SwingConstants.CENTER);
            slot.setVerticalTextPosition(SwingConstants.BOTTOM);
            slot.setForeground(Color.WHITE);
            slot.setFont(new Font("Dialog", Font.BOLD, 10));
            slot.setFocusPainted(false);
            slot.setContentAreaFilled(false);
            slot.setToolTipText("Empty inventory slot");
            slot.addActionListener(e -> selectVisible(slotIndex));
            slots.add(slot);
            add(slot);
        }
        MouseWheelListener wheel = (MouseWheelEvent e) -> {
            int maxFirst = Math.max(0, items.size() - slots.size());
            firstVisible = Math.max(0, Math.min(maxFirst, firstVisible + e.getWheelRotation()));
            refresh();
        };
        addMouseWheelListener(wheel);
        for (JButton slot : slots) slot.addMouseWheelListener(wheel);
        refresh();
    }

    public void refresh() {
        List<Item> next = new ArrayList<>();
        for (Crop crop : gameData.getInventory().getCrops()) {
            if (crop.getQuantity() > 0) next.add(new Item(false, crop.getName(), crop.getQuantity(), crop.getImagePath()));
        }
        for (Seed seed : gameData.getSeeds()) {
            int count = gameData.getInventory().getSeedQuantity(seed.getName());
            if (count > 0) next.add(new Item(true, seed.getName(), count, seed.getImagePath()));
        }
        items = next;
        firstVisible = Math.min(firstVisible, Math.max(0, items.size() - slots.size()));
        for (int i = 0; i < slots.size(); i++) {
            int itemIndex = firstVisible + i;
            JButton button = slots.get(i);
            if (itemIndex < items.size()) {
                Item item = items.get(itemIndex);
                String key = key(item);
                ImageIcon icon = AssetUtil.icon(item.getImagePath(), 31, 31);
                button.setIcon(icon);
                button.setText("×" + item.getQuantity());
                button.setToolTipText(item.getName() + (item.isSeed() ? " Seed" : " Crop") + " ×" + item.getQuantity());
                button.putClientProperty("item", item);
                button.setBorder(new LineBorder(key.equals(selectedKey) ? new Color(255, 221, 88) : new Color(92, 86, 68), key.equals(selectedKey) ? 3 : 1));
                button.setEnabled(true);
                button.setBackground(key.equals(selectedKey) ? new Color(106, 102, 65, 180) : new Color(30, 30, 27, 160));
            } else {
                button.setIcon(null);
                button.setText("" + (itemIndex + 1));
                button.putClientProperty("item", null);
                button.setToolTipText("Empty inventory slot");
                button.setBorder(new LineBorder(new Color(92, 86, 68), 1));
                button.setEnabled(false);
            }
        }
        repaint();
    }

    public String getSelectedSeedName() {
        for (Item item : items) if (key(item).equals(selectedKey) && item.isSeed()) return item.getName();
        return null;
    }

    public String getPlantingSeedName() {
        String selected = getSelectedSeedName();
        if (selected != null) return selected;
        for (Item item : items) if (item.isSeed()) return item.getName();
        return null;
    }

    public void selectSeed(String name) {
        selectedKey = "seed:" + name.toLowerCase();
        refresh();
    }

    private void selectVisible(int slotIndex) {
        Object value = slots.get(slotIndex).getClientProperty("item");
        if (!(value instanceof Item)) return;
        Item item = (Item) value;
        selectedKey = key(item);
        onSelect.accept(item);
        refresh();
    }

    private String key(Item item) { return (item.isSeed() ? "seed:" : "crop:") + item.getName().toLowerCase(); }
}
