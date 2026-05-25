const User = require("../models/User");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const nodemailer = require("nodemailer");

exports.register = async (req, res) => {
    // try {
    //     const { email, password } = req.body;

    //     const userExists = await User.findOne({ email });

    //     if (userExists) {
    //         return res.status(400).json({ message: "Tài khoản Email đã tồn tại" });
    //     }

    //     const salt = await bcrypt.genSalt(10);
    //     const hashedPassword = await bcrypt.hash(password, salt);

    //     const user = new User({
    //         email,
    //         password: hashedPassword
    //     });

    //     await user.save();

    //     res.status(201).json({
    //         success: true,
    //         message: "Đăng ký thành công"
    //     });

    // } catch (error) {
    //     res.status(500).json({ error: error.message });
    // }
    try {
        const { full_name, email, password } = req.body;

        if (!full_name || !email || !password) {
            return res.status(400).json({
                status: false,
                message: "Vui lòng cung cấp đầy đủ full_name, email và password"
            });
        }

        const userExists = await User.findOne({ email });

        if (userExists) {
            return res.status(400).json({
                success: false,
                message: "Tài khoản email đã tồn tại"
            });
        }

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        const user = new User({
            full_name,
            email,
            password: hashedPassword
        });

        await user.save();

        res.status(201).json({
            success: true,
            message: "Đăng ký thành công"
        });

    } catch (error) {
        res.status(500).json({
            success: false,
            error: error.message
        });
    }

};

exports.login = async (req, res) => {

    try {

        const { email, password } = req.body;

        const user = await User.findOne({ email });

        if (!user) {
            return res.status(400).json({ status: false, message: "Email không tồn tại" });
        }

        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            return res.status(400).json({ status: false, message: "Email hoặc mật khẩu không chính xác" });
        }

        const token = jwt.sign(
            { userId: user._id },
            process.env.JWT_SECRET,
            { expiresIn: "7d" }
        );

        const refreshToken = jwt.sign(
            { userId: user._id },
            process.env.REFRESH_TOKEN_SECRET,
            { expiresIn: "30d" }
        );

        user.refreshToken = refreshToken;
        await user.save();

        return res.json({
            success: true,
            data: {
                token,
                refreshToken,
            }
        });

    } catch (error) {
        return res.status(500).json({ status: false, error: error.message });
    }

};

exports.profile = async (req, res) => {
    try {
        const userId = req.user.userId;
        const user = await User.findById(userId)
            .select("-password");
        if (!user) {
            return res.status(404).json({
                message: "Không tìm thấy tài khoản"
            });
        }
        res.json({
            success: true,
            data: user
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.forgotPassword = async (req, res) => {
    try {
        const { email } = req.body;
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({ success: false, message: "User not found" });
        }

        // Generate 6-digit OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString();

        // Set OTP and expiration (e.g., 5 minutes from now)
        user.resetOtp = otp;
        user.resetOtpExpires = Date.now() + 5 * 60 * 1000;
        await user.save();

        // In a real application, send this OTP to the user's email.
        const transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS
            }
        });

        const mailOptions = {
            from: process.env.EMAIL_USER,
            to: user.email,
            subject: 'Mã OTP đặt lại mật khẩu của bạn',
            text: `Mã OTP của bạn là: ${otp}. Mã này sẽ hết hạn trong vòng 5 phút.`
        };

        await transporter.sendMail(mailOptions);

        res.json({
            success: true,
            message: "Mã OTP đã được gửi đến email của bạn",
            data: {
                // Return for testing if environment variable not setup, otherwise don't return it
                // otp 
            }
        });

    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.verifyOtp = async (req, res) => {
    try {
        const { email, otp } = req.body;
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({ success: false, message: "Không tìm thấy tài khoản" });
        }

        if (user.resetOtp !== otp) {
            return res.status(400).json({ success: false, message: "Mã OTP không chính xác" });
        }

        if (user.resetOtpExpires < Date.now()) {
            return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn" });
        }

        res.json({
            success: true,
            message: "Xác thực OTP thành công"
        });

    } catch (error) {
        res.status(500).json({ status: false, error: error.message });
    }
};

exports.resetPassword = async (req, res) => {
    try {
        const { email, otp, newPassword } = req.body;
        const user = await User.findOne({ email });

        if (!user) {
            return res.status(404).json({ success: false, message: "Không tìm thấy tài khoản" });
        }

        if (user.resetOtp !== otp) {
            return res.status(400).json({ success: false, message: "Mã OTP không chính xác" });
        }

        if (user.resetOtpExpires < Date.now()) {
            return res.status(400).json({ success: false, message: "Mã OTP đã hết hạn" });
        }

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(newPassword, salt);

        user.password = hashedPassword;
        user.resetOtp = null;
        user.resetOtpExpires = null;
        await user.save();

        res.json({
            success: true,
            message: "Đặt lại mật khẩu thành công"
        });

    } catch (error) {
        res.status(500).json({ status: false, error: error.message });
    }
};

exports.refreshToken = async (req, res) => {
    try {
        const { refreshToken } = req.body;

        if (!refreshToken) {
            return res.status(401).json({ success: false, message: "Không có mã làm mới" });
        }

        const decoded = jwt.verify(refreshToken, process.env.REFRESH_TOKEN_SECRET);

        const user = await User.findById(decoded.userId);

        if (!user || user.refreshToken !== refreshToken) {
            return res.status(403).json({ success: false, message: "Mã làm mới không hợp lệ" });
        }

        const newToken = jwt.sign(
            { userId: user._id },
            process.env.JWT_SECRET,
            { expiresIn: "7d" }
        );

        res.json({
            success: true,
            data: {
                token: newToken
            }
        });

    } catch (error) {
        res.status(403).json({ success: false, message: "Mã làm mới không hợp lệ hoặc đã hết hạn" });
    }
};