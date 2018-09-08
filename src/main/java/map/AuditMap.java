package map;

import java.io.Serializable;

public class AuditMap implements Serializable, Comparable<AuditMap> {


    private String date;
    private String realOnwer;
    private String name;
    private String webViewLink;
    private String target_name;
    private String eventAction;
    private String history;
    private String v3_owners;
    private Boolean allEmailFromINovus;
    private String fileid;

    public AuditMap(String name) {
        this.name = name;
    }

    public AuditMap(String name, String realOnwer, String webViewLink, String v3_getOwners, Boolean allEmailFromINovus) {
        this.name = name;
        this.realOnwer = realOnwer;
        this.webViewLink = webViewLink;
        this.v3_owners = v3_getOwners;
        this.allEmailFromINovus = allEmailFromINovus;

    }

    public AuditMap(String name, String v3_getOwners, Boolean allEmailFromINovus) {
        this.name = name;
        this.v3_owners = v3_getOwners;
        this.allEmailFromINovus = allEmailFromINovus;
    }

    public AuditMap(String date, String name, String target_name, String eventAction, String history, String v3_getOwners, Boolean allEmailFromINovus) {
        this.date = date;
        this.name = name;
        this.target_name = target_name;
        this.eventAction = eventAction;
        this.history = history;
        this.v3_owners = v3_getOwners;
        this.allEmailFromINovus = allEmailFromINovus;
    }

    public AuditMap(String date, String name, String target_name, String eventAction, String history, String v3_getOwners, Boolean allEmailFromINovus, String fileid) {
        this.date = date;
        this.name = name;
        this.target_name = target_name;
        this.eventAction = eventAction;
        this.history = history;
        this.v3_owners = v3_getOwners;
        this.allEmailFromINovus = allEmailFromINovus;
        this.fileid = fileid;
    }

    @Override
    public int compareTo(AuditMap o) {
        int result = this.target_name.compareToIgnoreCase(o.target_name);
        if (result != 0) {
            return result;
        } else {
            return new String(this.target_name).compareTo(new String(o.target_name));
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget_name() {
        return target_name;
    }

    public void setTarget_name(String target_name) {
        this.target_name = target_name;
    }

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(String eventAction) {
        this.eventAction = eventAction;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getV3_owners() {
        return v3_owners;
    }

    public void setV3_owners(String v3_owners) {
        this.v3_owners = v3_owners;
    }

    public Boolean getAllEmailFromINovus() {
        return allEmailFromINovus;
    }

    public void setAllEmailFromINovus(Boolean allEmailFromINovus) {
        this.allEmailFromINovus = allEmailFromINovus;
    }

    public String getAll() {
        return date + name + target_name + eventAction + history + v3_owners + allEmailFromINovus;
    }

    public String getBody() {
        String sep = "\n";
        return date + sep + name + sep + target_name + sep + eventAction + sep + history + sep + v3_owners;
    }

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getRealOnwer() {
        return realOnwer;
    }

    public void setRealOnwer(String realOnwer) {
        this.realOnwer = realOnwer;
    }

    @Override
    public boolean equals(Object obj) {
        AuditMap that = (AuditMap) obj;
        if (!(this.name.equals(that.name))
                || !(this.target_name.equals(that.target_name))
                || !(this.date.equals(that.date))
                || !(this.eventAction.equals(that.eventAction))
                || !(this.history.equals(that.history))
                ) return false;
        return true;
    }

    @Override
    //this is required to print the user friendly information about the map.AuditMap
    public String toString() {
        return "[date=" + this.date + ", name=" + this.name + ", target_name=" + this.target_name + ", eventAction=" +
                this.eventAction + ", history=" + this.history + "]";
    }
}