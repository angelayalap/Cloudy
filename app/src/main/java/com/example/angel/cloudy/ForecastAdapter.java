package com.example.angel.cloudy;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.angel.cloudy.utilities.CloudyDateUtils;
import com.example.angel.cloudy.utilities.CloudyWeatherUtils;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private final Context mContext;
    private final ForecastAdapterOnClickHandler mClickHandler;
    private Cursor mCursor;
    private boolean mUseTodayLayout;

    //Interfaz que recibe mensajes onClick
    public interface ForecastAdapterOnClickHandler {
        void onClick(long date);
    }

    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        final ImageView iconView;

        ForecastAdapterViewHolder(View view) {
            super(view);
            // Se referencian todas las vistas

            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);

            view.setOnClickListener(this);
        }

        /**
         * Cuando se da click en una vista pasamos la fecha de dicha vista
         * @param v La vista que se dio click
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }

    /**
     * El siguiente Override es llamado cada que un ViewHolder es creado.
     *
     * @param viewGroup Recibe el ViewGroup donde esta contenido el ViewHolders.
     * @param viewType  Recibe el tipo de elemento.
     * @return Regresa un nuevo ForecastAdapterViewHolder que contendra la vista de cada elemento.
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId;

        switch (viewType) {
            case VIEW_TYPE_TODAY: { // Si la vista es hoy, se usa el layout de hoy
                layoutId = R.layout.list_item_forecast_today;
                break;
            }

            case VIEW_TYPE_FUTURE_DAY: { // Si la vista es un dia futuro, se muestra layout adecuado
                layoutId = R.layout.forecast_list_item;
                break;
            }
            default:  throw new IllegalArgumentException("Vista no valida " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);

        return new ForecastAdapterViewHolder(view);
    }


    /**
     * OnBindViewHolder  es llamado para mostrar la info en determinada posicion
     * En este metodo se actualiza el contenido del ViewHolder para mostrar los detalles del clima
     *
     * @param forecastAdapterViewHolder Recibe el ViewHolder  que sera actualizado para representar el contenido
     *                                  el elemento dada una posicion.
     * @param position                  Recibe la posicion del elemento.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        /****************
         * Icono de Clima*
         ****************/
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;
        int viewType = getItemViewType(position);

        switch (viewType) {
            case VIEW_TYPE_TODAY: // Si el ViewType es hoy, muestra icono grande
                weatherImageId = CloudyWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);
                break;

            case VIEW_TYPE_FUTURE_DAY:// Si el ViewType no es hoy, muestra icono pequeño
                weatherImageId = CloudyWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);
                break;

            default:
                throw new IllegalArgumentException("Vista Invalida " + viewType);
        }
        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        /****************
         * Fecha         *
         ****************/

        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE); // Lee la fecha de cursor
        String dateString = CloudyDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        forecastAdapterViewHolder.dateView.setText(dateString); // Se muestra la fecha de manera amigable

        /***********************
         * Descripción Clima   *
         ***********************/
        String description = CloudyWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);
        forecastAdapterViewHolder.descriptionView.setText(description);
        forecastAdapterViewHolder.descriptionView.setContentDescription(descriptionA11y);

        /***************
         * Temp maxima *
         ***************/

        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);

        //Cambia formato de acuerdo a preferencias de usuario
        String highString = CloudyWeatherUtils.formatTemperature(mContext, highInCelsius);
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);
        forecastAdapterViewHolder.highTempView.setText(highString);
        forecastAdapterViewHolder.highTempView.setContentDescription(highA11y);

        /***************
         * Temp minima *
         ***************/

        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        //Cambia formato de acuerdo a preferencias de usuario
        String lowString = CloudyWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);
        forecastAdapterViewHolder.lowTempView.setText(lowString);
        forecastAdapterViewHolder.lowTempView.setContentDescription(lowA11y);
    }

    @Override // Este metodo regresa el numero de elementos a mostrar
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override //Regresa codigo de vista de acuerdo a posicion
    public int getItemViewType(int position) {

        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    void swapCursor(Cursor newCursor) { // Crea nuevo cursor cuando hay datos nuevos
        mCursor = newCursor;
        notifyDataSetChanged();
    }

}