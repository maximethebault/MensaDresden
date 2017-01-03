package org.tud.mensadresden.model.repository;

import android.location.Location;

import org.tud.mensadresden.model.entity.Mensa;

import java.util.List;

public interface MensaRepository {
    Mensa findById(String id);

    List<Mensa> findAllOrderedByDistance(Location location);

    void insertAll(List<Mensa> mensas);

    void update(Mensa mensa);

    void deleteAll();
}
