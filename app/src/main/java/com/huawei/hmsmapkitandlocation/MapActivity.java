package com.huawei.hmsmapkitandlocation;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.GeocoderService;
import com.huawei.hms.location.Geofence;
import com.huawei.hms.location.GeofenceRequest;
import com.huawei.hms.location.GeofenceService;
import com.huawei.hms.location.GetFromLocationRequest;
import com.huawei.hms.location.HWLocation;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hmsmapkitandlocation.pushService.GeoFenceBrodcastReceiver;
import com.huawei.hmsmapkitandlocation.ui.CustomInfoWindowAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String[] RUNTIME_LOCATION_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int REQUEST_CODE_PERMISSIONS = 10001;

    private HuaweiMap hMap;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeofenceService geofenceService;
    private GeocoderService geocoderService;
    private List<HWLocation> geocoderList;
    private int geocoderMaxResults = 10;
    private ArrayList<Geofence> geofenceList;

    private Bundle bundleSavedinstanceState;
    private double latitude = -33.4176004;
    private double longtude = -70.6059687;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        askForPermissions(this, RUNTIME_LOCATION_PERMISSIONS);

        bundleSavedinstanceState = savedInstanceState;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationRequest();
            createGeofence();
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private void askForPermissions(Activity activity, String[] permissions) {
        List<String> permissionsList = checkPermissions(activity, permissions);
        if (!permissionsList.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_PERMISSIONS);
        } else {
            locationRequest();
            createGeofence();
        }

    }

    private void createGeofence() {
        if(completeGeofenceList().size() > 0){
            geofenceService = LocationServices.getGeofenceService(getApplicationContext());
            geofenceService.createGeofenceList(getGeofencingRequest(), getPendingIntent())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful()) {
                                Log.i(TAG, "ADD GEOFENCE SUCCESS ");
                            } else {
                                Log.i(TAG, "ADD GEOFENCE FAILED "+task.getException().getMessage());
                            }
                        }
                    });
        } else {
            Log.i(TAG, "ERROR - GEOFENCE NOT CREATED");
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, GeoFenceBrodcastReceiver.class);
        intent.setAction(GeoFenceBrodcastReceiver.ACTION_PROCESS_LOCATION);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
    }

    private GeofenceRequest getGeofencingRequest() {
        return new GeofenceRequest.Builder()
                .setInitConversions(GeofenceRequest.ENTER_INIT_CONVERSION)
                .createGeofenceList(geofenceList)
                .build();
    }

    private ArrayList<Geofence> completeGeofenceList() {
        Geofence.Builder geoBuild = new Geofence.Builder();
        geofenceList = new ArrayList<Geofence>();
        geofenceList.add(
                geoBuild.setUniqueId("COSTANERA - NUEVA TOBALABA")
                        .setRoundArea(-33.4195684, -70.60506868, 200)
                        .setValidContinueTime(Geofence.GEOFENCE_NEVER_EXPIRE)
                        .setConversions(Geofence.ENTER_GEOFENCE_CONVERSION)
                        .setDwellDelayTime(1000).build());

        return geofenceList;
    }

    private void locationRequest() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        LocationRequest mLocationRequest = new LocationRequest();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);
        LocationCallback mLocationCallback;

        mLocationCallback = new LocationCallback() {
            public void onLocationResults(LocationResult locationResult) {
                if (locationResult != null) {
                    List<Location> locations = locationResult.getLocations();

                    geocoderServiceInit(locations, bundleSavedinstanceState);
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdatesEx(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "SUCCESS");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i(TAG, "ERROR");
                    }
                });

    }

    private void geocoderServiceInit(List<Location> locations, Bundle savedInstanceState) {
        geocoderService = LocationServices.getGeocoderService(getApplicationContext(), new Locale("es", "CL"));

        GetFromLocationRequest getFromLocationRequest =
                new GetFromLocationRequest(
                        locations.get(0).getLatitude(),
                        locations.get(0).getLongitude(),
                        geocoderMaxResults);

        geocoderService.getFromLocation(getFromLocationRequest)
                .addOnSuccessListener(new OnSuccessListener<List<HWLocation>>() {
                    @Override
                    public void onSuccess(List<HWLocation> hwLocations) {
                        geocoderList = hwLocations;

                        latitude = getFromLocationRequest.getLatitude();
                        longtude = getFromLocationRequest.getLongitude();

                        createNewMap(savedInstanceState);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.i(TAG, "FAIL "+e);
                    }
                });
    }

    private static List<String> checkPermissions(Activity activity, String[] permissions) {
        List<String> permissionsList = new ArrayList<>();
        for(String permission: permissions) {
            int permissionState = activity.checkSelfPermission(permission);
            if (permissionState == PackageManager.PERMISSION_DENIED) {
                permissionsList.add(permission);
            }
        }
        return permissionsList;
    }

    private void createNewMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if(savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hMap = huaweiMap;
        hMap.setMyLocationEnabled(true);
        hMap.getUiSettings().setMyLocationButtonEnabled(true);

        float zoom = 18.0f;
        LatLng latlng = new LatLng(latitude, longtude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, zoom);
        hMap.moveCamera(cameraUpdate);

        addGeocoderMarker(hMap);

    }

    private HuaweiMap addGeocoderMarker(HuaweiMap hMap) {
        for(int x=0;geocoderList.size()>x;x++) {
            String snippet = "Comuna: " + geocoderList.get(x).getCity() + "\n" +
                    "Código: " + geocoderList.get(x).getCountryCode() + "\n" +
                    "País: " + geocoderList.get(x).getCountryName() + "\n" +
                    "Dirección: " + geocoderList.get(x).getStreet() + "\n" +
                    "Cod. Postal : " + geocoderList.get(x).getPostalCode() + "\n" +
                    geocoderList.get(x).getFeatureName();


            hMap.addMarker(new MarkerOptions()
                    .position(new LatLng(geocoderList.get(x).getLatitude(), geocoderList.get(x).getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_map)));

            hMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this, "Geocoder Title", snippet));
            hMap.setMarkersClustering(true);
        }

        return hMap;
    }

    private void getToken(Context context) {
        new Thread() {
            @Override
            public void run() {
                try {
                    AGConnectOptionsBuilder agc = new AGConnectOptionsBuilder();
                    agc.build(context).getString("client/app_id");

                    if(agc != null) {
                        String pushToken = HmsInstanceId.getInstance(MapActivity.this).getToken("08908120831", "HCM");

                        if(!TextUtils.isEmpty(pushToken)) {
                            Log.i(TAG, "GET TOKEN: "+pushToken);
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, "getToken falied " + e);
                }
            }
        }.start();
    }
}
