package com.ai.food.recognition.presentation.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.ai.food.recognition.base.BaseFragment
import com.ai.food.recognition.databinding.FragmentHistoryBinding
import com.ai.food.recognition.ext.withBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val imageLoader: ImageLoader by inject()
    private val viewModel: HistoryViewModel by viewModel()
    private lateinit var historyAdapter: HistoryAdapter

    override fun inflateBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
        FragmentHistoryBinding.inflate(layoutInflater, container, false)

    override fun initializeViews() {
        historyAdapter = HistoryAdapter(imageLoader) { item ->
            // Open details
            val intent = Intent(requireContext(), HistoryDetailActivity::class.java).apply {
                putExtra("HISTORY_ITEM", item)
            }
            startActivity(intent)
        }

        withBinding(binding) {
            rvHistory.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = historyAdapter
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onIntent(HistoryDRE.Intent.LoadHistory)
    }

    override fun collectLaunchWhenStarted() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    withBinding(binding) {
                        progressBar.isVisible = state.isLoading
                        
                        if (state.isLoading) {
                            layoutEmpty.isVisible = false
                            rvHistory.isVisible = false
                        } else {
                            if (state.historyList.isEmpty()) {
                                layoutEmpty.isVisible = true
                                rvHistory.isVisible = false
                            } else {
                                layoutEmpty.isVisible = false
                                rvHistory.isVisible = true
                                historyAdapter.submitList(state.historyList)
                            }
                        }
                    }
                }
            }
        }
    }
}
