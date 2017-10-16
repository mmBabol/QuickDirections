package ListView;

/**
 * Created by Mat on 11/27/2016.
 *
 * LocationRow, custom ListView item
 */
public class LocationRow {

    private int imageID;
    private String locationName;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String postal;

    public LocationRow(int img, String name, double lat, double lng, String address, String city, String postal){
        imageID = img;
        locationName = name;
        latitude = lat;
        longitude = lng;
        this.address = address;
        this.city = city;
        this.postal = postal;
    }

    public int getImageID(){
        return this.imageID;
    }

    public String getLocationName(){
        return this.locationName;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public double getLatitude() { return this.latitude; }

    public String getAddress(){ return address; }

    public  String getCity(){ return city; }

    @Override
    public String toString(){
        return (this.address + ", " + this.city + ", " + this.postal);
    }
}
