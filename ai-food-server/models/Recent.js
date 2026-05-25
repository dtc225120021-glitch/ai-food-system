const mongoose = require("mongoose");

const recentSchema = new mongoose.Schema({
    image: String,

    foods: [
        {
            name: String,
            carbs: Number,
            protein: Number,
            fat: Number,
            calories: Number
        }
    ],
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User"
    },
    createBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User"
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model("Recent", recentSchema);