import java.io.Serializable;

public class Employee implements Serializable, Comparable<Employee> {

    private String date;
    private String name;
    private String target_name;
    private String getPrimaryEventType;
    private String history;

    public Employee(String date, String name, String target_name, String getPrimaryEventType, String history) {
        this.date = date;
        this.name = name;
        this.target_name = target_name;
        this.getPrimaryEventType = getPrimaryEventType;
        this.history = history;
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

    public String getGetPrimaryEventType() {
        return getPrimaryEventType;
    }

    public void setGetPrimaryEventType(String getPrimaryEventType) {
        this.getPrimaryEventType = getPrimaryEventType;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    @Override
    public int compareTo(Employee o) {
        int result = this.target_name.compareToIgnoreCase(o.target_name);
        if(result != 0){
            return result;
        }else{
            return new String(this.target_name).compareTo(new String(o.target_name));
        }
    }

    @Override
    //this is required to print the user friendly information about the Employee
    public String toString() {
        return "[date=" + this.date + ", name=" + this.name + ", target_name=" + this.target_name + ", getPrimaryEventType=" +
                this.getPrimaryEventType + ", history=" + this.history +"]";
    }

}