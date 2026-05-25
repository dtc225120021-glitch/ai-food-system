const multer = require("multer");
const path = require("path");

const storage = multer.diskStorage({

    destination: (req, file, cb) => {
        cb(null, "uploads/");
    },

    filename: (req, file, cb) => {
        // Tạo một mã định danh ngẫu nhiên duy nhất (Timestamp + Số ngẫu nhiên) kèm phần mở rộng ban đầu
        const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
        const extension = path.extname(file.originalname);
        const uniqueName = uniqueSuffix + extension;
        cb(null, uniqueName);
    }

});

const upload = multer({ storage });

module.exports = upload;