package chaitanya.im.searchforreddit.DataModel;

import java.util.ArrayList;
import java.util.List;

public class Data {
    private String modhash;
    private List<Child> children = new ArrayList<>();
    private String after;
    private String before;
    private Object facets;

    public Object getFacets() {
        return facets;
    }

    public void setFacets(Object facets) {
        this.facets = facets;
    }

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public Object getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public Object getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }
}
