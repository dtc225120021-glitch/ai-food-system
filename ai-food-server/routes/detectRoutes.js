const express          = require("express");
const router           = express.Router();
const authMiddleware   = require("../middleware/authMiddleware");
const detectController = require("../controllers/detectController");

router.post(
    "/detect-food",
    authMiddleware,
    detectController.detectFood,
);
module.exports = router;