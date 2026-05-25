package com.ai.food.recognition.presentation.auth

import android.os.CountDownTimer
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ai.food.recognition.base.BaseFragment
import com.ai.food.recognition.databinding.FragmentForgotPasswordBinding
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.util.Notify
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>() {

    private val viewModel: ForgotPasswordViewModel by viewModel()

    private var currentEmail = ""
    private var countDownTimer: CountDownTimer? = null

    override fun inflateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentForgotPasswordBinding.inflate(layoutInflater, container, false)

    override fun initializeViews() {
        withBinding(binding) {
            ivClose.setOnClickListener {
                findNavController().popBackStack()
            }

            // Step 1: Request OTP
            btnSendOtp.setOnClickListener {
                val email = edtEmailForgot.text.toString().trim()
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Notify.show(requireActivity(), "Email không hợp lệ!", Notify.Type.ERROR)
                    return@setOnClickListener
                }
                currentEmail = email
                viewModel.onIntent(ForgotPasswordDRE.Intent.SendOtp(email))
            }

            // Step 2: Verify OTP
            btnVerifyOtp.setOnClickListener {
                val otp = edtOtp.text.toString().trim()
                if (otp.length < 6) {
                    Notify.show(requireActivity(), "OTP phải gồm 6 số!", Notify.Type.ERROR)
                    return@setOnClickListener
                }
                viewModel.onIntent(ForgotPasswordDRE.Intent.VerifyOtp(otp))
            }

            tvResendOtp.setOnClickListener {
                if (tvResendOtp.isEnabled) {
                    viewModel.onIntent(ForgotPasswordDRE.Intent.SendOtp(currentEmail))
                }
            }

            // Step 3: Reset Password
            btnResetPassword.setOnClickListener {
                val newPass = edtNewPassword.text.toString().trim()
                val confirmPass = edtConfirmNewPassword.text.toString().trim()
                if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    Notify.show(requireActivity(), "Vui lòng nhập mật khẩu!", Notify.Type.ERROR)
                    return@setOnClickListener
                }
                if (newPass != confirmPass) {
                    Notify.show(requireActivity(), "Mật khẩu xác nhận không khớp!", Notify.Type.ERROR)
                    return@setOnClickListener
                }
                viewModel.onIntent(ForgotPasswordDRE.Intent.ResetPassword(newPass))
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
                            is ForgotPasswordDRE.Effect.ShowError -> {
                                Notify.show(requireActivity(), effect.message, Notify.Type.ERROR)
                            }
                            is ForgotPasswordDRE.Effect.OtpSent -> {
                                Notify.show(requireActivity(), "Mã OTP đã được gửi đến email của bạn!", Notify.Type.SUCCESS)
                                binding?.viewFlipper?.displayedChild = 1
                                startCountdownTimer()
                            }
                            is ForgotPasswordDRE.Effect.OtpVerified -> {
                                Notify.show(requireActivity(), "Xác thực OTP thành công!", Notify.Type.SUCCESS)
                                stopCountdownTimer()
                                binding?.viewFlipper?.displayedChild = 2
                            }
                            is ForgotPasswordDRE.Effect.PasswordReset -> {
                                Notify.show(requireActivity(), "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Notify.Type.SUCCESS)
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startCountdownTimer() {
        stopCountdownTimer()
        binding?.tvResendOtp?.isEnabled = false
        binding?.tvResendOtp?.setTextColor(android.graphics.Color.parseColor("#94A3B8"))

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding?.tvResendOtp?.text = "Gửi lại mã OTP (${seconds}s)"
            }

            override fun onFinish() {
                binding?.tvResendOtp?.isEnabled = true
                binding?.tvResendOtp?.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
                binding?.tvResendOtp?.text = "Gửi lại mã OTP"
            }
        }.start()
    }

    private fun stopCountdownTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCountdownTimer()
    }
}
