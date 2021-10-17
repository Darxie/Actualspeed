package cz.feldis.actualspeed.search

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.search.*
import cz.feldis.actualspeed.ktx.position.PositionManagerKtx
import cz.feldis.actualspeed.ktx.search.SearchManagerKtx
import cz.feldis.actualspeed.utils.SignalingLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchFragmentViewModel : ViewModel(), TextWatcher {

    private val searchResultsMutable = MutableLiveData<List<SearchItem>>()
    val searchResults: LiveData<List<SearchItem>> = searchResultsMutable

    private val showSearchResultMutable = SignalingLiveData<GeocodingResult>()
    val showSearchResult: LiveData<GeocodingResult> = showSearchResultMutable

    private val positionManagerKtx = PositionManagerKtx()
    private val searchManagerKtx = SearchManagerKtx()
    private var searchJob: Job? = null
    private var searchSession: Session? = null

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }

    fun onSearchItemClick(searchItem: SearchItem) {
        viewModelScope.launch {
            searchSession?.let {
                searchSession = null
                val geocodeLocationRequest = GeocodeLocationRequest(searchItem.locationId)
                it.geocode(geocodeLocationRequest, object : GeocodingResultListener {
                    override fun onGeocodingResult(geocodingResult: GeocodingResult) {
                        viewModelScope.launch {
                            showSearchResultMutable.postValue(geocodingResult)
                            searchManagerKtx.closeSession(it)
                        }
                    }

                    override fun onGeocodingResultError(status: ResultStatus) {
                        viewModelScope.launch {
                            searchManagerKtx.closeSession(it)
                        }
                    }
                })
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModelScope.launch {
            searchJob?.let {
                it.cancel()
                it.join()
            }

            if (searchSession == null) {
                searchSession = searchManagerKtx.newOnlineSession()
            }

            searchJob = launch {
                val searchInput = s.toString()
                val searchCoordinates = positionManagerKtx.lastKnownPosition().coordinates
                val searchRequest = SearchRequest(searchInput, searchCoordinates)
                searchSession?.autocomplete(searchRequest, object : AutocompleteResultListener {
                    override fun onAutocomplete(autocompleteResult: List<AutocompleteResult>) {
                        searchResultsMutable.postValue(autocompleteResult.map { resultItem ->
                            SearchItem(resultItem.title, resultItem.subtitle, resultItem.locationId)
                        })
                    }

                    override fun onAutocompleteError(status: ResultStatus) {
                        searchResultsMutable.postValue(emptyList())
                    }
                })
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}
}