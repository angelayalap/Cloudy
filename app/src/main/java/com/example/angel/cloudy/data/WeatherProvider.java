package com.example.angel.cloudy.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.angel.cloudy.utilities.CloudyDateUtils;

public class WeatherProvider extends ContentProvider {


    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;


    public static UriMatcher buildUriMatcher() {


        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        return matcher;
    }

    @Override  /** Inicializa el content provider*/
    public boolean onCreate() {

        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override  /** Maneja los ingresos de nuevas filas */
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_WEATHER:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long weatherDate =
                                value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!CloudyDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("La fecha es incorrecta");
                        }

                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsInserted;
            default: return super.bulkInsert(uri, values);
        }
    }

    @Override // Maneja peticiones
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_WEATHER_WITH_DATE: {

                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case CODE_WEATHER: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("URI desconocida: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override /** Borra la infomacion dada una URI*/
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int numRowsDeleted;
        if (null == selection) selection = "1";

        switch (sUriMatcher.match(uri)) {
            case CODE_WEATHER:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("URI desconocida: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("No se implementa getType.");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException("No se implementa getType");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("No se implementa getType");
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}