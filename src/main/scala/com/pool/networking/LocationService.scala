package com.pool.networking

import android.app.AlertDialog
import android.content.{Intent, DialogInterface, Context}
import android.location._
import android.os.Bundle
import android.util.Log

/**
 * Created by StevW on 4/26/15.
 * Not a anrdoid service because we want to embed it inside networkService
 */
class LocationService extends java.io.Serializable {
  var appContext : Context = null
  var mp : MessagePasser = null
  var locationManager : LocationManager = null
  var locationListener : LocationListener = new LocationListener() {
    def onLocationChanged(location: Location): Unit = {
      // Called when a new location is found by the network location provider.
      if (!locationInit) {
        initialLocation = location
        locationInit = true
        Log.w("Pool", "Location initialized")
      }
      currentLocation = location
      Log.w("Pool", "Got location" + location.getLatitude.toString + " " + location.getLongitude.toString)
      mp.updateLocation()
    }


    def onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    def onProviderEnabled(provider: String) {}

    def onProviderDisabled(provider: String) {}
  }


  var isGpsOn : Boolean = false
  var isNetworkOn : Boolean = false

  var locationInit : Boolean = false
  var initialLocation : Location = null
  var currentLocation : Location = null

  val LOCATION_INTERVAL : Int = 30
  val MIN_DISTANCE : Int = 10

  def this(con : Context) {
    this()

    Log.w("Pool", "Starting location service")

    //save external context
    appContext = con
    // Acquire a reference to the system Location Manager
    locationManager = appContext.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]


    isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    isNetworkOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)


    // Register the listener with the Location Manager to receive location updates
    if (isGpsOn) {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, MIN_DISTANCE, locationListener)
      Log.w("Pool", "Location: using gps")
    } else if (isNetworkOn) {
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, MIN_DISTANCE, locationListener)
      Log.w("Pool", "Location: using network")
    } else {
      Log.w("Pool", "Location: ERROR No Provider")
    }
  }

  def getLocation(): Location = {
    var loc : Location = null

    if (isGpsOn) {
      loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } else if (isNetworkOn) {
      loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    if (loc == null) {
      loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
    }

    return loc
  }

}

object LocationService {
  def checkGps(con : Context) : Unit = {
    val locationManager : LocationManager = con.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    //check if gps is on
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      buildAlertMessageNoGps(con);
    }
  }

  def buildAlertMessageNoGps(con : Context) : Unit = {
    val builder : AlertDialog.Builder = new AlertDialog.Builder(con)
    builder.setMessage("Your GPS is disabled, do you want to enable it?")
      .setCancelable(false)
      .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      def onClick(dialog : DialogInterface, id : Int) : Unit = {
        con.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
      }
    })
      .setNegativeButton("No", new DialogInterface.OnClickListener() {
      def onClick(dialog : DialogInterface, id : Int) : Unit = {
        dialog.cancel()
      }
    })

    val alert : AlertDialog = builder.create()
    alert.show()
  }

}