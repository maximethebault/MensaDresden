package org.tud.mensadresden.model.service.mensa;

import android.location.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class LocationDeserializer implements JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull())
            return null;

        double x = json.getAsJsonArray().get(0).getAsDouble();
        double y = json.getAsJsonArray().get(1).getAsDouble();

        Location location = new Location("");
        location.setLongitude(y);
        location.setLatitude(x);
        return location;
    }

}