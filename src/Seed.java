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
    private String name;
    private int price;
    private int stock;
    private int maxStock;
    private String rarity;
    private String imagePath;

    public Seed(String name, int price, int maxStock, String rarity, String imagePath) {
        this.name = name;
        this.price = price;
        this.stock = maxStock;
        this.maxStock = maxStock;
        this.rarity = rarity;
        this.imagePath = imagePath;
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
        if (isAvailable()) {
            stock--;
            return true;
        }

        return false;
    }

    public void restock() {
        stock = maxStock;
    }
}
