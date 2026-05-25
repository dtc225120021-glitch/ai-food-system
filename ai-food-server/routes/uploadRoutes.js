const express          = require("express");
const router           = express.Router();
const upload           = require("../middleware/uploadMiddleware");
const authMiddleware   = require("../middleware/authMiddleware");
const uploadController = require("../controllers/uploadController");

router.post(
    "/upload",
    authMiddleware,
    upload.single("file"),
    uploadController.uploadFile
);

module.exports = router;