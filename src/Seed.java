/*
Seed components include:
    1. name
    2. price
    3. stock
    4. max stock (after restocking)
    5. rarity
    6. image location
*/

public class Seed {
    private final String name;
    private final int price;
    private int stock;
    private final int maxStock;
    private final String rarity;
    private final String imagePath;

    public Seed(String name, int price, int maxStock, String rarity, String imagePath) {
        this(name, price, maxStock, maxStock, rarity, imagePath);
    }

    public Seed(String name, int price, int stock, int maxStock, String rarity, String imagePath) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Seed name cannot be blank.");
        }
        if (price < 0 || stock < 0 || maxStock < 0 || stock > maxStock) {
            throw new IllegalArgumentException("Seed price and stock cannot be negative.");
        }
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
        this.maxStock = maxStock;
        this.rarity = rarity == null || rarity.trim().isEmpty() ? "Common" : rarity.trim();
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public int getMaxStock() {
        return maxStock;
    }

    public String getRarity() {
        return rarity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isAvailable() {
        return stock > 0;
    }

    public boolean decreaseStock() {
        return decreaseStock(1);
    }

    public boolean decreaseStock(int amount) {
        if (amount <= 0 || stock < amount) {
            return false;
        }
        stock -= amount;
        return true;
    }

    public void restock() {
        stock = maxStock;
    }
}
