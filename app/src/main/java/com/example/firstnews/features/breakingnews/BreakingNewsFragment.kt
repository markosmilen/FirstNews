package com.example.firstnews.features.breakingnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstnews.R
import com.example.firstnews.databinding.FragmentBreakingNewsBinding
import com.example.firstnews.data.NewsArticle
import com.example.firstnews.shared.NewsArticleListAdapter
import com.example.firstnews.util.Resource
import com.example.firstnews.util.exhaustive
import com.example.firstnews.util.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    private val viewModel: BreakingNewsViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBreakingNewsBinding.bind(view)

        val articleListAdapter = NewsArticleListAdapter(
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
                adapter = articleListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.breakingNews.collect {
                    val articles = it ?: return@collect

                    swipeRefreshLayout.isRefreshing = articles is Resource.Loading
                    recyclerView.isVisible = !articles.data.isNullOrEmpty()
                    textViewError.isVisible =
                        articles.error != null && articles.data.isNullOrEmpty()
                    textViewError.text = getString(
                        R.string.could_not_refresh,
                        articles.error?.localizedMessage ?: R.string.unknown_error_occurred
                    )
                    buttonRetry.isVisible = articles.error != null && articles.data.isNullOrEmpty()

                    articleListAdapter.submitList(articles.data) {
                        if (viewModel.pendingScrollToTopLocation) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopLocation = false
                        }
                    }
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            buttonRetry.setOnClickListener {
                viewModel.onManualRefresh()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.events.collect { event ->
                    when (event) {
                        is BreakingNewsViewModel.Event.ShowErrorMessage -> showSnackBar(
                            getString(
                                R.string.could_not_refresh,
                                event.message.localizedMessage ?: R.string.unknown_error_occurred
                            ),

                            )
                    }.exhaustive
                }
            }
        }

        setupMenu()

    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_breaking_news, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_refresh -> {
                        viewModel.onManualRefresh()
                        return true
                    }
                    else -> false
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }
}