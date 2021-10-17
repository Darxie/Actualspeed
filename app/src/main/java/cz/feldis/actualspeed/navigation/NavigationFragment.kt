package cz.feldis.actualspeed.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sygic.sdk.map.MapFragment
import cz.feldis.actualspeed.databinding.FragmentNavigationBinding

class NavigationFragment : MapFragment() {
    private lateinit var binding: FragmentNavigationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavigationBinding.inflate(inflater, container, false)
        val mapView = super.onCreateView(inflater, container, savedInstanceState)
        binding.root.addView(mapView, 0)
        return binding.root
    }
}