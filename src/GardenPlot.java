public class GardenPlot {
    private final int index;
    private String cropName;
    private String cropImagePath;
    private int harvestValue;
    private long readyAtMillis;

    public GardenPlot(int index) {
        this.index = index;
    }

    public int getIndex() { return index; }
    public String getCropName() { return cropName; }
    public String getCropImagePath() { return cropImagePath; }
    public int getHarvestValue() { return harvestValue; }
    public long getReadyAtMillis() { return readyAtMillis; }
    public boolean isPlanted() { return cropName != null; }
    public boolean isReady(long nowMillis) { return isPlanted() && nowMillis >= readyAtMillis; }

    void plant(String cropName, String cropImagePath, int harvestValue, long readyAtMillis) {
        this.cropName = cropName;
        this.cropImagePath = cropImagePath;
        this.harvestValue = harvestValue;
        this.readyAtMillis = readyAtMillis;
    }

    void clear() {
        cropName = null;
        cropImagePath = null;
        harvestValue = 0;
        readyAtMillis = 0;
    }
}
