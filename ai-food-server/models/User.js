const mongoose = require("mongoose");

const userSchema = new mongoose.Schema({
  full_name: {
    type: String,
    required: true,
    unique: true
  },

  email: {
    type: String,
    required: true,
    unique: true
  },

  password: {
    type: String,
    required: true
  },

  avatar: {
    type: String,
    default: "https://img.freepik.com/premium-vector/gundam-wallpaper-background_272430-335.jpg"
  },

  age: {
    type: Number,
    default: null
  },

  gender: {
    type: String,
    default: null
  },

  height: {
    type: Number,
    default: null
  },

  weight: {
    type: Number,
    default: null
  },

  goal: {
    type: String,
    default: null
  },

  activityLevel: {
    type: String,
    default: null
  },

  dailyCalorieGoal: {
    type: Number,
    default: null
  },

  createdAt: {
    type: Date,
    default: Date.now
  },

  resetOtp: {
    type: String,
    default: null
  },

  resetOtpExpires: {
    type: Date,
    default: null
  },

  refreshToken: {
    type: String,
    default: null
  }
});

module.exports = mongoose.model("User", userSchema);