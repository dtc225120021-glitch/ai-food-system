const express = require("express");
const router = express.Router();
const authMiddleware = require("../middleware/authMiddleware");
const foodController = require("../controllers/foodController");

router.post(
    "/foods/confirm",
    authMiddleware,
    foodController.confirmFood
);

router.get(
    "/foods",
    authMiddleware,
    foodController.getFoods
);

module.exports = router;
