package cz.feldis.actualspeed.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapFragment
import com.sygic.sdk.map.MapView
import com.sygic.sdk.search.GeocodingResult
import cz.feldis.actualspeed.R
import cz.feldis.actualspeed.databinding.FragmentSearchResultBinding
import kotlinx.android.synthetic.main.fragment_search_result.*

class SearchResultFragment : MapFragment() {
    companion object {
        const val ARG_SEARCH_RESULT = "arg_show_coordinates"
    }

    private lateinit var binding: FragmentSearchResultBinding
    private val viewModel: SearchResultFragmentViewModel by viewModels()
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
        binding.mapFrameLayout.addView(mapView, 0)
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

        viewModel.navigateTo.observe(viewLifecycleOwner, {
            findNavController().navigate(R.id.action_searchResultFragment_to_driveFragment)
        })
        viewModel.resultTitle.observe(viewLifecycleOwner, { binding.resultTitle.text = it })
        viewModel.resultSubtitle.observe(viewLifecycleOwner, { binding.resultSubtitle.text = it })
        viewModel.fastestRoute.observe(
            viewLifecycleOwner,
            { binding.useFastestRouteSwitch.isChecked = it })
        viewModel.avoidTollRoads.observe(
            viewLifecycleOwner,
            { binding.avoidTollRoadsSwitch.isChecked = it })
        viewModel.useUnpavedRoads.observe(
            viewLifecycleOwner,
            { binding.useUnpavedRoadsSwitch.isChecked = it })
        viewModel.calculateButtonVisible.observe(viewLifecycleOwner, {
            if (it) binding.fabCalculateRoute.show() else binding.fabCalculateRoute.hide()
        })
        viewModel.navigateButtonVisible.observe(viewLifecycleOwner, {
            if (it) binding.fabNavigation.show() else binding.fabNavigation.hide()
        })
        viewModel.progressBarVisible.observe(viewLifecycleOwner, {
            calculateRouteProgress.visibility = if (it) View.VISIBLE else View.GONE
        })

        binding.fabNavigation.setOnClickListener { viewModel.startNavigation() }
        binding.fabCalculateRoute.setOnClickListener { viewModel.calculateRoute() }
        binding.useFastestRouteSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFastestRoute(
                isChecked
            )
        }
        binding.avoidTollRoadsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAvoidTollRoads(
                isChecked
            )
        }
        binding.useUnpavedRoadsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUseUnpavedRoads(
                isChecked
            )
        }
    }
}