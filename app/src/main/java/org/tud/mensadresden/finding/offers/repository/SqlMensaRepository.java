package org.tud.mensadresden.finding.offers.repository;

import android.location.Location;

import org.tud.mensadresden.finding.offers.model.Mensa;

import java.util.List;

public class SqlMensaRepository implements MensaRepository {
    @Override
    public List<Mensa> findAllOrderedByDistance(Location location) {
        return null;
    }

    @Override
    public void insertAll(List<Mensa> mensas) {

    }

    @Override
    public void update(Mensa mensa) {

    }

    @Override
    public void deleteAll() {

    }
}
