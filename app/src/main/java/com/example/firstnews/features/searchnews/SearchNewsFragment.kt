package com.example.firstnews.features.searchnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstnews.MainActivity
import com.example.firstnews.R
import com.example.firstnews.databinding.FragmentSearchNewsBinding
import com.example.firstnews.util.onQueryTextSubmit
import com.example.firstnews.util.showIfOrInvisible
import com.example.firstnews.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news),
    MainActivity.OnBottomNavigationReselectedListener {

    private val viewModel: SearchNewsViewModel by viewModels()

    private var currentBinding: FragmentSearchNewsBinding? = null
    private val binding get() = currentBinding!!

    private lateinit var newsArticleAdapter: NewsArticlePagingAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBinding = FragmentSearchNewsBinding.bind(view)

        newsArticleAdapter = NewsArticlePagingAdapter(
            onItemClicked = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            onBookmarkedClicked = { article ->
                viewModel.onBookmarkedClicked(article)
            }
        )

        binding.apply {
            recyclerView.apply {
                adapter = newsArticleAdapter.withLoadStateFooter(
                    NewsArticleLoadStateAdapter(newsArticleAdapter::retry)
                )
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.searchResults.collectLatest { data ->
                    newsArticleAdapter.submitData(data)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.hasCurrentQuery.collect { hasCurrentQuery ->
                    textViewInstructions.isVisible = !hasCurrentQuery
                    swipeRefreshLayout.isEnabled = hasCurrentQuery
                    if (!hasCurrentQuery) {
                        recyclerView.isVisible = false
                    }

                }
            }


            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsArticleAdapter.loadStateFlow
                    .distinctUntilChangedBy { it.source.refresh }
                    .filter { it.source.refresh is LoadState.NotLoading }
                    .collect {
                        if (viewModel.pendingScrollToTopAfterNewQuery) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterNewQuery = false
                        }
                        if (viewModel.pendingScrollToTopAfterRefresh && it.mediator?.refresh is LoadState.NotLoading) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
            }



            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsArticleAdapter.loadStateFlow
                    .collect { loadState ->
                        when (val refresh = loadState.mediator?.refresh) {
                            is LoadState.Loading -> {
                                textViewError.isVisible = false
                                buttonRetry.isVisible = false
                                swipeRefreshLayout.isRefreshing = true
                                textViewNoResults.isVisible = false
                                recyclerView.showIfOrInvisible {
                                    !viewModel.newQueryInProgress && newsArticleAdapter.itemCount > 0
                                }

                                viewModel.refreshInProgress = true
                                viewModel.pendingScrollToTopAfterRefresh = true
                            }
                            is LoadState.NotLoading -> {
                                textViewError.isVisible = false
                                buttonRetry.isVisible = false
                                swipeRefreshLayout.isRefreshing = false
                                recyclerView.isVisible = newsArticleAdapter.itemCount > 0

                                val noResults =
                                    newsArticleAdapter.itemCount < 1 && loadState.append.endOfPaginationReached
                                            && loadState.source.append.endOfPaginationReached

                                textViewNoResults.isVisible = noResults

                                viewModel.refreshInProgress = false
                                viewModel.newQueryInProgress = false
                            }

                            is LoadState.Error -> {
                                swipeRefreshLayout.isRefreshing = false
                                textViewNoResults.isVisible = false
                                recyclerView.isVisible = newsArticleAdapter.itemCount > 0

                                val noCashedResults =
                                    newsArticleAdapter.itemCount < 1 && loadState.source.append.endOfPaginationReached

                                textViewError.isVisible = noCashedResults
                                buttonRetry.isVisible = noCashedResults

                                val errorMessage = getString(
                                    R.string.could_not_load_search_results,
                                    refresh.error.localizedMessage
                                        ?: getString(R.string.unknown_error_occurred)
                                )
                                textViewError.text = errorMessage

                                if (viewModel.refreshInProgress) {
                                    showSnackBar(errorMessage)
                                }
                                viewModel.refreshInProgress = false
                                viewModel.pendingScrollToTopAfterRefresh = false
                                viewModel.newQueryInProgress = false
                            }


                            else -> {}
                        }

                    }
            }
            swipeRefreshLayout.setOnRefreshListener {
                newsArticleAdapter.refresh()
            }

            buttonRetry.setOnClickListener {
                newsArticleAdapter.retry()
            }
        }
        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {


            override fun onPrepareMenu(menu: Menu) {
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as SearchView

                searchView.onQueryTextSubmit { query ->
                    viewModel.onSearchQuerySubmit(query)
                    searchView.clearFocus()
                }

            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_news_search, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_refresh -> {
                        newsArticleAdapter.refresh()
                        return true
                    }
                    else -> false
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        currentBinding = null
    }
}