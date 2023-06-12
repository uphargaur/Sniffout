import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> get() = _currentLocation

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _currentLocation.value = latLng
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch location
            }
    }
}
