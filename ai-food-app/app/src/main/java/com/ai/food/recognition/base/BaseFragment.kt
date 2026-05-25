package com.ai.food.recognition.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected var binding: VB? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflateBinding(inflater, container)
        collectLaunchWhenStarted()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        retrieveData()
    }

    open fun retrieveData() {}

    //
    open fun collectLaunchWhenStarted() {}

    abstract fun inflateBinding(layoutInflater: LayoutInflater, container: ViewGroup?): VB

    open fun initializeViews() {}

    override fun onResume() {
        super.onResume()
        didUpdateViews()
    }

    open fun didUpdateViews() {}

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}