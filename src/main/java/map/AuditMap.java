package map;

import java.io.Serializable;

public class AuditMap implements Serializable, Comparable<AuditMap> {


    private String date;
    private String name;
    private String target_name;
    private String eventAction;
    private String history;
    private String v1_getEditors;


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

    public String getV1_getEditors() {
        return v1_getEditors;
    }

    public void setV1_getEditors(String v1_getEditors) {
        this.v1_getEditors = v1_getEditors;
    }

    public AuditMap(String date, String name, String target_name, String eventAction, String history, String v1_getEditors) {//, String eventID) {

        //this.eventID=eventID;
        this.date = date;
        this.name = name;
        this.target_name = target_name;
        this.eventAction = eventAction;
        this.history = history;
        this.v1_getEditors = v1_getEditors;
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

    @Override
    public boolean equals(Object obj) {
        AuditMap that = (AuditMap) obj;
        if (
                   !(this.name.equals(that.name))
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