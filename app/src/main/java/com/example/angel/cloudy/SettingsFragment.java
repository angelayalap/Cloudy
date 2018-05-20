package com.example.angel.cloudy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.example.angel.cloudy.data.CloudyPreferences;
import com.example.angel.cloudy.data.WeatherContract;
import com.example.angel.cloudy.sync.CloudySyncUtils;

/**
 * La clase SettingsFragment sirve para mostrar los configuraciones o preferencias del usuario
 * En este caso se puede cambiar las unidades de medicion del sistema metrico al imperial
 * y definir la ubicacion, asi como habilitar notificaciones.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements

        SharedPreferences.OnSharedPreferenceChangeListener {

    // El metodo muestra un resumen de las preferencias del usuario
    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);

            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
    }


    @Override // Se agrega el xml de las preferencias de usuario
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.user_preferences);
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {//  Se define cada preferencia que no tiene checkbox
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override // Actualiza preferencias cuando hay cambios
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();

        if (key.equals(getString(R.string.pref_location_key))) {

            CloudyPreferences.resetLocationCoordinates(activity);
            CloudySyncUtils.startImmediateSync(activity);

        } else if (key.equals(getString(R.string.pref_units_key))) {

            activity.getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        }
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }
}