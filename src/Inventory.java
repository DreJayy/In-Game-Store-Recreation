
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class Inventory {
    private Map<String, Integer> seeds;
    private Map<String, Crop> crops;
    private Crop equippedCrop;

    public Inventory() {
        seeds = new HashMap<>();
        crops = new HashMap<>();
        equippedCrop = null;
    }

    public void addSeed(Seed seed) {
        String name = seed.getName();
        seeds.put(name, seeds.getOrDefault(name, 0) + 1);
    }

    public int getSeedQuantity(String name) {
        return seeds.getOrDefault(name, 0);
    }

    public void addCrop(Crop crop) {
        String name = crop.getName();

        if (crops.containsKey(name)) {
            crops.get(name).addQuantity(crop.getQuantity());
        } else {
            crops.put(name, crop);
        }
    }

    public Collection<Crop> getCrops() {
        return crops.values();
    }

    public boolean equipCrop(String name) {
        Crop crop = crops.get(name);

        if (crop != null && crop.isAvailable()) {
            equippedCrop = crop;
            return true;
        }

        return false;
    }

    public Crop getEquippedCrop() {
        return equippedCrop;
    }

    public boolean removeEquippedCrop() {
        if (equippedCrop == null) {
            return false;
        }

        if (!equippedCrop.decreaseQuantity()) {
            return false;
        }

        if (!equippedCrop.isAvailable()) {
            equippedCrop = null;
        }

        return true;
    }
}
