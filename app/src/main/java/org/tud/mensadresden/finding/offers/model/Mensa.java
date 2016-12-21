package org.tud.mensadresden.finding.offers.model;

public class Mensa {
    private int id;
    private String name;
    private String city;
    private String address;
    private SerializableLocation coordinates;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SerializableLocation getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(SerializableLocation coordinates) {
        this.coordinates = coordinates;
    }
}
