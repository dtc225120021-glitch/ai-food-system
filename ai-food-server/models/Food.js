const mongoose = require("mongoose");

const foodSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User"
    },
    image: String,
    category: {
        type: String,
        default: "Bữa Sáng"
    },
    foods: [
        {
            name: String,
            carbs: Number,
            protein: Number,
            fat: Number,
            calories: Number
        }
    ],
    createdAt: {
        type: Date,
        default: Date.now
    },
    createBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User"
    },
});

module.exports = mongoose.model("Food", foodSchema);