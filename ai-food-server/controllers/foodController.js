const Food = require("../models/Food");
const Recent = require("../models/Recent");

/**
 * API Xác nhận lưu trữ món ăn thực tế đã tiêu thụ
 * Method: POST /api/foods/confirm
 * Header: Authorization: Bearer <token>
 * Body: { recentId, image, foods }
 */
exports.confirmFood = async (req, res) => {
    try {
        const userId = req.user.userId;
        const { recentId, image, foods, category } = req.body;

        if (!foods || !Array.isArray(foods) || foods.length === 0) {
            return res.status(400).json({
                success: false,
                message: "Danh sách thực phẩm không hợp lệ hoặc trống."
            });
        }

        // Tự động tìm hình ảnh từ lịch sử quét Recent nếu người dùng không truyền image trực tiếp
        let finalImage = image;
        if (recentId && !finalImage) {
            const recent = await Recent.findById(recentId);
            if (recent) {
                finalImage = recent.image;
            }
        }

        // Tạo bản ghi thực phẩm đã ăn
        const foodLog = new Food({
            userId: userId,
            image: finalImage,
            category: category || "Bữa Sáng",
            foods: foods.map(food => ({
                name: food.name,
                carbs: Number(food.carbs) || 0,
                protein: Number(food.protein) || 0,
                fat: Number(food.fat) || 0,
                calories: Number(food.calories) || 0
            })),
            createBy: userId
        });

        const savedFood = await foodLog.save();

        // Đồng bộ cập nhật bảng DailyReport (bảng csdl của user cho ngày hiện tại)
        let totalCalories = 0;
        let totalProtein = 0;
        let totalFat = 0;
        let totalCarbs = 0;

        foods.forEach(food => {
            totalCalories += Number(food.calories) || 0;
            totalProtein += Number(food.protein) || 0;
            totalFat += Number(food.fat) || 0;
            totalCarbs += Number(food.carbs) || 0;
        });

        const DailyReport = require("../models/DailyReport");
        const tzOffset = 7 * 60 * 60 * 1000;
        const dateString = new Date(Date.now() + tzOffset).toISOString().split("T")[0];

        await DailyReport.findOneAndUpdate(
            { userId: userId, date: dateString },
            {
                $inc: {
                    consumedCalories: totalCalories,
                    consumedProtein: totalProtein,
                    consumedFat: totalFat,
                    consumedCarbs: totalCarbs
                }
            },
            { new: true, upsert: true }
        );

        return res.status(201).json({
            success: true,
            message: "Xác nhận thực phẩm thành công.",
            data: savedFood
        });
    } catch (error) {
        console.error("Lỗi API confirmFood:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi xác nhận thực phẩm.",
            error: error.message
        });
    }
};

/**
 * API Lấy danh sách món ăn đã xác nhận ăn của người dùng
 * Method: GET /api/foods
 * Header: Authorization: Bearer <token>
 */
exports.getFoods = async (req, res) => {
    try {
        const userId = req.user.userId;
        const { date } = req.query;

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "Không tìm thấy thông tin người dùng trong token."
            });
        }

        let query = { createBy: userId };

        if (date) {
            const startOfDay = new Date(`${date}T00:00:00.000+07:00`);
            const endOfDay = new Date(`${date}T23:59:59.999+07:00`);
            query.createdAt = {
                $gte: startOfDay,
                $lte: endOfDay
            };
        }

        const foods = await Food.find(query).sort({ createdAt: -1 });

        return res.json({
            success: true,
            data: foods
        });
    } catch (error) {
        console.error("Lỗi API getFoods:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi lấy lịch sử thực phẩm.",
            error: error.message
        });
    }
};
