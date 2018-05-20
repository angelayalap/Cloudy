package com.example.angel.cloudy.utilities;

import android.content.Context;
import android.text.format.DateUtils;

import com.example.angel.cloudy.R;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class CloudyDateUtils {
    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    /*** Este metodo regresa el numero de dia en formato UTC (Universal Time Coordinated) tomando el dia local
     *
     * @param date Recibe la hora locan en milisegundos.
     * @return Regresa el numero de días en dformato UTC.
     */
    public static long getDayNumber(long date) {
        TimeZone tz = TimeZone.getDefault();
        long gmtOffset = tz.getOffset(date);
        return (date + gmtOffset) / DAY_IN_MILLIS;
    }

    /*** Metodo que normaliza la fecha
     *
     * @param date Recibe la fecha a normalizar
     * @return Regresa la fecha UTC normalzada
     */
    public static long normalizeDate(long date) {

        long retValNew = date / DAY_IN_MILLIS * DAY_IN_MILLIS;
        return retValNew;
    }

    public static boolean isDateNormalized(long millisSinceEpoch) {
        boolean isDateNormalized = false;
        if (millisSinceEpoch % DAY_IN_MILLIS == 0) {
            isDateNormalized = true;
        }

        return isDateNormalized;
    }
    /**
     *
     * Este metodo convierte le fecha UTC en fecha local, usando TimeZone offset.
     *
     * @param utcDate Reciebe la fecha UTC que se convertira a fecha local.
     * @return Regresa la fecha local ( fecha UTC - TimeZone offset) .
     */
    public static long getLocalDateFromUTC(long utcDate) {
        TimeZone tz = TimeZone.getDefault();
        long gmtOffset = tz.getOffset(utcDate);
        return utcDate - gmtOffset;
    }

    /**
     * Este método getFriendlyDateString convierte la fecha almacenada en algo más presentable.
     *
     * Se usa la siguiente logica:
     * Para hoy : "Hoy, 8 de Abril"
     * Para mañana:  "Mañana"
     * Para proximos 5 dias: "Lunes" (Solo el nombre del día)
     * Para todos los siguientes días: "Lun, 10 de Abril"
     *
     * @param context      Usado para la ubicacion
     * @param dateInMillis Fecha en  formato UTC
     * @param showFullDate Para mostrar la fecha completa.
     *
     * @return Regresa una fecha en forma amigable como "Hoy, 8 de Abril", en lugar de 20180408
     */
    public static String getFriendlyDateString(Context context, long dateInMillis, boolean showFullDate) {

        long localDate = getLocalDateFromUTC(dateInMillis);
        long dayNumber = getDayNumber(localDate);
        long currentDayNumber = getDayNumber(System.currentTimeMillis());

        if (dayNumber == currentDayNumber || showFullDate) {

            String dayName = getDayName(context, localDate);
            String readableDate = getReadableDateString(context, localDate);

            if (dayNumber - currentDayNumber < 2) {

                String localizedDayName = new SimpleDateFormat("EEEE").format(localDate);
                return readableDate.replace(localizedDayName, dayName);
            } else {
                return readableDate;
            }
        } else if (dayNumber < currentDayNumber + 7) {

            return getDayName(context, localDate);
        } else {
            int flags = DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_NO_YEAR
                    | DateUtils.FORMAT_ABBREV_ALL
                    | DateUtils.FORMAT_SHOW_WEEKDAY;

            return DateUtils.formatDateTime(context, localDate, flags);
        }
    }

    /** Este método regresa un  a fecha sin año, muestra el dia de la semana completo*/
    private static String getReadableDateString(Context context, long timeInMillis) {
        int flags = DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_NO_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY;

        return DateUtils.formatDateTime(context, timeInMillis, flags);
    }

    /** Este metodo regresa el nombre del día. Ejemplo "Hoy", "Mañana", "Lunes"*/
    private static String getDayName(Context context, long dateInMillis) {

        long dayNumber = getDayNumber(dateInMillis);
        long currentDayNumber = getDayNumber(System.currentTimeMillis());

        if (dayNumber == currentDayNumber) {
            return context.getString(R.string.today);
        } else if (dayNumber == currentDayNumber + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            /* Si es cualquier otro dia, se muestra solo el dia de la semana*/
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static long getNormalizedUtcDateForToday() {

        long utcNowMillis = System.currentTimeMillis();
        TimeZone currentTimeZone = TimeZone.getDefault();
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);
        long normalizedUtcMidnightMillis = TimeUnit.DAYS.toMillis(daysSinceEpochLocal);
        return normalizedUtcMidnightMillis;
    }

}
