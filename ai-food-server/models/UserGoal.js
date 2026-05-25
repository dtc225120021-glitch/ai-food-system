const mongoose = require("mongoose");

const userGoalSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        required: true,
        unique: true
    },
    dailyCalorieGoal: {
        type: Number,
        required: true
    },
    dailyProteinGoal: {
        type: Number,
        required: true
    },
    dailyFatGoal: {
        type: Number,
        required: true
    },
    dailyCarbsGoal: {
        type: Number,
        required: true
    }
}, {
    timestamps: true
});

module.exports = mongoose.model("UserGoal", userGoalSchema);
