public class Crop {
    private final String name;
    private final int sellPrice;
    private int quantity;
    private final String imagePath;

    public Crop(String name, int sellPrice, int quantity, String imagePath) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Crop name cannot be blank.");
        }
        if (sellPrice < 0 || quantity < 0) {
            throw new IllegalArgumentException("Crop price and quantity cannot be negative.");
        }
        this.name = name.trim();
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public String getName() {
        return name;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public boolean decreaseQuantity() {
        return decreaseQuantity(1);
    }

    public boolean decreaseQuantity(int amount) {
        if (amount <= 0 || quantity < amount) {
            return false;
        }
        quantity -= amount;
        return true;
    }

    public void addQuantity(int amount) {
        if (amount < 0 || (long) quantity + amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid crop quantity increase.");
        }
        quantity += amount;
    }

    public void clearQuantity() {
        quantity = 0;
    }
}
