import java.io.Serializable;
import java.sql.Date;

public class Employee implements Serializable, Comparable<Employee> {

    private String date;
    private String name;
    private String target_name;
    private String get1;
    private String get2;

    public Employee(String date, String name, String target_name, String get1, String get2) {
        this.date = date;
        this.name = name;
        this.target_name = target_name;
        this.get1 = get1;
        this.get2 = get2;
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

    public String getGet1() {
        return get1;
    }

    public void setGet1(String get1) {
        this.get1 = get1;
    }

    public String getGet2() {
        return get2;
    }

    public void setGet2(String get2) {
        this.get2 = get2;
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
        return "[date=" + this.date + ", name=" + this.name + ", target_name=" + this.target_name + ", get1=" +
                this.get1 + ", get2=" + this.get2 +"]";
    }

}