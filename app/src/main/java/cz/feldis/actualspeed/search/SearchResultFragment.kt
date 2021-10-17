package cz.feldis.actualspeed.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapFragment
import com.sygic.sdk.map.MapView
import com.sygic.sdk.search.GeocodingResult
import cz.feldis.actualspeed.databinding.FragmentSearchResultBinding

class SearchResultFragment : MapFragment() {
    companion object {
        val ARG_SEARCH_RESULT = "arg_show_coordinates"
    }

    private lateinit var binding: FragmentSearchResultBinding
    private val viewModel: SearchResultFragmentViewModel by activityViewModels()
    private var searchResult: GeocodingResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchResult = it.getParcelable(ARG_SEARCH_RESULT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        val mapView = super.onCreateView(inflater, binding.mapFrameLayout, savedInstanceState)
        binding.root.addView(mapView, 0)
        return binding.root
    }

    override fun getMapDataModel(): MapView.MapDataModel {
        return viewModel.mapDataModel
    }

    override fun getCameraDataModel(): Camera.CameraModel {
        return viewModel.cameraDataModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchResult?.let {
            viewModel.showResult(it)
        }
    }
}