package tc.scworldeditor;

public class WorldListItem {
    private String name;
    private String fileName;

    public WorldListItem() {
    }

    public WorldListItem(String fileName, String name) {
        this.name = name;
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

