package cz.feldis.actualspeed.drive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapFragment
import com.sygic.sdk.map.MapView
import com.sygic.sdk.position.GeoCoordinates
import cz.feldis.actualspeed.R
import cz.feldis.actualspeed.databinding.FragmentDriveBinding

class DriveFragment : MapFragment() {
    companion object {
        const val ARG_DESTINATION = "arg_destination"
    }

    private lateinit var binding: FragmentDriveBinding
    private val viewModel: DriveFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val destination: GeoCoordinates? = it.getParcelable(ARG_DESTINATION)
            destination?.let {
                viewModel.navigateTo(destination)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriveBinding.inflate(inflater, container, false)
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

        binding.speedInfoLayout.speed.setOnClickListener { viewModel.resetCamera() }
        binding.fabSimulation.setOnLongClickListener { viewModel.enableDebugLayer() }
        binding.fabSearch.setOnClickListener { findNavController().navigate(R.id.action_driveFragment_to_searchFragment) }
        binding.fabSimulation.setOnClickListener { viewModel.simulate() }
        binding.fabStopNavigation.setOnClickListener { viewModel.stopNavigation() }

        viewModel.simulateButtonVisible.observe(viewLifecycleOwner, {
            if (it == true) binding.fabSimulation.show() else binding.fabSimulation.hide()
        })
        viewModel.simulateButtonIcon.observe(viewLifecycleOwner, {
            binding.fabSimulation.setImageDrawable(AppCompatResources.getDrawable(requireContext(), it))
        })
        viewModel.stopNavigationButtonVisible.observe(viewLifecycleOwner, {
            if (it == true) binding.fabStopNavigation.show() else binding.fabStopNavigation.hide()
        })
        viewModel.currentSpeedText.observe(viewLifecycleOwner, {
            binding.speedInfoLayout.speed.text = it
        })
        viewModel.currentSpeedColor.observe(viewLifecycleOwner, {
            binding.speedInfoLayout.currentSpeedImage.setColorFilter(it)
        })
        viewModel.speedLimitText.observe(viewLifecycleOwner, {
            binding.speedInfoLayout.speedLimit.text = it
        })
        viewModel.currentStreetInfo.observe(viewLifecycleOwner, {
            binding.street.text = getString(R.string.current_street, it.street, it.city)
        })
        viewModel.currentStreetDetail.observe(viewLifecycleOwner, {
            binding.junction.text = getString(R.string.next_junction, it.distanceToNextJunction)
        })
    }
}