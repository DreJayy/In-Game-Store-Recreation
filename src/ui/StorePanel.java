import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class StorePanel extends JPanel {
    private static final String SEEDS = "Seeds";
    private static final String GARDEN = "Garden";
    private static final String SELL = "Sell";

    private final GameData gameData;
    private final JLabel balanceLabel = new JLabel();
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel scene = new JPanel(new GridBagLayout());
    private final SeedShopPanel seedShopPanel;
    private final HotbarPanel hotbar;
    private final GardenPanel gardenPanel;
    private final SellPanel sellPanel;
    private final AnimatedShopWindow shopWindow;
    private final JButton openShopButton = new JButton("SHOP");
    private final NumberFormat moneyFormat = NumberFormat.getIntegerInstance(Locale.US);
    private Timer messageTimer;
    private Timer restockTimer;
    private String activePage = SEEDS;
    private Image backgroundImage;

    public StorePanel() { this(new GameData()); }

    public StorePanel(GameData gameData) {
        this.gameData = gameData;
        setLayout(new BorderLayout());
        setOpaque(false);

        seedShopPanel = new SeedShopPanel(gameData, this::buySeed, this::refreshAll);
        hotbar = new HotbarPanel(gameData, this::selectHotbarItem);
        gardenPanel = new GardenPanel(gameData, hotbar::getPlantingSeedName, this::refreshAll,
                message -> showMessage(message, false));
        sellPanel = new SellPanel(this::chooseSellOption);
        shopWindow = createShopWindow();

        add(createNavigation(), BorderLayout.NORTH);
        scene.setOpaque(false);
        addToScene(shopWindow);
        addToScene(gardenPanel);
        addToScene(sellPanel);
        addToScene(openShopButton);
        add(scene, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        openShopButton.setVisible(false);
        openShopButton.setFont(new Font("Dialog", Font.BOLD, 18));
        openShopButton.setForeground(Color.WHITE);
        openShopButton.setBackground(new Color(58, 159, 45));
        openShopButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(208, 231, 128), 3), new EmptyBorder(12, 24, 12, 24)));
        openShopButton.setFocusPainted(false);
        openShopButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openShopButton.addActionListener(e -> { shopWindow.setVisible(true); openShopButton.setVisible(false); animateShopOpen(); });

        refreshAll();
        setPage(SEEDS);
        restockTimer = new Timer(1000, e -> tick());
        restockTimer.start();
    }

    private void addToScene(Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = component == gardenPanel ? GridBagConstraints.BOTH : GridBagConstraints.NONE;
        c.insets = new Insets(5, 8, 5, 8);
        scene.add(component, c);
    }

    private JPanel createNavigation() {
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        tabs.setOpaque(false);
        tabs.add(tabButton("SEEDS", "assets/ui/tab_seeds.png", SEEDS));
        tabs.add(tabButton("GARDEN", "assets/ui/tab_garden.png", GARDEN));
        tabs.add(tabButton("SELL", "assets/ui/tab_sell.png", SELL));
        return tabs;
    }

    private JButton tabButton(String text, String path, String page) {
        JButton button = new JButton();
        ImageIcon icon = AssetUtil.icon(path, 124, 38);
        if (icon == null) button.setText(text); else button.setIcon(icon);
        button.setToolTipText(text);
        button.getAccessibleContext().setAccessibleName(text + " tab");
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> setPage(page));
        return button;
    }

    private AnimatedShopWindow createShopWindow() {
        AnimatedShopWindow frame = new AnimatedShopWindow();
        frame.setLayout(new BorderLayout(0, 6));
        frame.setOpaque(false);
        frame.setPreferredSize(new Dimension(550, 555));
        frame.setBorder(new EmptyBorder(37, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout(8, 0)) {
            private final Image texture = AssetUtil.image("assets/ui/shop_header.png");
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (texture != null) g.drawImage(texture, 0, 0, getWidth(), getHeight(), this);
                else { g.setColor(new Color(86, 165, 68)); g.fillRect(0, 0, getWidth(), getHeight()); }
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 10, 4, 8));
        JLabel timer = new JLabel();
        timer.setName("restockTimer");
        timer.setForeground(Color.WHITE);
        timer.setFont(new Font("Dialog", Font.BOLD, 15));
        header.add(timer, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        actions.setOpaque(false);
        JButton restock = imageButton("assets/ui/button_restock.png", "RESTOCK", 103, 29);
        restock.setToolTipText("Seeds restock automatically every five minutes");
        restock.addActionListener(e -> showMessage("Seeds restock automatically every five minutes.", false));
        actions.add(restock);
        JButton close = imageButton("assets/ui/button_close.png", "X", 26, 26);
        close.setToolTipText("Close the seed shop");
        close.addActionListener(e -> closeShop());
        actions.add(close);
        header.add(actions, BorderLayout.EAST);
        frame.add(header, BorderLayout.NORTH);
        frame.add(seedShopPanel, BorderLayout.CENTER);
        return frame;
    }

    private JButton imageButton(String path, String fallback, int width, int height) {
        JButton button = new JButton();
        ImageIcon icon = AssetUtil.icon(path, width, height);
        if (icon == null) button.setText(fallback); else button.setIcon(icon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 2));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 18, 10, 18));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        balanceLabel.setIcon(AssetUtil.icon("assets/ui/sheckle_coin.png", 27, 27));
        balanceLabel.setIconTextGap(7);
        footer.add(balanceLabel, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(new Color(255, 247, 216));
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 13));
        center.add(statusLabel);
        center.add(Box.createVerticalStrut(4));
        hotbar.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(hotbar);
        footer.add(center, BorderLayout.CENTER);
        footer.add(Box.createHorizontalStrut(160), BorderLayout.EAST);
        return footer;
    }

    private void setPage(String page) {
        activePage = page;
        backgroundImage = AssetUtil.image(page.equals(GARDEN)
                ? "assets/background/garden_background.jpg"
                : page.equals(SELL) ? "assets/background/sell_background.jpeg" : "assets/background/seeds_background.jpeg");
        boolean seeds = SEEDS.equals(page);
        shopWindow.setVisible(seeds);
        gardenPanel.setVisible(GARDEN.equals(page));
        sellPanel.setVisible(SELL.equals(page));
        openShopButton.setVisible(false);
        if (seeds) {
            seedShopPanel.refresh();
            animateShopOpen();
        } else if (GARDEN.equals(page)) {
            gardenPanel.repaint();
            showMessage("Select a seed in your hotbar, then click one of the brown plots to plant.", true);
        } else {
            showMessage("Welcome to the sell stall. Choose an option.", true);
            refreshAll();
        }
        repaint();
    }

    private void closeShop() {
        shopWindow.setVisible(false);
        openShopButton.setVisible(true);
        showMessage("Shop closed. Click SHOP to reopen it.", true);
    }

    private void animateShopOpen() {
        shopWindow.setProgress(0f);
        Timer animation = new Timer(16, null);
        animation.addActionListener(e -> {
            float next = Math.min(1f, shopWindow.getProgress() + 0.12f);
            shopWindow.setProgress(next);
            if (next >= 1f) animation.stop();
        });
        animation.start();
    }

    private void buySeed(String name) {
        GameData.ActionResult result = gameData.buySeed(name);
        if (result.isSuccess()) hotbar.selectSeed(name);
        showMessage(result.getMessage(), result.isSuccess());
        refreshAll();
    }

    private void equipCrop(String name) {
        boolean equipped = gameData.getInventory().equipCrop(name);
        showMessage(equipped ? name + " equipped." : "That crop is not available to equip.", equipped);
        refreshAll();
    }

    private void selectHotbarItem(HotbarPanel.Item item) {
        if (item.isSeed()) {
            showMessage(item.getName() + " Seed selected. Click a brown garden plot to plant it.", true);
        } else {
            equipCrop(item.getName());
        }
    }

    private void chooseSellOption(int option) {
        switch (option) {
            case 1: applySale(gameData.sellAllCrops()); break;
            case 2: applySale(gameData.sellEquippedCrop()); break;
            case 3:
                Crop crop = gameData.getInventory().getEquippedCrop();
                if (crop == null) showMessage("Select or equip a crop in your hotbar first.", false);
                else showMessage(crop.getName() + " is worth " + moneyFormat.format(crop.getSellPrice()) + " Sheckles each.", true);
                break;
            default:
                showMessage("No problem. You can choose another selling option.", true);
                break;
        }
    }

    private void applySale(GameData.ActionResult result) {
        showMessage(result.getMessage(), result.isSuccess());
        refreshAll();
    }

    private void refreshAll() {
        balanceLabel.setText(moneyFormat.format(gameData.getBalance()) + " Sheckles");
        seedShopPanel.refresh();
        hotbar.refresh();
        gardenPanel.repaint();
        scene.revalidate();
        scene.repaint();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        if (gameData.restockIfDue(now)) {
            seedShopPanel.refresh();
            showMessage("The seed shop has restocked!", true);
        }
        gardenPanel.repaint();
        Component timer = findNamed(shopWindow, "restockTimer");
        if (timer instanceof JLabel) {
            long remaining = gameData.getMillisUntilRestock(now);
            long seconds = (remaining + 999) / 1000;
            ((JLabel) timer).setText("New seeds in " + String.format("%d:%02d", seconds / 60, seconds % 60));
        }
    }

    private Component findNamed(Container root, String name) {
        for (Component component : root.getComponents()) {
            if (name.equals(component.getName())) return component;
            if (component instanceof Container) {
                Component found = findNamed((Container) component, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void showMessage(String text, boolean success) {
        statusLabel.setText(text);
        statusLabel.setForeground(success ? new Color(226, 255, 200) : new Color(255, 203, 177));
        if (messageTimer != null) messageTimer.stop();
        messageTimer = new Timer(3800, e -> statusLabel.setText(" "));
        messageTimer.setRepeats(false);
        messageTimer.start();
    }

    public void stopTimers() {
        if (messageTimer != null) messageTimer.stop();
        if (restockTimer != null) restockTimer.stop();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        else { g.setColor(new Color(91, 147, 67)); g.fillRect(0, 0, getWidth(), getHeight()); }
        g.setColor(new Color(15, 22, 13, 45));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private final class AnimatedShopWindow extends JPanel {
        private float progress = 1f;
        private final Image frameImage = AssetUtil.image("assets/ui/shop_frame.png");
        AnimatedShopWindow() { super(new BorderLayout(0, 6)); }
        float getProgress() { return progress; }
        void setProgress(float progress) {
            this.progress = progress;
            int width = (int) (550 * (0.88 + 0.12 * progress));
            int height = (int) (555 * (0.88 + 0.12 * progress));
            setPreferredSize(new Dimension(width, height));
            revalidate();
            repaint();
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setComposite(AlphaComposite.SrcOver.derive(progress));
            if (frameImage != null) g.drawImage(frameImage, 0, 0, getWidth(), getHeight(), this);
            else { g.setColor(new Color(68, 37, 25)); g.fillRect(0, 0, getWidth(), getHeight()); }
            g.dispose();
        }
        @Override protected void paintChildren(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setComposite(AlphaComposite.SrcOver.derive(progress));
            super.paintChildren(g);
            g.dispose();
        }
    }
}
