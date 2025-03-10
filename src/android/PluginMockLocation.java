package com.ccervantesb.mocklocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class echoes a string called from JavaScript.
 */
public class PluginMockLocation extends CordovaPlugin {

    private static final int REQUEST_CHECK_MOCK_LOCATION = 1;
    private static final int REQUEST_DISABLE_MOCK_LOCATION = 2;
    private CallbackContext callbackContext;
    private boolean getInfoFromLocation = false;
    private JSONObject resultJSON;
    private String testProvider;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("checkMockLocation")) {
            if(!args.isNull(0)) {
                this.getInfoFromLocation = args.getBoolean(0);
            }
            this.checkMockLocation(callbackContext);
            return true;
        }
        else if (action.equals("setMockLocation")) {
            JSONObject params;
            String provider = null;
            Double latitude = null;
            Double longitude = null;
            if(!args.isNull(0)) {
                params = args.getJSONObject(0);
                provider = params.getString("provider");
                latitude = params.getDouble("latitude");
                longitude = params.getDouble("longitude");
            }
            if (provider != null && latitude != null && longitude != null) {
                this.setMockLocation(callbackContext, provider, latitude, longitude);
            }
            else {
                handleError(new JSONObject(), "INVALID_OR_MISSING_PARAMS", "Invalid or missing params.", callbackContext);
            }
            return true;
        }
        else if (action.equals("disableMockLocation")) {
            this.testProvider = null;
            if(!args.isNull(0)) {
                testProvider = args.getString(0);
            }
            if (testProvider != null) {
                this.disableMockLocation(callbackContext, testProvider);
            }
            else {
                handleError(new JSONObject(), "INVALID_OR_MISSING_PARAMS", "Invalid or missing params.", callbackContext);
            }
            return true;
        }
        else if (action.equals("isAvailable")) {
            Boolean isAvailable = isMockLocationAvailable();
            // Create result JSON
            resultJSON = new JSONObject();
            resultJSON.put("isAvailable", isAvailable);
            // Send plugin result
            PluginResult result = new PluginResult(PluginResult.Status.OK, resultJSON);
            callbackContext.sendPluginResult(result);
            return true;
        }
        return false;
    }

    private void checkMockLocation(CallbackContext callbackContext) {
        if (this.checkLocationPermissions()) {
            detectMockLocation(callbackContext);
        } else {
            cordova.requestPermission(this, REQUEST_CHECK_MOCK_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void detectMockLocation(CallbackContext callbackContext) {
        this.resultJSON = new JSONObject();

        LocationManager locationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            handleError(this.resultJSON, "LOCATION_MANAGER_NOT_FOUND", "\"LocationManager\" not found.", callbackContext);
        } else {
            Location location = null;
            String usedProvider = "";
            List<String> providers = locationManager.getProviders(true);

            for (String provider : providers) {
                Location locationFromProvider = locationManager.getLastKnownLocation(provider);

                if (locationFromProvider != null
                        && (location == null || locationFromProvider.getAccuracy() < location.getAccuracy())) {
                    location = locationFromProvider;
                    usedProvider = provider;
                }
            }

            if (location != null) {
                boolean isMockLocation = (Build.VERSION.SDK_INT < 31) ? location.isFromMockProvider() : location.isMock();
                try {
                    this.resultJSON.put("isMockLocation", isMockLocation);
                    Log.d("DetectMockLocation", "Used location provider is: " + usedProvider);
                    if(this.getInfoFromLocation && isMockLocation) {
                        JSONObject mockLocation = getLocationInfo(location);
                        this.resultJSON.put("mockLocation", mockLocation);
                        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (gpsLocation != null) {
                            resultJSON.put("gpsLocation", getLocationInfo(gpsLocation));
                        }
                        else {
                            resultJSON.put("gpsLocation", new JSONObject());
                        }
                        PluginResult result = new PluginResult(PluginResult.Status.OK, resultJSON);
                        callbackContext.sendPluginResult(result);
                    }
                    else {
                        PluginResult result = new PluginResult(PluginResult.Status.OK, this.resultJSON);
                        callbackContext.sendPluginResult(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                handleError(this.resultJSON, "LOCATION_NOT_FOUND", "\"Location\" object not found.", callbackContext);
            }
        }
    }

    private boolean checkLocationPermissions() {
        return cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || cordova.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CHECK_MOCK_LOCATION:
                    detectMockLocation(callbackContext);
                    break;
                case REQUEST_DISABLE_MOCK_LOCATION:
                    disableMockLocation(callbackContext, testProvider);
                    break;
            }
        }
        else {
            handleError(new JSONObject(), "PERMISSION_DENIED", "Location permission denied.", callbackContext);
        }
    }

    private void handleError(JSONObject resultJSON, String code, String message, CallbackContext callbackContext) {
        try {
            JSONObject error = new JSONObject();
            error.put("code", code);
            error.put("message", message);
            resultJSON.put("isMockLocation", "");
            resultJSON.put("error", error);
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, resultJSON);
            callbackContext.sendPluginResult(result);
        } catch (JSONException e) {
            Log.e("PluginMockLocation", "Error creating JSON Object", e);
        }
    }

    private JSONObject getLocationInfo(Location location) {
        JSONObject result = new JSONObject();
        try {
            result.put("latitude", location.getLatitude());
            result.put("longitude", location.getLongitude());
            result.put("accuracy", location.getAccuracy());
            result.put("altitude", location.getAltitude());
            result.put("provider", location.getProvider());
        }
        catch (JSONException e) {
            Log.e("PluginMockLocation", "Error creating JSON Object", e);
        }
        return result;
    }

    private void setMockLocation(CallbackContext callbackContext, String provider, Double latitude, Double longitude) {
        if (isMockLocationAvailable()) {
            try {
                LocationManager locationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
                // Disable provider
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.setTestProviderEnabled(provider, false);
                }
                // Remove provider
                locationManager.removeTestProvider(provider);
                // Add test provider
                locationManager.addTestProvider(provider, false, false, false, false, false, false, false, ProviderProperties.POWER_USAGE_MEDIUM, ProviderProperties.ACCURACY_FINE);
                // Create custom location
                Location customLocation = new Location(provider);
                customLocation.setLatitude(latitude);
                customLocation.setLongitude(longitude);
                customLocation.setAccuracy(1);
                customLocation.setAltitude(0);
                customLocation.setTime(System.currentTimeMillis());
                customLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                customLocation.setProvider(provider);
                // Add provider location
                locationManager.setTestProviderLocation(provider, customLocation);
                // Enable provider
                locationManager.setTestProviderEnabled(provider, true);
                // Check mock location
                this.getInfoFromLocation = true;
                checkMockLocation(callbackContext);
            }
            catch (Exception e) {
                e.printStackTrace();
                handleError(new JSONObject(), "ERR_SET_MOCK_LOCATION", e.getMessage(), callbackContext);
            }
        }
        else {
            handleError(new JSONObject(), "NOT_ALLOWED_SET_MOCK_LOCATION", "Set mock location is not allowed.", callbackContext);
        }
    }

    @SuppressLint("MissingPermission")
    private void disableMockLocation(CallbackContext callbackContext, String provider) {
        if(!checkLocationPermissions()) {
            cordova.requestPermission(this, REQUEST_DISABLE_MOCK_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (isMockLocationAvailable()) {
            try {
                LocationManager locationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
                // Check if provider exist
                LocationProvider testProvider = locationManager.getProvider(provider);
                if (testProvider != null) {
                    // Disable provider
                    locationManager.setTestProviderEnabled(provider, false);
                    locationManager.clearTestProviderEnabled(provider);
                    // Remove provider
                    locationManager.removeTestProvider(provider);
                    // Force to check network location
                    getInfoFromLocation = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        locationManager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, cordova.getActivity().getMainExecutor(), new Consumer<Location>() {
                            @Override
                            public void accept(Location location) {
                                checkMockLocation(callbackContext);
                            }
                        });
                    } else {
                        LocationListener listener = new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                checkMockLocation(callbackContext);
                            }
                        };
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, cordova.getActivity().getMainLooper());
                    }
                }
                else {
                    handleError(new JSONObject(), "PROVIDER_NOT_FOUND", "Provider \"" + provider + "\" not found.", callbackContext);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                handleError(new JSONObject(), "ERR_SET_MOCK_LOCATION", e.getMessage(), callbackContext);
            }
        }
        else {
            handleError(new JSONObject(), "NOT_ALLOWED_SET_MOCK_LOCATION", "Set mock location is not allowed.", callbackContext);
        }
    }

    private boolean isMockLocationAvailable() {
        AppOpsManager appOpsManager = (AppOpsManager) cordova.getActivity().getSystemService(Context.APP_OPS_SERVICE);
        int uid = cordova.getActivity().getApplication().getApplicationInfo().uid;
        int allowMockLocation = appOpsManager.noteOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, uid, cordova.getContext().getPackageName());
        return allowMockLocation == AppOpsManager.MODE_ALLOWED;
    }
}
