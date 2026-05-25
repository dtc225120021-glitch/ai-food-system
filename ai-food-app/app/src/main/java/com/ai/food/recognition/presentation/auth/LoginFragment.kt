package com.ai.food.recognition.presentation.auth

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ai.food.recognition.R
import com.ai.food.recognition.base.BaseFragment
import com.ai.food.recognition.databinding.FragmentLoginBinding
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.config.UserConfigActivity
import com.ai.food.recognition.util.Notify
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModel()

    override fun inflateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginBinding.inflate(layoutInflater, container, false)

    override fun initializeViews() {
        withBinding(binding) {
            btnRegister.setOnClickListener {
                findNavController().navigate(R.id.action_auth_login_to_auth_register)
            }
            tvForgotPassword.setOnClickListener {
                findNavController().navigate(R.id.action_auth_login_to_auth_forgot_password)
            }

            btnLogin.setOnClickListener {
                val email = edtEmail.text.toString().trim()
                val password = edtPassword.text.toString().trim()
                if (email.isEmpty() || password.isEmpty()) {
                    Notify.show(requireActivity(), "Thiếu thông tin đăng nhâp", Notify.Type.ERROR)
                    return@setOnClickListener
                }

                viewModel.onIntent(LoginDRE.Intent.Login(email, password, cbSelected.isChecked))
            }
        }
    }

    override fun collectLaunchWhenStarted() {
        super.collectLaunchWhenStarted()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        if (isLoading) {
                            (requireActivity() as AuthActivity).showLoading()
                        } else {
                            (requireActivity() as AuthActivity).hideLoading()
                        }
                    }
                }

                launch {
                    viewModel.uiEffect.collect { effect ->
                        when (effect) {
                            is LoginDRE.Effect.ShowError -> {
                                Notify.show(requireActivity(), effect.message, Notify.Type.ERROR)
                            }

                            is LoginDRE.Effect.NavigateToHome -> {
                                Notify.show(requireActivity(), "Đăng nhập thành công", Notify.Type.SUCCESS)
                                delay(1000)
                                (requireActivity() as AuthActivity).goToHome()
                            }

                            is LoginDRE.Effect.NavigateToUserConfig -> {
                                Notify.show(
                                    requireActivity(),
                                    "Đăng nhập thành công, vui lòng thiết lập thông tin!",
                                    Notify.Type.SUCCESS
                                )
                                delay(1000)
                                startActivity(Intent(requireActivity(), UserConfigActivity::class.java))
                                requireActivity().finish()
                            }
                        }
                    }
                }
            }
        }
    }
}
