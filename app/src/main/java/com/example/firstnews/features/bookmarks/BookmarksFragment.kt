package com.example.firstnews.features.bookmarks

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
import com.example.firstnews.databinding.FragmentBookmarksBinding
import com.example.firstnews.shared.NewsArticleListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    private val viewModel: BookmarksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBookmarksBinding.bind(view)

        val bookmarksAdapter = NewsArticleListAdapter(
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
                adapter = bookmarksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.bookmarks.collect{
                    val bookmarks = it ?: return@collect

                    bookmarksAdapter.submitList(bookmarks)
                    textViewNoBookmarks.isVisible = bookmarks.isEmpty()
                    recyclerView.isVisible = bookmarks.isNotEmpty()
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
                menuInflater.inflate(R.menu.menu_bookmarks, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_delete_all_bookmarks -> {
                        viewModel.onDeleteAllBookmarks()
                        return true
                    }
                    else -> false
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}