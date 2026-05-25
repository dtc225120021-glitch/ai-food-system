require("dotenv").config();

const OpenAI = require("openai");
const fs = require("fs");
const sharp = require("sharp");

const Recent = require("../models/Recent");

const openai = new OpenAI({
    apiKey: process.env.OPENAI_API_KEY
});

// Helper to safely parse JSON from AI outputs, removing markdown wrappers and extracting valid brackets
const parseJSONSafely = (text) => {
    try {
        const cleanText = text.replace(/```json/gi, "").replace(/```/g, "").trim();
        return JSON.parse(cleanText);
    } catch (e) {
        console.error("Lỗi khi parse JSON gốc:", e, text);
        // Fallback: Cố gắng trích xuất phần nằm giữa [ và ] hoặc { và }
        const arrayStart = text.indexOf("[");
        const arrayEnd = text.lastIndexOf("]");
        if (arrayStart !== -1 && arrayEnd !== -1 && arrayEnd > arrayStart) {
            try {
                return JSON.parse(text.substring(arrayStart, arrayEnd + 1));
            } catch (err) {
                console.error("Lỗi khi parse JSON trích xuất mảng:", err);
            }
        }
        
        const objectStart = text.indexOf("{");
        const objectEnd = text.lastIndexOf("}");
        if (objectStart !== -1 && objectEnd !== -1 && objectEnd > objectStart) {
            try {
                return JSON.parse(text.substring(objectStart, objectEnd + 1));
            } catch (err) {
                console.error("Lỗi khi parse JSON trích xuất đối tượng:", err);
            }
        }
        
        return null;
    }
};

exports.detectFood = async (req, res) => {
    try {
        const userId = req.user.userId;

        const { path } = req.body;

        if (!path) {
            return res.status(400).json({
                status: false,
                error: "path is required"
            });
        }

        if (!fs.existsSync(path)) {
            return res.status(404).json({
                status: false,
                error: "image not found"
            });
        }

        const metadata = await sharp(path).metadata();

        let buffer;

        if (metadata.width > 768) {
            buffer = await sharp(path)
                .resize(768)
                .jpeg({ quality: 70 })
                .toBuffer();
        } else {
            buffer = await sharp(path)
                .jpeg({ quality: 90 })
                .toBuffer();
        }
        const base64Image = buffer.toString("base64");

        const response = await openai.responses.create({
            model: "gpt-4.1",
            input: [
                {
                    role: "user",
                    content: [
                        {
                            type: "input_text",
                            text: `Trong ảnh có thể có nhiều món ăn, tôi muốn tôi biết tên của từng món ăn và khối lượng carbs, protein, fat và calories của từng món ăn. Trả về cho tôi duy nhất 1 json dạng [{ "name": "", "carbs": 0, "protein": 0, "fat": 0, "calories": 0 }]. Nếu không có món ăn nào thì trả về null`
                        },
                        {
                            type: "input_image",
                            image_url: `data:image/jpeg;base64,${base64Image}`
                        }
                    ]
                }
            ]
        });

        const detectedFoods = parseJSONSafely(response.output_text);

        const recentLog = new Recent({
            image: path,
            foods: (detectedFoods && Array.isArray(detectedFoods)) ? detectedFoods.map(food => ({
                name: food.name,
                carbs: Number(food.carbs) || 0,
                protein: Number(food.protein) || 0,
                fat: Number(food.fat) || 0,
                calories: Number(food.calories) || 0
            })) : [], // Nếu không detect ra gì (null), foods sẽ là mảng rỗng để biểu thị kết quả trống
            userId: userId,
            createBy: userId
        });
        const savedRecent = await recentLog.save();

        res.json({
            status: true,
            image: path,
            result: detectedFoods,
            recent: savedRecent
        });
    } catch (error) {
        res.status(500).json({
            status: false,
            error: error.message
        });
    }
};