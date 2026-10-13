import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Holds the sample shop state and applies purchase, sale, and restock rules. */
public class GameData {
    public static final long RESTOCK_INTERVAL_MILLIS = 5 * 60 * 1000L;
    public static final int DEFAULT_BALANCE = 1250;

    private final Inventory inventory;
    private final Map<String, Seed> seedCatalog;
    private final GardenPlot[] gardenPlots;
    private final Random random;
    private int balance;
    private long nextRestockTimeMillis;

    public GameData() {
        this(ThreadLocalRandom.current().nextInt(500, 2501), System.currentTimeMillis(), ThreadLocalRandom.current());
    }

    /** Clock-injectable constructor; balance is fixed, inventory and shop stock are randomized. */
    public GameData(int startingBalance, long nowMillis) {
        this(startingBalance, nowMillis, new Random());
    }

    public GameData(int startingBalance, long nowMillis, Random random) {
        if (startingBalance < 0) {
            throw new IllegalArgumentException("Starting balance cannot be negative.");
        }
        if (random == null) throw new IllegalArgumentException("Random generator cannot be null.");
        balance = startingBalance;
        this.random = random;
        inventory = new Inventory();
        seedCatalog = new LinkedHashMap<>();
        gardenPlots = new GardenPlot[8];
        for (int i = 0; i < gardenPlots.length; i++) gardenPlots[i] = new GardenPlot(i);
        addDefaultSeeds();
        addStartingCrops();
        addStartingSeeds();
        nextRestockTimeMillis = nowMillis + RESTOCK_INTERVAL_MILLIS;
    }

    public int getBalance() {
        return balance;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public GardenPlot[] getGardenPlots() {
        return gardenPlots.clone();
    }

    public ActionResult plantSeed(int plotIndex, String seedName, long nowMillis) {
        if (plotIndex < 0 || plotIndex >= gardenPlots.length) return result(false, "That is not a garden plot.", 0);
        GardenPlot plot = gardenPlots[plotIndex];
        if (plot.isPlanted()) return result(false, "That plot is already planted.", 0);
        Seed seed = getSeed(seedName);
        if (seed == null) return result(false, "Select a seed from your hotbar first.", 0);
        if (inventory.getSeedQuantity(seed.getName()) <= 0) return result(false, "You do not have that seed.", 0);
        if (!inventory.removeSeed(seed.getName())) return result(false, "You do not have that seed.", 0);

        String cropName = seed.getName();
        String cropImage = "assets/crops/" + cropName.toLowerCase(Locale.ROOT).replace(' ', '_') + ".png";
        int harvestValue = Math.max(10, seed.getPrice() * 2);
        plot.plant(cropName, cropImage, harvestValue, nowMillis + 10_000L);
        return result(true, "Planted " + cropName + "! Ready to harvest in 10 seconds.", 0);
    }

    public ActionResult harvestPlot(int plotIndex, long nowMillis) {
        if (plotIndex < 0 || plotIndex >= gardenPlots.length) return result(false, "That is not a garden plot.", 0);
        GardenPlot plot = gardenPlots[plotIndex];
        if (!plot.isPlanted()) return result(false, "That plot is empty. Select a seed and plant it.", 0);
        if (!plot.isReady(nowMillis)) return result(false, "" + plot.getCropName() + " is still growing.", 0);

        String cropName = plot.getCropName();
        inventory.addCrop(new Crop(cropName, plot.getHarvestValue(), 1, plot.getCropImagePath()));
        plot.clear();
        return result(true, "Harvested " + cropName + "! Added to your inventory.", 1);
    }

    public Collection<Seed> getSeeds() {
        return Collections.unmodifiableCollection(seedCatalog.values());
    }

    public Seed getSeed(String name) {
        if (name == null) return null;
        return seedCatalog.get(key(name));
    }

    public ActionResult buySeed(String seedName) {
        Seed seed = getSeed(seedName);
        if (seed == null) return result(false, "That seed is not sold here.", 0);
        if (!seed.isAvailable()) return result(false, seed.getName() + " is out of stock.", 0);
        if (balance < seed.getPrice()) return result(false, "Not enough Sheckles to buy " + seed.getName() + ".", 0);
        if (inventory.getSeedQuantity(seed.getName()) == Integer.MAX_VALUE) {
            return result(false, "You cannot carry any more of that seed.", 0);
        }

        // Mutate only after every purchase condition has passed.
        if (!seed.decreaseStock()) return result(false, seed.getName() + " is out of stock.", 0);
        balance -= seed.getPrice();
        inventory.addSeed(seed);
        return result(true, "Purchased 1 " + seed.getName() + " Seed.", seed.getPrice());
    }

    public ActionResult sellEquippedCrop() {
        Crop crop = inventory.getEquippedCrop();
        if (crop == null || !crop.isAvailable()) {
            return result(false, "Equip a crop before selling.", 0);
        }
        int salePrice = crop.getSellPrice();
        if ((long) balance + salePrice > Integer.MAX_VALUE) {
            return result(false, "The sale would exceed the balance limit.", 0);
        }
        String cropName = crop.getName();
        if (!inventory.removeEquippedCrop()) return result(false, "There is no equipped crop to sell.", 0);
        balance += salePrice;
        return result(true, "Sold 1 " + cropName + " for " + salePrice + " Sheckles.", salePrice);
    }

    public ActionResult sellAllCrops() {
        long totalValue = 0;
        long totalQuantity = 0;
        for (Crop crop : inventory.getCrops()) {
            totalValue += (long) crop.getSellPrice() * crop.getQuantity();
            totalQuantity += crop.getQuantity();
        }
        if (totalQuantity == 0) return result(false, "There are no crops to sell.", 0);
        if (totalValue > Integer.MAX_VALUE || (long) balance + totalValue > Integer.MAX_VALUE) {
            return result(false, "The sale would exceed the balance limit.", 0);
        }

        for (Crop crop : inventory.getCrops()) crop.clearQuantity();
        inventory.clearEquippedIfEmpty();
        balance += (int) totalValue;
        return result(true, "Sold all " + totalQuantity + " crops for " + totalValue + " Sheckles.", (int) totalValue);
    }

    /** Restocks all seed types once when the five-minute deadline has passed. */
    public boolean restockIfDue(long nowMillis) {
        if (nowMillis < nextRestockTimeMillis) return false;
        for (Seed seed : seedCatalog.values()) seed.restock();
        do {
            nextRestockTimeMillis += RESTOCK_INTERVAL_MILLIS;
        } while (nextRestockTimeMillis <= nowMillis);
        return true;
    }

    public long getMillisUntilRestock(long nowMillis) {
        return Math.max(0, nextRestockTimeMillis - nowMillis);
    }

    private void addDefaultSeeds() {
        addSeed("Carrot", 10, 12, "Common", "carrot");
        addSeed("Strawberry", 50, 8, "Uncommon", "strawberry");
        addSeed("Blueberry", 400, 4, "Rare", "blueberry");
        addSeed("Tomato", 800, 3, "Rare", "tomato");
        addSeed("Corn", 1300, 2, "Legendary", "corn");
        addSeed("Watermelon", 2500, 1, "Mythical", "watermelon");
        addSeed("Pumpkin", 3000, 2, "Mythical", "pumpkin");
        addSeed("Mango", 100000, 1, "Divine", "mango");
        addSeed("Dragon Fruit", 500000, 1, "Divine", "dragon_fruit");
        addSeed("Mushroom", 1500000, 1, "Prismatic", "mushroom");
        addSeed("Coconut", 6000, 1, "Mythical", "coconut");
        addSeed("Bamboo", 4000, 2, "Legendary", "bamboo");
        addSeed("Cacao", 2500, 2, "Mythical", "cacao");
        addSeed("Grape", 8500, 1, "Divine", "grape");
        addSeed("Pepper", 1000, 3, "Rare", "pepper");
        addSeed("Orange Tulip", 600, 4, "Uncommon", "orange_tulip");
        addSeed("Apple", 3250, 2, "Legendary", "apple");
        addSeed("Daffodil", 1000, 3, "Rare", "daffodil");
        addSeed("Cactus", 15000, 1, "Mythical", "cactus");
        addSeed("Beanstalk", 10000, 1, "Divine", "beanstalk");
    }

    private void addSeed(String name, int price, int stock, String rarity, String imageName) {
        int initialStock = random.nextInt(stock + 1);
        // Keep at least one inexpensive option in stock for the demo's purchase interaction.
        if ((name.equals("Carrot") || name.equals("Strawberry")) && initialStock == 0) initialStock = 1;
        Seed seed = new Seed(name, price, initialStock, stock, rarity, "assets/seeds/" + imageName + "_seed.png");
        seedCatalog.put(key(name), seed);
    }

    private void addStartingCrops() {
        Crop[] startingCrops = {
            new Crop("Carrot", 20, 1 + random.nextInt(6), "assets/crops/carrot.png"),
            new Crop("Strawberry", 120, random.nextInt(4), "assets/crops/strawberry.png"),
            new Crop("Blueberry", 500, random.nextInt(3), "assets/crops/blueberry.png"),
            new Crop("Tomato", 800, random.nextInt(2), "assets/crops/tomato.png")
        };
        for (Crop crop : startingCrops) inventory.addCrop(crop);
        Crop equipped = startingCrops[random.nextInt(startingCrops.length)];
        if (!equipped.isAvailable()) {
            for (Crop crop : startingCrops) {
                if (crop.isAvailable()) { equipped = crop; break; }
            }
        }
        inventory.equipCrop(equipped.getName());
    }

    private void addStartingSeeds() {
        for (Seed seed : seedCatalog.values()) {
            int chance = seed.getPrice() <= 100 ? 32 : seed.getPrice() <= 1000 ? 18 : 7;
            if (random.nextInt(100) < chance) {
                int quantity = 1 + random.nextInt(2);
                for (int i = 0; i < quantity; i++) inventory.addSeed(seed);
            }
        }
    }

    private ActionResult result(boolean success, String message, int amount) {
        return new ActionResult(success, message, amount, balance);
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    public static final class ActionResult {
        private final boolean success;
        private final String message;
        private final int amount;
        private final int balance;

        private ActionResult(boolean success, String message, int amount, int balance) {
            this.success = success;
            this.message = message;
            this.amount = amount;
            this.balance = balance;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        /** Purchase cost or sale proceeds. */
        public int getAmount() { return amount; }
        public int getBalance() { return balance; }
    }
}
