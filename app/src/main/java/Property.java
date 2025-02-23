public class Property {
    private String address;
    private int imageResId;
    private boolean saved;

    public Property(String title, int imageResId,boolean saved) {
        this.address = title;
        this.imageResId = imageResId;
        this.saved = saved;
    }

    public String getTitle() { return address; }
    public int getImageResId() { return imageResId; }
    public boolean isSaved() {
        return saved;
    }
}
