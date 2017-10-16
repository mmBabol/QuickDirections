package Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ListView.LocationRow;

/**
 * Created by Mat on 11/27/2016.
 *
 * SQLite database, methods to access, modify, and delete from database
 */

public class DataSource {
    private SQLiteDatabase database; // the SQLite database
    private MySQLiteHelper dbHelper; // a helper to create and manage the database

    // column names
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_IMG,
            MySQLiteHelper.COLUMN_NAME,
            MySQLiteHelper.COLUMN_LATITUDE,
            MySQLiteHelper.COLUMN_LONGITUDE,
            MySQLiteHelper.COLUMN_ADDRESS,
            MySQLiteHelper.COLUMN_CITY,
            MySQLiteHelper.COLUMN_POSTAL,
    };

    public DataSource(Context context) {
        dbHelper = new MySQLiteHelper ( context );
    }

    // the database is created and opened via SQLitOpenHelper
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    // close the database via SQLiteOpenHelper
    public void close() {
        dbHelper.close();
    }

    // Retrieve all locations
    public Cursor getAllLocations() {
        Cursor c = database.rawQuery("SELECT * FROM " + MySQLiteHelper.TABLE_NAME, null);
        if(c!=null)
            c.moveToFirst();
        return c;
    }

    // Add new location to database, receive all information relating to location for saving
    public Cursor addLocation(Integer img, String name, double latitude,
                              double longitude, String address, String city,
                              String postal) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_IMG, img);
        values.put(MySQLiteHelper.COLUMN_NAME, name);
        values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
        values.put(MySQLiteHelper.COLUMN_ADDRESS, address);
        values.put(MySQLiteHelper.COLUMN_CITY, city);
        values.put(MySQLiteHelper.COLUMN_POSTAL, postal);

        // INSERT a ContentValues object containing data about the new comment
        // primary key of value inserted is returned
        long insertId = database.insert(MySQLiteHelper.TABLE_NAME, null, values);

        Cursor cursor = getAllLocations();
        return cursor;
    }

    // Delete location from database, receive ID val as a long, and delete that from database
    public Cursor deleteLocation(long del) {
        Cursor cursor = getAllLocations();
        cursor.moveToFirst();

        long id = cursor.getLong(0);

        database.delete(MySQLiteHelper.TABLE_NAME, MySQLiteHelper.COLUMN_ID
                + " = " + del, null);

        cursor = getAllLocations();
        return cursor;
    }

    // Retrieve a location, receive ID val as long, search for location inside the database,
    // create a Location object for sending back
    public LocationRow getLocation(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        LocationRow location;
        String idVal = Long.toString(id);

        Cursor cursor = db.query(MySQLiteHelper.TABLE_NAME,
                new String[] {allColumns[0], allColumns[1], allColumns[2], allColumns[3], allColumns[4], allColumns[5], allColumns[6], allColumns[7]},
                "_id=?",    /* selection */
                new String[]{idVal},               /* selection args */
                null,
                null,
                null,
                null);

        if (cursor != null)
            cursor.moveToFirst();

        location = new LocationRow(0 ,cursor.getString(2), cursor.getDouble(3), cursor.getDouble(4),
                cursor.getString(5), cursor.getString(6), cursor.getString(7));

        cursor.close();
        db.close();
        return location;
    }
}
