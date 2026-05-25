package com.ai.food.recognition.presentation.auth

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ai.food.recognition.R
import com.ai.food.recognition.base.BaseFragment
import com.ai.food.recognition.databinding.FragmentRegisterBinding
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.util.Notify
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModel()

    private var selectedAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            binding?.ivAvatar?.setImageURI(it)
        }
    }

    override fun inflateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRegisterBinding.inflate(layoutInflater, container, false)

    override fun initializeViews() {
        withBinding(binding) {
            ivClose.setOnClickListener {
                findNavController().popBackStack()
            }

            // Bind photo picker triggers
            layoutAvatar.setOnClickListener {
                pickImageLauncher.launch(getString(R.string.image_type))
            }
            cardAvatar.setOnClickListener {
                pickImageLauncher.launch(getString(R.string.image_type))
            }
            cardCameraBadge.setOnClickListener {
                pickImageLauncher.launch(getString(R.string.image_type))
            }

            btnRegister.setOnClickListener {
                if (validateForm()) {
                    val fullName = edtFullName.text.toString().trim()
                    val email = edtEmail.text.toString().trim()
                    val password = edtPassword.text.toString().trim()
                    viewModel.onIntent(RegisterDRE.Intent.Register(fullName, email, password))
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        withBinding(binding) {
            val fullName = edtFullName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val cfPassword = edtConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                edtFullName.error = getString(R.string.notify_full_name_is_required)
                Notify.show(requireActivity(), getString(R.string.notify_full_name_is_required), Notify.Type.ERROR)
                isValid = false
                return@withBinding
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.error = "Email không đúng định dạng!"
                Notify.show(requireActivity(), "Email không đúng định dạng!", Notify.Type.ERROR)
                isValid = false
                return@withBinding
            }
            if (email.isEmpty() || password.isEmpty() || cfPassword.isEmpty()) {
                edtEmail.error = "Vui lòng nhâp Email"
                edtPassword.error = "Vui lòng nhập mật khẩu"
                Notify.show(requireActivity(), "Vui lòng nhập đầy đủ email và mật khẩu!", Notify.Type.ERROR)
                isValid = false
                return@withBinding
            }
            if (password != cfPassword) {
                edtConfirmPassword.error = "Mật khẩu xác nhận không khớp!"
                Notify.show(requireActivity(), "Mật khẩu xác nhận không khớp!", Notify.Type.ERROR)
                isValid = false
                return@withBinding
            }
        }
        return isValid
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
                            is RegisterDRE.Effect.ShowError -> {
                                Notify.show(requireActivity(), effect.message, Notify.Type.ERROR)
                            }
                            is RegisterDRE.Effect.RegisterSuccess -> {
                                Notify.show(requireActivity(), "Đăng ký tài khoản thành công!", Notify.Type.SUCCESS)
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}