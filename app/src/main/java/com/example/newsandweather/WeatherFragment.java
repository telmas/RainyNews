package com.example.newsandweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class WeatherFragment extends Fragment implements LocationListener {

    String OPEN_WEATHER_MAP_API = "88d39728e018f174336a0188aa271645";
    TextView selectCity, cityField, detailsField, currentTemperatureField, humidity_field, pressure_field, weatherIcon, updatedField;
    RadioButton degreesCelsiusRadioButton, degreesFahrenheitRadioButton;
    CheckBox rememberCityCheckBox;
    RadioGroup radioGroup;
    Typeface weatherFont;
    ProgressBar loader;

    String city = "Paris, FR";
    DBHelper db;

    Button getLocationBtn;
    LocationManager locationManager;
    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_fragment, container, false);
    }

    @Override
    public void  onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        super.onCreate(savedInstanceState);

        uid = getArguments().getString("uid");

        loader = (ProgressBar) getView().findViewById(R.id.loader);
        selectCity = (TextView) getView().findViewById(R.id.selectCity);
        cityField = (TextView) getView().findViewById(R.id.city_field);
        updatedField = (TextView) getView().findViewById(R.id.updated_field);
        detailsField = (TextView) getView().findViewById(R.id.details_field);
        currentTemperatureField = (TextView) getView().findViewById(R.id.current_temperature_field);
        humidity_field = (TextView) getView().findViewById(R.id.humidity_field);
        pressure_field = (TextView) getView().findViewById(R.id.pressure_field);
        weatherIcon = (TextView) getView().findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "icons/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);
        radioGroup = (RadioGroup) getView().findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                taskLoadUp(city);
            }
        });
        degreesCelsiusRadioButton = (RadioButton) getView().findViewById(R.id.degreesCelsiusRadioButton);
        degreesFahrenheitRadioButton= (RadioButton) getView().findViewById(R.id.degreesFahrenheitRadioButton);

        rememberCityCheckBox = (CheckBox) getView().findViewById(R.id.saveCityCheckBoxID) ;
        db = new DBHelper(getContext());
        rememberCityCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                db.deleteCity(uid);
                if (isChecked) {
                    db.rememberCty(city, uid);
                }
            }
        });

        Cursor cursor = db.getCity(uid);
        if(cursor != null && cursor.moveToFirst() ){
            city = cursor.getString(cursor.getColumnIndex("city"));
            rememberCityCheckBox.setChecked(true);
            cursor.close();
        }

        taskLoadUp(city);

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Select City");
                final EditText input = new EditText(getContext());
                input.setText(city);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                alertDialog.setPositiveButton("Choose",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                city = input.getText().toString();
                                taskLoadUp(city);
                                db.deleteCity(uid);
                                if (rememberCityCheckBox.isChecked()) {
                                    db.rememberCty(city, uid);
                                }
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });

        getLocationBtn = (Button) getView().findViewById(R.id.getLocationBtn);
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();

            }
        });
    }

    public void taskLoadUp(String query) {
        if (!Utilities.hasNetworkAccess(Objects.requireNonNull(getContext()))) {
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        } else {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        }
    }

    class DownloadWeather extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loader.setVisibility(View.VISIBLE);
        }
        protected String doInBackground(String...args) {

            String unit;
            if(degreesCelsiusRadioButton.isChecked()){
                unit ="metric";
            } else {
                unit ="imperial";
            }

            String xml = Utilities.excuteget("http://api.openweathermap.org/data/2.5/weather?cnt=2&q=" + args[0] +
                    "&units="+unit+"&appid=" + OPEN_WEATHER_MAP_API);
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {

            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();

                    cityField.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));
                    detailsField.setText(details.getString("description").toUpperCase(Locale.US));
                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + (degreesCelsiusRadioButton.isChecked() ? "°C" : "°F"));
                    humidity_field.setText("Humidity: " + main.getString("humidity") + "%");
                    pressure_field.setText("Pressure: " + main.getString("pressure") + " hPa");
                    updatedField.setText(df.format(new Date(json.getLong("dt") * 1000)));
                    weatherIcon.setText(Html.fromHtml(Utilities.setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000)));

                    loader.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }
        }
    }


    void getLocation() {
        try {
            loader.setVisibility(View.VISIBLE);
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            String cityAndCountryString = addresses.get(0).getAddressLine(0);
            this.city = cityAndCountryString.substring(cityAndCountryString.indexOf(',') + 1).trim();
            taskLoadUp(city);
            db.deleteCity(uid);
            if (rememberCityCheckBox.isChecked()) {
                db.rememberCty(city, uid);
            }
        }catch(Exception e){
            Toast.makeText(getActivity(), "Couldn't retrieve location", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Please Enable GPS and Internet", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

}