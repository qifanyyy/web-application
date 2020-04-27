public class Star {
    private final String name;
    private final int count;
    private final String id;

    public Star(String name, String id, int count) {
        this.name = name;
        this.count = count;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Star [name=" + name + ", count=" + count + "]";
    }

    public int getCount() {
        return -count;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }



}