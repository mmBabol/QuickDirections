package Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Mat on 11/27/2016.
 *
 * SQLite database table definition
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    // database table name; column names of the table
    public static final String TABLE_NAME = "Locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMG = "imgID";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_POSTAL = "postal";

    // database name; database version number
    private static final String DATABASE_NAME = "directions.db";
    private static final int DATABASE_VERSION = 1;

    // SQL statement to create a database table
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_IMG + " integer not null, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_LATITUDE + " double not null, "
            + COLUMN_LONGITUDE + " double not null, "
            + COLUMN_ADDRESS + " text not null, "
            + COLUMN_CITY + " text not null, "
            + COLUMN_POSTAL + " text not null "
            + ")";

    // Create database
    public MySQLiteHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // upgrade the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w( MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(db);
    }
}
