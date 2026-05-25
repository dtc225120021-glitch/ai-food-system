const User = require("../models/User");
const OpenAI = require("openai");
const UserGoal = require("../models/UserGoal");
const DailyReport = require("../models/DailyReport");
const Recent = require("../models/Recent");
const Food = require("../models/Food");

const openai = new OpenAI({
    apiKey: process.env.OPENAI_API_KEY
});

// Helper to get YYYY-MM-DD date string in UTC+7 timezone
const getUTC7DateString = (dateObj = new Date()) => {
    const tzOffset = 7 * 60 * 60 * 1000; // 7 hours in milliseconds
    const utc7Date = new Date(dateObj.getTime() + tzOffset);
    return utc7Date.toISOString().split("T")[0];
};

// Helper to get UTC start and end Dates for a YYYY-MM-DD date string in UTC+7
const getUTCStartAndEndOfDayInUTC7 = (dateString) => {
    const startOfDay = new Date(`${dateString}T00:00:00.000+07:00`);
    const endOfDay = new Date(`${dateString}T23:59:59.999+07:00`);
    return { startOfDay, endOfDay };
};

/**
 * API thiết lập thông số ban đầu
 * Method: POST /api/user/setup
 * Header: Authorization: Bearer <token>
 * Body: { age, gender, height, weight, goal, activityLevel } (Supports Vietnamese fields as well)
 */
exports.setupConfig = async (req, res) => {
    try {
        const userId = req.user.userId;

        // Support both Vietnamese and English request bodies
        const {
            age,
            gender,
            height,
            weight,
            goal,
            activityLevel,
        } = req.body;

        const finalAge = age;
        const finalGender = gender;
        const finalHeight = height;
        const finalWeight = weight;
        const finalGoal = goal;
        const finalActivityLevel = activityLevel;

        if (!finalAge || !finalGender || !finalHeight || !finalWeight || !finalGoal || !finalActivityLevel) {
            return res.status(400).json({
                success: false,
                message: "Thiếu thông tin cấu hình ban đầu. Các thông số sau là bắt buộc: độ tuổi, giới tính, chiều cao, cân nặng, mục tiêu, cường độ tập luyện."
            });
        }

        // Build a friendly prompt in Vietnamese to request daily calorie goal from AI
        const prompt = `Dựa trên các thông tin sức khỏe và mục tiêu của người dùng sau đây, hãy tính toán và đưa ra mục tiêu lượng calo, lượng protein, lượng chất béo (fats), lượng đường (carbs) hàng ngày (Daily Calorie Goal) phù hợp và tối ưu nhất cho họ:
- Độ tuổi: ${finalAge} tuổi
- Giới tính: ${finalGender}
- Chiều cao: ${finalHeight} cm
- Cân nặng: ${finalWeight} kg
- Mục tiêu: ${finalGoal}
- Cường độ tập luyện: ${finalActivityLevel}

Hãy trả về duy nhất 1 chuỗi JSON có định dạng chính xác sau đây (không kèm markdown, không kèm lời giải thích nào khác):
{
  "dailyCalorieGoal": <số_calo_trong_ngày>,
  "dailyProteinGoal": <số_protein_trong_ngày>,
  "dailyFatGoal": <số_chất_béo_trong_ngày>,
  "dailyCarbsGoal": <số_đường_trong_ngày>
}
Ví dụ:
{
  "dailyCalorieGoal": 2000,
  "dailyProteinGoal": 50,
  "dailyFatGoal": 50,
  "dailyCarbsGoal": 50
}`;

        let dailyCalorieGoal;
        let dailyProteinGoal;
        let dailyFatGoal;
        let dailyCarbsGoal;

        try {
            // Requesting AI using the Responses API standard in this workspace
            const response = await openai.responses.create({
                model: "gpt-4.1",
                input: [
                    {
                        role: "user",
                        content: [
                            {
                                type: "input_text",
                                text: prompt
                            }
                        ]
                    }
                ]
            });

            const outputText = response.output_text;

            try {
                const cleanText = outputText.replace(/```json/gi, "").replace(/```/g, "").trim();
                const result = JSON.parse(cleanText);
                dailyCalorieGoal = parseInt(result.dailyCalorieGoal || result.daily_calorie_goal || result.calorieGoal);
                dailyProteinGoal = parseInt(result.dailyProteinGoal || result.daily_protein_goal || result.proteinGoal);
                dailyFatGoal = parseInt(result.dailyFatGoal || result.daily_fat_goal || result.fatGoal);
                dailyCarbsGoal = parseInt(result.dailyCarbsGoal || result.daily_carbs_goal || result.carbsGoal);
            } catch (parseError) {
                console.error("Lỗi khi parse kết quả từ AI:", parseError, outputText);
                // Regex fallback to find 4-digit numbers or any numbers as a backup
                const match = outputText.match(/\d{4}/) || outputText.match(/\d+/);
                if (match) {
                    dailyCalorieGoal = parseInt(match[0]);
                    dailyProteinGoal = parseInt(match[0]);
                    dailyFatGoal = parseInt(match[0]);
                    dailyCarbsGoal = parseInt(match[0]);
                }
            }
        } catch (aiError) {
            console.error("Lỗi khi gọi OpenAI API:", aiError);
        }

        // Standard formula calculation as a solid fallback if AI fails or key is missing
        if (!dailyCalorieGoal || isNaN(dailyCalorieGoal) || !dailyProteinGoal || isNaN(dailyProteinGoal) || !dailyFatGoal || isNaN(dailyFatGoal) || !dailyCarbsGoal || isNaN(dailyCarbsGoal)) {
            const isMale = finalGender.toString().toLowerCase().includes("nam") || finalGender.toString().toLowerCase().includes("male");
            // Mifflin-St Jeor Equation for BMR
            const bmr = 10 * Number(finalWeight) + 6.25 * Number(finalHeight) - 5 * Number(finalAge) + (isMale ? 5 : -161);

            // Adjust factor based on activity level
            let activityFactor = 1.2; // Sedentary
            const intensityLower = finalActivityLevel.toString().toLowerCase();
            if (intensityLower.includes("nhẹ") || intensityLower.includes("light")) activityFactor = 1.375;
            else if (intensityLower.includes("vừa") || intensityLower.includes("moderate")) activityFactor = 1.55;
            else if (intensityLower.includes("nhiều") || intensityLower.includes("heavy") || intensityLower.includes("cường độ cao")) activityFactor = 1.725;

            let calorieTarget = bmr * activityFactor;

            // Adjust based on goal
            const goalLower = finalGoal.toString().toLowerCase();
            if (goalLower.includes("giảm") || goalLower.includes("lose")) {
                calorieTarget -= 500; // Calorie deficit
            } else if (goalLower.includes("tăng") || goalLower.includes("gain")) {
                calorieTarget += 300; // Calorie surplus
            }

            dailyCalorieGoal = Math.round(calorieTarget);
        }

        // Ensure the goal is within realistic limits
        if (dailyCalorieGoal < 1000) dailyCalorieGoal = 1200;
        if (dailyCalorieGoal > 5000) dailyCalorieGoal = 2500;

        // Populate/recalculate macronutrients goals if missing or invalid
        if (!dailyProteinGoal || isNaN(dailyProteinGoal) || dailyProteinGoal >= dailyCalorieGoal) {
            dailyProteinGoal = Math.round((dailyCalorieGoal * 0.3) / 4); // 30% protein
        }
        if (!dailyFatGoal || isNaN(dailyFatGoal) || dailyFatGoal >= dailyCalorieGoal) {
            dailyFatGoal = Math.round((dailyCalorieGoal * 0.3) / 9); // 30% fat
        }
        if (!dailyCarbsGoal || isNaN(dailyCarbsGoal) || dailyCarbsGoal >= dailyCalorieGoal) {
            dailyCarbsGoal = Math.round((dailyCalorieGoal * 0.4) / 4); // 40% carbs
        }

        // Save parameters to User model in MongoDB
        const updatedUser = await User.findByIdAndUpdate(
            userId,
            {
                age: finalAge,
                gender: finalGender,
                height: finalHeight,
                weight: finalWeight,
                goal: finalGoal,
                activityLevel: finalActivityLevel,
                dailyCalorieGoal: dailyCalorieGoal
            },
            { new: true }
        ).select("-password");

        if (!updatedUser) {
            return res.status(404).json({
                success: false,
                message: "Không tìm thấy người dùng."
            });
        }

        // Save macronutrient goals to separate UserGoal model
        const userGoal = await UserGoal.findOneAndUpdate(
            { userId: userId },
            {
                dailyCalorieGoal: dailyCalorieGoal,
                dailyProteinGoal: dailyProteinGoal,
                dailyFatGoal: dailyFatGoal,
                dailyCarbsGoal: dailyCarbsGoal
            },
            { new: true, upsert: true }
        );

        return res.json({
            success: true,
            message: "Thiết lập cấu hình thông số ban đầu thành công",
            data: {
                dailyCalorieGoal: updatedUser.dailyCalorieGoal,
                user: updatedUser,
                nutritionGoal: userGoal
            }
        });

    } catch (error) {
        console.error("Lỗi API setupConfig:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi thiết lập thông số ban đầu.",
            error: error.message
        });
    }
};

/**
 * API kiểm tra và lấy thông số ban đầu
 * Method: GET /api/user/config
 * Header: Authorization: Bearer <token>
 */
exports.getConfig = async (req, res) => {
    try {
        const userId = req.user.userId;
        const user = await User.findById(userId);

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "Không tìm thấy người dùng."
            });
        }

        // Trường hợp user này chưa có cấu hình (dailyCalorieGoal là null hoặc các thông số chưa thiết lập)
        // Thì trả về null để báo client điều hướng màn hình thiết lập ban đầu
        if (!user.dailyCalorieGoal || !user.age) {
            return res.json({
                success: false,
                message: "Thiếu thông tin cấu hình ban đầu. Hãy thiết lập thông số ban đầu để sử dụng ứng dụng."
            });
        }

        const nutritionGoal = await UserGoal.findOne({ userId });

        return res.json({
            success: true,
            data: {
                dailyCalorieGoal: user.dailyCalorieGoal,
                age: user.age,
                gender: user.gender,
                height: user.height,
                weight: user.weight,
                goal: user.goal,
                activityLevel: user.activityLevel,
                dailyProteinGoal: nutritionGoal ? nutritionGoal.dailyProteinGoal : Math.round((user.dailyCalorieGoal * 0.3) / 4),
                dailyFatGoal: nutritionGoal ? nutritionGoal.dailyFatGoal : Math.round((user.dailyCalorieGoal * 0.3) / 9),
                dailyCarbsGoal: nutritionGoal ? nutritionGoal.dailyCarbsGoal : Math.round((user.dailyCalorieGoal * 0.4) / 4)
            }
        });

    } catch (error) {
        console.error("Lỗi API getConfig:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi kiểm tra thông số.",
            error: error.message
        });
    }
};

/**
 * API lấy thông tin chi tiết user kết hợp cấu hình
 * Method: GET /api/user/profile
 * Header: Authorization: Bearer <token>
 */
exports.getUserProfile = async (req, res) => {
    try {
        const userId = req.user.userId;
        const user = await User.findById(userId);

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "Không tìm thấy người dùng."
            });
        }

        // 1. Lấy ngày hôm nay định dạng YYYY-MM-DD theo múi giờ UTC+7
        const dateString = getUTC7DateString();

        // 2. Quét lịch sử ăn uống thực tế (Food) trong ngày để đồng bộ hóa và tính tổng thực tế tiêu thụ
        const { startOfDay, endOfDay } = getUTCStartAndEndOfDayInUTC7(dateString);

        const foodsEaten = await Food.find({
            createBy: userId,
            createdAt: {
                $gte: startOfDay,
                $lte: endOfDay
            }
        });

        let totalCalories = 0;
        let totalProtein = 0;
        let totalFat = 0;
        let totalCarbs = 0;

        for (const log of foodsEaten) {
            if (log.foods && Array.isArray(log.foods)) {
                for (const food of log.foods) {
                    totalCalories += food.calories || 0;
                    totalProtein += food.protein || 0;
                    totalFat += food.fat || 0;
                    totalCarbs += food.carbs || 0;
                }
            }
        }

        // 3. Cập nhật hoặc chèn mới vào bảng DailyReport chỉ khi có dữ liệu tiêu thụ
        let dailyReport = null;
        if (foodsEaten.length > 0) {
            dailyReport = await DailyReport.findOneAndUpdate(
                { userId, date: dateString },
                {
                    consumedCalories: totalCalories,
                    consumedProtein: totalProtein,
                    consumedFat: totalFat,
                    consumedCarbs: totalCarbs
                },
                { new: true, upsert: true }
            );
        } else {
            dailyReport = await DailyReport.findOne({ userId, date: dateString });
        }

        const dailyReportData = dailyReport ? {
            _id: dailyReport._id,
            userId: dailyReport.userId,
            date: dailyReport.date,
            consumedCalories: dailyReport.consumedCalories || 0,
            consumedProtein: dailyReport.consumedProtein || 0,
            consumedFat: dailyReport.consumedFat || 0,
            consumedCarbs: dailyReport.consumedCarbs || 0,
            createdAt: dailyReport.createdAt,
            updatedAt: dailyReport.updatedAt
        } : {
            userId: userId,
            date: dateString,
            consumedCalories: 0,
            consumedProtein: 0,
            consumedFat: 0,
            consumedCarbs: 0
        };

        // 4. Lấy mục tiêu dinh dưỡng UserGoal
        let nutritionGoal = await UserGoal.findOne({ userId });

        // Nếu chưa có bảng UserGoal (ví dụ user cũ), tự động tạo bản ghi mặc định cho họ
        if (!nutritionGoal && user.dailyCalorieGoal) {
            nutritionGoal = await UserGoal.findOneAndUpdate(
                { userId: userId },
                {
                    dailyCalorieGoal: user.dailyCalorieGoal,
                    dailyProteinGoal: Math.round((user.dailyCalorieGoal * 0.3) / 4),
                    dailyFatGoal: Math.round((user.dailyCalorieGoal * 0.3) / 9),
                    dailyCarbsGoal: Math.round((user.dailyCalorieGoal * 0.4) / 4)
                },
                { new: true, upsert: true }
            );
        }

        return res.json({
            success: true,
            data: {
                user: {
                    _id: user._id,
                    full_name: user.full_name,
                    email: user.email,
                    avatar: user.avatar,
                    createdAt: user.createdAt
                },
                config: {
                    age: user.age,
                    gender: user.gender,
                    height: user.height,
                    weight: user.weight,
                    goal: user.goal,
                    activityLevel: user.activityLevel,
                    dailyCalorieGoal: user.dailyCalorieGoal
                },
                nutritionGoal: nutritionGoal || {
                    dailyCalorieGoal: user.dailyCalorieGoal || 2000,
                    dailyProteinGoal: Math.round(((user.dailyCalorieGoal || 2000) * 0.3) / 4),
                    dailyFatGoal: Math.round(((user.dailyCalorieGoal || 2000) * 0.3) / 9),
                    dailyCarbsGoal: Math.round(((user.dailyCalorieGoal || 2000) * 0.4) / 4)
                },
                dailyReport: dailyReportData
            }
        });

    } catch (error) {
        console.error("Lỗi API getUserProfile:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi lấy thông tin người dùng.",
            error: error.message
        });
    }
};

/**
 * API báo cáo kết quả và tiến độ dinh dưỡng hàng ngày
 * Method: GET /api/user/daily-report
 * Header: Authorization: Bearer <token>
 * Query: date (định dạng YYYY-MM-DD, ví dụ: 2026-05-24)
 */
exports.getDailyReport = async (req, res) => {
    try {
        const userId = req.user.userId;
        const { date } = req.query;

        // Định dạng mặc định là ngày hôm nay YYYY-MM-DD theo múi giờ UTC+7
        const dateString = date || getUTC7DateString();

        // Tìm thông tin người dùng
        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({
                success: false,
                message: "Không tìm thấy người dùng."
            });
        }

        // 1. Quét lịch sử ăn uống thực tế (Food) trong ngày để đồng bộ hóa và tính tổng thực tế
        const { startOfDay, endOfDay } = getUTCStartAndEndOfDayInUTC7(dateString);

        const foodsEaten = await Food.find({
            createBy: userId,
            createdAt: {
                $gte: startOfDay,
                $lte: endOfDay
            }
        });

        let totalCalories = 0;
        let totalProtein = 0;
        let totalFat = 0;
        let totalCarbs = 0;

        for (const log of foodsEaten) {
            if (log.foods && Array.isArray(log.foods)) {
                for (const food of log.foods) {
                    totalCalories += food.calories || 0;
                    totalProtein += food.protein || 0;
                    totalFat += food.fat || 0;
                    totalCarbs += food.carbs || 0;
                }
            }
        }

        // 2. Cập nhật hoặc chèn mới vào bảng DailyReport
        const dailyReport = await DailyReport.findOneAndUpdate(
            { userId, date: dateString },
            {
                consumedCalories: totalCalories,
                consumedProtein: totalProtein,
                consumedFat: totalFat,
                consumedCarbs: totalCarbs
            },
            { new: true, upsert: true }
        );

        // 3. Lấy mục tiêu dinh dưỡng UserGoal
        const nutritionGoal = await UserGoal.findOne({ userId });

        const calorieGoal = nutritionGoal ? nutritionGoal.dailyCalorieGoal : (user.dailyCalorieGoal || 2000);
        const proteinGoal = nutritionGoal ? nutritionGoal.dailyProteinGoal : Math.round((calorieGoal * 0.3) / 4);
        const fatGoal = nutritionGoal ? nutritionGoal.dailyFatGoal : Math.round((calorieGoal * 0.3) / 9);
        const carbsGoal = nutritionGoal ? nutritionGoal.dailyCarbsGoal : Math.round((calorieGoal * 0.4) / 4);

        // 4. Tính toán phần trăm và lượng còn lại
        const remainingCalories = Math.max(0, calorieGoal - dailyReport.consumedCalories);
        const remainingProtein = Math.max(0, proteinGoal - dailyReport.consumedProtein);
        const remainingFat = Math.max(0, fatGoal - dailyReport.consumedFat);
        const remainingCarbs = Math.max(0, carbsGoal - dailyReport.consumedCarbs);

        const caloriePercentage = calorieGoal > 0 ? Math.round((dailyReport.consumedCalories / calorieGoal) * 100) : 0;
        const proteinPercentage = proteinGoal > 0 ? Math.round((dailyReport.consumedProtein / proteinGoal) * 100) : 0;
        const fatPercentage = fatGoal > 0 ? Math.round((dailyReport.consumedFat / fatGoal) * 100) : 0;
        const carbsPercentage = carbsGoal > 0 ? Math.round((dailyReport.consumedCarbs / carbsGoal) * 100) : 0;

        return res.json({
            success: true,
            data: {
                date: dateString,
                goals: {
                    calories: calorieGoal,
                    protein: proteinGoal,
                    fat: fatGoal,
                    carbs: carbsGoal
                },
                consumed: {
                    calories: dailyReport.consumedCalories,
                    protein: dailyReport.consumedProtein,
                    fat: dailyReport.consumedFat,
                    carbs: dailyReport.consumedCarbs
                },
                remaining: {
                    calories: remainingCalories,
                    protein: remainingProtein,
                    fat: remainingFat,
                    carbs: remainingCarbs
                },
                percentage: {
                    calories: caloriePercentage,
                    protein: proteinPercentage,
                    fat: fatPercentage,
                    carbs: carbsPercentage
                }
            }
        });

    } catch (error) {
        console.error("Lỗi API getDailyReport:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi lấy báo cáo dinh dưỡng ngày.",
            error: error.message
        });
    }
};

exports.updateProfile = async (req, res) => {
    try {
        const userId = req.user.userId;
        const { fullName, gender, height, weight, avatar } = req.body;

        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "Không tìm thấy người dùng."
            });
        }

        const updateData = {};
        if (fullName) updateData.full_name = fullName;
        if (gender) updateData.gender = gender;
        if (height) updateData.height = height;
        if (weight) updateData.weight = weight;
        if (avatar) updateData.avatar = avatar;

        const updatedUser = await User.findByIdAndUpdate(
            userId,
            updateData,
            { new: true }
        ).select("-password");

        return res.json({
            success: true,
            message: "Cập nhật thông tin thành công",
            data: updatedUser
        });
    } catch (error) {
        console.error("Lỗi API updateProfile:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi cập nhật thông tin.",
            error: error.message
        });
    }
};

