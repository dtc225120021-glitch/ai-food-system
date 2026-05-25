const Recent = require("../models/Recent");

exports.recents = async (req, res) => {
    try {
        const userId = req.user.userId;
        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "Không tìm thấy thông tin người dùng trong token."
            });
        }

        const recents = await Recent.find({
            createBy: userId
        }).sort({ createdAt: -1 });

        return res.json({
            success: true,
            data: recents
        });
    } catch (error) {
        console.error("Lỗi API recents:", error);
        return res.status(500).json({
            success: false,
            message: "Đã xảy ra lỗi hệ thống khi lấy lịch sử quét.",
            error: error.message
        });
    }
};

exports.fakeRecent = async (req, res) => {
    try {

        const userId = req.user.userId;

        const fakeData = [
            {
                image: "uploads/pho.jpg",

                foods: [
                    {
                        name: "Phở bò",
                        carbs: 45,
                        protein: 25,
                        fat: 10,
                        calories: 350
                    }
                ],
                createBy: userId
            },
            {
                image: "uploads/burger.jpg",

                foods: [
                    {
                        name: "Burger",
                        carbs: 30,
                        protein: 20,
                        fat: 15,
                        calories: 295
                    }
                ],
                createBy: userId
            },
            {
                image: "uploads/sushi.jpg",

                foods: [
                    {
                        name: "Sushi",
                        carbs: 25,
                        protein: 12,
                        fat: 5,
                        calories: 180
                    }
                ],
                createBy: userId
            }
        ];

        const inserted = await Recent.insertMany(fakeData);

        res.json({
            message: "Fake data created",
            count: inserted.length,
            data: inserted
        });

    } catch (error) {

        res.status(500).json({
            error: error.message
        });

    }
};