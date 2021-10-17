package cz.feldis.actualspeed.freedrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapFragment
import com.sygic.sdk.map.MapView
import cz.feldis.actualspeed.R
import cz.feldis.actualspeed.databinding.FragmentFreeDriveBinding

class FreeDriveFragment : MapFragment() {
    private lateinit var binding: FragmentFreeDriveBinding
    private val viewModel: FreeDriveFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFreeDriveBinding.inflate(inflater, container, false)
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
        binding.fabSearch.setOnClickListener { findNavController().navigate(R.id.action_freeDriveFragment_to_searchFragment) }

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