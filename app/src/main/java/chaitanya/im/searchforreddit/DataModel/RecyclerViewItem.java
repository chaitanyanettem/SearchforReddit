package chaitanya.im.searchforreddit.DataModel;

public class RecyclerViewItem {
    private String permalink;
    private String title;
    private String author;
    private String domain;
    private String subreddit;
    private String timeString;
    private String url;
    private int score;
    private long utcTime;
    private int numComments;
    private String linkFlairText;
    private int gilded;
    private boolean archived;
    private boolean over18;
    private boolean locked;

    private boolean isSelf;

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(long utcTime) {
        this.utcTime = utcTime;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public String getLinkFlairText() {
        return linkFlairText;
    }

    public void setLinkFlairText(String linkFlairText) {
        this.linkFlairText = linkFlairText;
    }

    public int getGilded() {
        return gilded;
    }

    public void setGilded(int gilded) {
        this.gilded = gilded;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "RecyclerViewItem{" +
                "permalink='" + permalink + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", domain='" + domain + '\'' +
                ", subreddit='" + subreddit + '\'' +
                ", timeString='" + timeString + '\'' +
                ", url='" + url + '\'' +
                ", score=" + score +
                ", utcTime=" + utcTime +
                ", numComments=" + numComments +
                ", linkFlairText='" + linkFlairText + '\'' +
                ", gilded=" + gilded +
                ", archived=" + archived +
                ", over18=" + over18 +
                ", locked=" + locked +
                ", isSelf=" + isSelf +
                '}';
    }

}
