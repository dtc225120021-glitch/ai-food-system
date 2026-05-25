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
        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "Không tìm thấy thông tin người dùng trong token."
            });
        }

        const foods = await Food.find({ createBy: userId }).sort({ createdAt: -1 });

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
