package org.tud.mensadresden.finding.offers.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import org.tud.mensadresden.DatabaseHelper;
import org.tud.mensadresden.finding.offers.model.Mensa;

import java.util.ArrayList;
import java.util.List;

public class SqlMensaRepository implements MensaRepository {
    private static SqlMensaRepository mInstance;

    private DatabaseHelper databaseHelper;

    private SqlMensaRepository(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static synchronized SqlMensaRepository getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SqlMensaRepository(context);
        }
        return mInstance;
    }

    @Override
    public Mensa findById(String id) {

        Cursor cursor = databaseHelper.getReadableDatabase().query("mensa", null, "id = ?", new String[]{id}, null, null, null);

        int indexId = cursor.getColumnIndexOrThrow("id");
        int indexName = cursor.getColumnIndexOrThrow("name");
        int indexAddress = cursor.getColumnIndexOrThrow("address");
        int indexLatitude = cursor.getColumnIndexOrThrow("latitude");
        int indexLongitude = cursor.getColumnIndexOrThrow("longitude");

        Mensa mensa = new Mensa();
        while (cursor.moveToNext()) {
            mensa.setId(cursor.getInt(indexId));
            mensa.setName(cursor.getString(indexName));
            mensa.setAddress(cursor.getString(indexAddress));
            Location location = new Location("");
            location.setLongitude(cursor.getDouble(indexLongitude));
            location.setLatitude(cursor.getDouble(indexLatitude));
            mensa.setCoordinates(location);
        }

        cursor.close();

        return mensa;
    }

    @Override
    public List<Mensa> findAllOrderedByDistance(Location locationCriteria) {
        String orderBy = null;
        if (locationCriteria != null) {
            double fudge = Math.pow(Math.cos(Math.toRadians(locationCriteria.getLatitude())), 2);
            orderBy = "((" + locationCriteria.getLatitude() + " - latitude) * (" + locationCriteria.getLatitude() + " - latitude) +" +
                    "(" + locationCriteria.getLongitude() + " - longitude) * (" + locationCriteria.getLongitude() + " - longitude) * " + fudge + ")";
        }

        Cursor cursor = databaseHelper.getReadableDatabase().query("mensa", null, null, null, null, null, orderBy);

        int indexId = cursor.getColumnIndexOrThrow("id");
        int indexName = cursor.getColumnIndexOrThrow("name");
        int indexAddress = cursor.getColumnIndexOrThrow("address");
        int indexLatitude = cursor.getColumnIndexOrThrow("latitude");
        int indexLongitude = cursor.getColumnIndexOrThrow("longitude");

        List<Mensa> mensas = new ArrayList<>();
        while (cursor.moveToNext()) {
            Mensa mensa = new Mensa();
            mensa.setId(cursor.getInt(indexId));
            mensa.setName(cursor.getString(indexName));
            mensa.setAddress(cursor.getString(indexAddress));
            Location location = new Location("");
            location.setLongitude(cursor.getDouble(indexLongitude));
            location.setLatitude(cursor.getDouble(indexLatitude));
            mensa.setCoordinates(location);
            mensas.add(mensa);
        }

        cursor.close();

        return mensas;
    }

    @Override
    public void insertAll(List<Mensa> mensas) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        for (Mensa mensa : mensas) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", mensa.getId());
            contentValues.put("name", mensa.getName());
            contentValues.put("address", mensa.getAddress());
            contentValues.put("latitude", mensa.getCoordinates().getLatitude());
            contentValues.put("longitude", mensa.getCoordinates().getLongitude());
            database.insert("mensa", null, contentValues);
        }
    }

    @Override
    public void update(Mensa mensa) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        databaseHelper.getWritableDatabase().delete("mensa", null, null);
    }
}
