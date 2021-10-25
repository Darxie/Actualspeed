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
import cz.feldis.actualspeed.drive.DriveFragment.Companion.ARG_DESTINATION

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
            val bundle = Bundle().apply {
                putParcelable(ARG_DESTINATION, it)
            }
            findNavController().navigate(R.id.action_searchResultFragment_to_driveFragment, bundle)
        })
        viewModel.resultTitle.observe(viewLifecycleOwner, { binding.resultTitle.text = it })
        viewModel.resultSubtitle.observe(viewLifecycleOwner, { binding.resultSubtitle.text = it })

        binding.fabNavigation.setOnClickListener { viewModel.onNavigateToResultClick() }
    }
}