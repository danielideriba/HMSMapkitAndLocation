package com.huawei.hmsmapkitandlocation.pushService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.huawei.hms.location.Geofence

import com.huawei.hms.location.GeofenceData

class GeoFenceBrodcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null) {
            var action = intent.getAction()

            if(ACTION_PROCESS_LOCATION.equals(action)) {
                var geofenceData: GeofenceData = GeofenceData.getDataFromIntent(intent)
                if(geofenceData != null) {
                    //Obtain a result trigger type
                    var errorCode = geofenceData.errorCode
                    var conversion = geofenceData.conversion
                    var list: List<Geofence>  = geofenceData.convertingGeofenceList
                    var mLocation: Location = geofenceData.convertingLocation
                    var status: Boolean = geofenceData.isSuccess
                }
            }
        }
    }

    companion object {
        private const val TAG: String = "GeoFenceBrodcastReceiver"
        public const val ACTION_PROCESS_LOCATION: String =
            "com.huawei.hmssample.geofence.GeoFenceBrodcastReceiver.ACTION_PROCESS_LOCATION"
    }
}