package com.example.firstnews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.firstnews.databinding.ActivityMainBinding
import com.example.firstnews.features.bookmarks.BookmarksFragment
import com.example.firstnews.features.breakingnews.BreakingNewsFragment
import com.example.firstnews.features.searchnews.SearchNewsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var breakingNewsFragment: BreakingNewsFragment
    private lateinit var searchNewsFragment: SearchNewsFragment
    private lateinit var bookmarksFragment: BookmarksFragment

    private val fragments: Array<Fragment>
        get() = arrayOf(
            breakingNewsFragment,
            searchNewsFragment,
            bookmarksFragment
        )

    private var selectedIndex = 0

    private val selectedFragment get() = fragments[selectedIndex]

    private fun selectFragment(selectedFragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (selectedFragment == fragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()

        title = when (selectedFragment) {
            is BreakingNewsFragment -> getString(R.string.title_breaking_news)
            is SearchNewsFragment -> getString(R.string.title_search_news)
            is BookmarksFragment -> getString(R.string.title_bookmarks)
            else -> ""
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            breakingNewsFragment = BreakingNewsFragment()
            searchNewsFragment = SearchNewsFragment()
            bookmarksFragment = BookmarksFragment()

            supportFragmentManager.beginTransaction()
                .add(breakingNewsFragment, TAG_BREAKING_NEWS_FRAGMENT)
                .add(searchNewsFragment, TAG_SEARCH_NEWS_FRAGMENT)
                .add(bookmarksFragment, TAG_BOOKMARKS_FRAGMENT)
                .commit()

        } else {
            breakingNewsFragment = supportFragmentManager.findFragmentByTag(
                TAG_BREAKING_NEWS_FRAGMENT
            ) as BreakingNewsFragment
            searchNewsFragment = supportFragmentManager.findFragmentByTag(
                TAG_SEARCH_NEWS_FRAGMENT
            ) as SearchNewsFragment
            bookmarksFragment = supportFragmentManager.findFragmentByTag(
                TAG_BOOKMARKS_FRAGMENT
            ) as BookmarksFragment

            selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
        }

        selectFragment(selectedFragment)

        binding.bottomNav.setOnItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.nav_breaking_news -> breakingNewsFragment
                R.id.nav_search_news -> searchNewsFragment
                R.id.nav_bookmarks -> bookmarksFragment
                else -> throw java.lang.IllegalArgumentException("Unexpected Error")
            }
            selectFragment(fragment)
            true
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }

    override fun onBackPressed() {
        if (selectedIndex != 0){
            binding.bottomNav.selectedItemId = R.id.nav_breaking_news
        } else{
            super.onBackPressed()
        }

    }


}

private const val TAG_BREAKING_NEWS_FRAGMENT = "TAG_BREAKING_NEWS_FRAGMENT"
private const val TAG_SEARCH_NEWS_FRAGMENT = "TAG_SEARCH_NEWS_FRAGMENT"
private const val TAG_BOOKMARKS_FRAGMENT = "TAG_BOOKMARKS_FRAGMENT"
private const val KEY_SELECTED_INDEX = "KEY_SELECTED_INDEX"