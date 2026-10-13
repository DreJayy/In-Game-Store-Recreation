
public class Crop {
    private String name;
    private int sellPrice;
    private int quantity;
    private String imagePath;

    public Crop(String name, int sellPrice, int quantity, String imagePath) {
        this.name = name;
        this.sellPrice = sellPrice;
        this.quantity = Math.max(0, quantity);
        this.imagePath = imagePath;
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
        if (isAvailable()) {
            quantity--;
            return true;
        }

        return false;
    }

    public void addQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;
        }
    }
}
