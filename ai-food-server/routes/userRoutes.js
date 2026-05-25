const express = require("express");
const router = express.Router();
const authMiddleware = require("../middleware/authMiddleware");
const userController = require("../controllers/userController");

// API thiết lập thông số ban đầu
router.post("/user/setup", authMiddleware, userController.setupConfig);

// API kiểm tra cấu hình thông số ban đầu
router.get("/user/config", authMiddleware, userController.getConfig);

// API lấy thông tin chi tiết user kết hợp cấu hình
router.get("/user/profile", authMiddleware, userController.getUserProfile);

// API cập nhật thông tin cá nhân
router.post("/user/update", authMiddleware, userController.updateProfile);

// API báo cáo kết quả và tiến độ dinh dưỡng hàng ngày
router.get("/user/daily-report", authMiddleware, userController.getDailyReport);

module.exports = router;
