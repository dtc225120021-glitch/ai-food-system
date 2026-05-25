const express = require("express");
const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");
const recentController = require("../controllers/recentController");

router.get(
    "/recents",
    authMiddleware,
    recentController.recents,
);

/// Fake recent API
router.post(
    "/fake-recent",
    authMiddleware,
    recentController.fakeRecent
);

module.exports = router;