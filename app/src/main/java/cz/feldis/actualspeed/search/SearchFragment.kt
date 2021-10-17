package cz.feldis.actualspeed.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cz.feldis.actualspeed.R
import cz.feldis.actualspeed.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        with(binding.searchResults) {
            layoutManager = LinearLayoutManager(context)
            adapter = SearchRecyclerViewAdapter(viewModel::onSearchItemClick)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchInput.addTextChangedListener(viewModel)

        viewModel.searchResults.observe(viewLifecycleOwner, {
            (binding.searchResults.adapter as SearchRecyclerViewAdapter).setData(it)
        })
        viewModel.showSearchResult.observe(viewLifecycleOwner, {
            val args = Bundle().apply {
                putParcelable(SearchResultFragment.ARG_SEARCH_RESULT, it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_searchResultFragment, args)
        })
    }
}