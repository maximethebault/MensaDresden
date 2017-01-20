package org.tud.mensaapp.model.entity;

import java.util.List;
import java.util.Map;

public class Meal {
    private int id;
    private String name;
    private List<String> notes;
    private Map<String, Float> prices;
    private String category;

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

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public Map<String, Float> getPrices() {
        return prices;
    }

    public void setPrices(Map<String, Float> prices) {
        this.prices = prices;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
