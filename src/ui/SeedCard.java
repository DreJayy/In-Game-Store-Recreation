import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class SeedCard extends JPanel {
    private static final Color PRICE_GREEN = new Color(52, 255, 60);

    public SeedCard(Seed seed, Consumer<String> purchase, Runnable refresh) {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(9, 10, 9, 10));
        setPreferredSize(new Dimension(445, 98));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 98));

        JLabel image = new JLabel();
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setPreferredSize(new Dimension(82, 78));
        ImageIcon cropIcon = AssetUtil.icon(seed.getImagePath(), 66, 66);
        if (cropIcon != null) image.setIcon(cropIcon);
        image.setOpaque(true);
        image.setBackground(new Color(87, 52, 34));
        image.setBorder(BorderFactory.createLineBorder(new Color(143, 91, 56), 2));
        add(image, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(seed.getName() + " Seed");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Dialog", Font.BOLD, 20));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel stock = new JLabel("X" + seed.getStock() + " Stock");
        stock.setForeground(new Color(230, 218, 199));
        stock.setFont(new Font("Dialog", Font.BOLD, 13));
        stock.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel price = new JLabel(seed.getPrice() + "¢");
        price.setForeground(PRICE_GREEN);
        price.setFont(new Font("Dialog", Font.BOLD, 18));
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(name);
        details.add(Box.createVerticalStrut(3));
        details.add(stock);
        details.add(price);
        add(details, BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel rarity = new JLabel(seed.getRarity(), SwingConstants.CENTER);
        rarity.setOpaque(true);
        rarity.setBackground(new Color(137, 137, 137));
        rarity.setForeground(Color.WHITE);
        rarity.setFont(new Font("Dialog", Font.BOLD, 12));
        rarity.setBorder(new EmptyBorder(3, 8, 3, 8));
        rarity.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton buy = new JButton();
        ImageIcon buyIcon = AssetUtil.icon("assets/ui/button_buy.png", 88, 31);
        if (buyIcon != null) buy.setIcon(buyIcon); else buy.setText("BUY");
        buy.setToolTipText("Buy " + seed.getName() + " Seed");
        buy.getAccessibleContext().setAccessibleName("Buy " + seed.getName() + " seed");
        buy.setBorderPainted(false);
        buy.setContentAreaFilled(false);
        buy.setFocusPainted(false);
        buy.setEnabled(seed.isAvailable());
        if (!seed.isAvailable()) buy.setText("SOLD OUT");
        buy.addActionListener(e -> {
            purchase.accept(seed.getName());
            refresh.run();
        });
        right.add(rarity);
        right.add(Box.createVerticalStrut(8));
        right.add(buy);
        add(right, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Image row = AssetUtil.image("assets/ui/seed_row_background.png");
        if (row != null) graphics.drawImage(row, 0, 0, getWidth(), getHeight(), this);
        else {
            graphics.setColor(new Color(72, 39, 26));
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        }
    }
}
