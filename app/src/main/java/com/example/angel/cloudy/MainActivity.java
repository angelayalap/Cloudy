package com.example.angel.cloudy;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import com.example.angel.cloudy.data.CloudyPreferences;
import com.example.angel.cloudy.data.WeatherContract;
import com.example.angel.cloudy.sync.CloudySyncUtils;
import com.example.angel.cloudy.ForecastAdapter.ForecastAdapterOnClickHandler;


public class MainActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, ForecastAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private ProgressBar mLoadingIndicator;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ID_FORECAST_LOADER = 44; // como buena practica se inicializa el Loader responsable  de cargar el pronostico

    // Esta es la información que nos interesa ver en el MainActivity
    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };
    // Guardamos el indice  del array anterior para acceder de forma mas rapida a los datos de nuestra consulta
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);

        /* Referenciamos el ReclyclerView al xml*/
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator);

        /*Creamos un LinearLauoyt (Vertical) para manejar el RecyclerView*/
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        /*ForecastAdapter es responsable de relacionar los datos con las vistas.*/
        mForecastAdapter = new ForecastAdapter(this, this);
        mRecyclerView.setAdapter(mForecastAdapter);

        showLoading();
        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);

        CloudySyncUtils.initialize(this);

    }


    /**Metodo que usa esquema URI para mostrar ubicacion en mapa
     * @referencia <a"https://developer.android.com/guide/components/intents-common.html#Maps">*/
    private void openPreferredLocationInMap() {
        double[] coords = CloudyPreferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else{
            Log.d(TAG, "No se pudo llamar " + geoLocation.toString()
                    + ", No hay aplicaccion instalada");
        }
    }

    /*** Inicializa  y regresa un nuevo Loader */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_FORECAST_LOADER:

                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this, forecastQueryUri, MAIN_FORECAST_PROJECTION, selection, null, sortOrder);
            default: throw new RuntimeException("Loader no implementado: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mForecastAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showWeatherDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onClick(long date) {

        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    private void showWeatherDataView() {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {

        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater(); /* Se usa getMenuInflater para manejar el menu inflater*/
        inflater.inflate(R.menu.forecast, menu);/* Se usa el metodo inflate del infflater para el layout del menu*/
        return true;  /* Regresa True para que el menu sea mostrado en Toolbar */
    }

    // Override onOptionsItemSelected para manejar los Menus
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
