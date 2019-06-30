package at.karrer.energymanagementsystem.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Sitemap implements Parcelable {
    private String name;
    private String ip;
    private ArrayList<Item> items;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public Sitemap(String name, String ip, ArrayList<Item> items) {
        this.name = name;
        this.ip = ip;
        this.items = items;
    }

    public Sitemap(String name, String ip) {
        this.name = name;
        this.ip = ip;
        this.items = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeList(items);

    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Sitemap> CREATOR = new Parcelable.Creator<Sitemap>() {
        public Sitemap createFromParcel(Parcel in) {
            return new Sitemap(in);
        }

        public Sitemap[] newArray(int size) {
            return new Sitemap[size];
        }
    };

    @Override
    public String toString() {
        return "Sitemap{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", items=" + items +
                '}';
    }

    @SuppressWarnings("unchecked")
    private Sitemap(Parcel in) {
        name = in.readString();
        ip = in.readString();
        items = in.readArrayList(Item.class.getClassLoader());
    }
}
