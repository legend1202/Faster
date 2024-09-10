package faster.com.ec.Getset;

/**
 * Created by Redixbit 2 on 30-08-2016.
 */
public class positionGetSet {

    private String id;
    private String user_id;
    private String lat;
    private String lon;
    private String address;
    private String alias;
    private String phone;
    private String delivery_note;
    private String department_number;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return user_id;
    }
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }
    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeliveryNote() {
        return delivery_note;
    }
    public void setDeliveryNote(String delivery_note) {
        this.delivery_note = delivery_note;
    }

    public String getDepartmentNumber() {
        return department_number;
    }
    public void setDepartmentNumber(String department_number) {
        this.department_number = department_number;
    }
}
