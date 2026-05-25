const mongoose = require("mongoose");

const dailyReportSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        required: true
    },
    date: {
        type: String, // Định dạng YYYY-MM-DD
        required: true
    },
    consumedCalories: {
        type: Number,
        default: 0
    },
    consumedProtein: {
        type: Number,
        default: 0
    },
    consumedFat: {
        type: Number,
        default: 0
    },
    consumedCarbs: {
        type: Number,
        default: 0
    }
}, {
    timestamps: true
});

// Compound index to guarantee uniqueness of report per user per day and enable super fast queries
dailyReportSchema.index({ userId: 1, date: 1 }, { unique: true });

module.exports = mongoose.model("DailyReport", dailyReportSchema);
