import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class InventoryPanel extends JPanel {
    private final GameData gameData;
    private final Consumer<String> equipAction;
    private final JPanel cropRows;
    private final JLabel equippedLabel;
    private final JLabel seedLabel;

    public InventoryPanel(GameData gameData, Consumer<String> equipAction) {
        this.gameData = gameData;
        this.equipAction = equipAction;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("YOUR INVENTORY");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Dialog", Font.BOLD, 21));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        equippedLabel = new JLabel();
        equippedLabel.setForeground(new Color(255, 229, 155));
        equippedLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        equippedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        seedLabel = new JLabel();
        seedLabel.setForeground(new Color(225, 215, 192));
        seedLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        seedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(equippedLabel);
        heading.add(Box.createVerticalStrut(3));
        heading.add(seedLabel);
        add(heading, BorderLayout.NORTH);

        cropRows = new JPanel();
        cropRows.setOpaque(false);
        cropRows.setLayout(new BoxLayout(cropRows, BoxLayout.Y_AXIS));
        cropRows.setBorder(new EmptyBorder(4, 2, 4, 2));
        JScrollPane scroll = new JScrollPane(cropRows);
        scroll.setPreferredSize(new Dimension(445, 350));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(64, 34, 23), 3));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        cropRows.removeAll();
        for (Crop crop : gameData.getInventory().getCrops()) {
            if (crop.getQuantity() <= 0) continue;
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(new Color(78, 45, 30, 220));
            row.setBorder(new EmptyBorder(6, 8, 6, 8));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
            JLabel icon = new JLabel();
            ImageIcon itemIcon = AssetUtil.icon(crop.getImagePath(), 48, 48);
            if (itemIcon != null) icon.setIcon(itemIcon);
            row.add(icon, BorderLayout.WEST);
            JLabel info = new JLabel("<html><b>" + crop.getName() + " ×" + crop.getQuantity()
                    + "</b><br>Sell value: " + crop.getSellPrice() + "¢ each</html>");
            info.setForeground(Color.WHITE);
            info.setFont(new Font("Dialog", Font.PLAIN, 13));
            row.add(info, BorderLayout.CENTER);
            JButton equip = new JButton(gameData.getInventory().getEquippedCrop() == crop ? "EQUIPPED" : "EQUIP");
            equip.setEnabled(gameData.getInventory().getEquippedCrop() != crop);
            equip.addActionListener(e -> {
                equipAction.accept(crop.getName());
                refresh();
            });
            row.add(equip, BorderLayout.EAST);
            cropRows.add(row);
            cropRows.add(Box.createVerticalStrut(5));
        }
        if (cropRows.getComponentCount() == 0) {
            JLabel empty = new JLabel("No crops in your inventory.");
            empty.setForeground(Color.WHITE);
            empty.setBorder(new EmptyBorder(12, 10, 10, 10));
            cropRows.add(empty);
        }
        Crop equipped = gameData.getInventory().getEquippedCrop();
        equippedLabel.setText("Equipped: " + (equipped == null ? "None" : equipped.getName()));
        StringBuilder seedText = new StringBuilder("Seeds: ");
        boolean hasSeeds = false;
        for (Seed seed : gameData.getSeeds()) {
            int count = gameData.getInventory().getSeedQuantity(seed.getName());
            if (count > 0) {
                if (hasSeeds) seedText.append(" · ");
                seedText.append(seed.getName()).append(" ×").append(count);
                hasSeeds = true;
            }
        }
        seedLabel.setText(hasSeeds ? seedText.toString() : "Seeds: none");
        cropRows.revalidate();
        cropRows.repaint();
    }
}
