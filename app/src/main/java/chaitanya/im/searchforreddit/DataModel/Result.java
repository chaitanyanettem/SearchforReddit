package chaitanya.im.searchforreddit.DataModel;

public class Result {
    private String kind;
    private Data data;

    public String getKind() {
        return kind;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
