package com.example.angel.cloudy.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.angel.cloudy.data.CloudyPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Esta Clase se usara para comunicarse con el servidor de clima.
 */
public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    // Se realizaron pruebas con la API Weatherbit
    private static final String DYNAMIC_WEATHER_URL = "http://api.weatherbit.io/v2.0/forecast/daily";

    private static final String FORECAST_BASE_URL = DYNAMIC_WEATHER_URL;

    /* Formato que queremos regrese la API Weatherbit maneja XML y JSON*/
    private static final String format = "json";
    /* Unidades de medicion que queremos que regrese la API */
    private static final String units = "M"; //M
    /* Numero de días que queremos regrese la API */
    private static final int numDays = 14;
    /*Llave generada en la API para realizar consultas*/
    private static final String apiKey = "4be6de292e2c40c6abe8be29a6f5b52d"; //35a24d3850bf4206353a8cf3b219f755
    /*Idioma en que se desea respuesta de la API soli afecta la descripcion del clima*/
    private static final String apiLang = "es";

    /*Parametros de ubicacion necesitados por la API*/
    final static String QUERY_PARAM = "city"; //q
    final static String LAT_PARAM = "lat";
    final static String LON_PARAM = "lon";
    final static String FORMAT_PARAM = "mode"; //mode
    final static String UNITS_PARAM = "units";
    final static String DAYS_PARAM = "days"; //cnt
    final static String API_KEY = "key"; //appid
    final static String API_LANG = "lang";


    public static URL getUrl(Context context) {
        if (CloudyPreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates = CloudyPreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude = preferredCoordinates[1];
            return buildUrlWithLatitudeLongitude(latitude, longitude);
        } else {
            String locationQuery = CloudyPreferences.getPreferredWeatherLocation(context);
            return buildUrlWithLocationQuery(locationQuery);
        }
    }

    /**Construye URL basado en Latitud y Longitud.*/
    private static URL buildUrlWithLatitudeLongitude(Double latitude, Double longitude) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, String.valueOf(latitude))
                .appendQueryParameter(LON_PARAM, String.valueOf(longitude))
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(API_KEY, apiKey)
                .appendQueryParameter(API_LANG,apiLang)
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**onstruye la URL para comunicarse con el Servidor Weatherbit API usando Ubicacion.*/
    private static URL buildUrlWithLocationQuery(String locationQuery) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(API_KEY, apiKey)
                .appendQueryParameter(API_LANG,apiLang)
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

     /**
      * Una vez generada la URL este metodo la usa para regresar la respuesta del HTTP
      * elimina espacios o caracteres extraños de JSON
     *
     * @param url URL para obtener la respuesta de HTTP.
     * @return Regresa el contenido de la respuesta de HTTP.
     * @throws IOException Si se detecta error en conexion o lectura dispara esxcepcion
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
