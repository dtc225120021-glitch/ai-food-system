const express = require("express");
const router = express.Router();
const authController = require("../controllers/authController");

router.post("/auth/register", authController.register);
router.post("/auth/login", authController.login);
router.post("/auth/refresh-token", authController.refreshToken);
router.post("/auth/forgot-password", authController.forgotPassword);
router.post("/auth/verify-otp", authController.verifyOtp);
router.post("/auth/reset-password", authController.resetPassword);

module.exports = router;