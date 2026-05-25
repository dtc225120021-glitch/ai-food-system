const File = require("../models/File");

exports.uploadFile = async (req, res) => {

    try {

        const userId = req.user.userId;

        const file = req.file;

        if (!file) {
            return res.status(400).json({
                success: false,
                message: "No file uploaded"
            });
        }

        const newFile = new File({
            filename: file.filename,
            path: file.path,
            uploadedBy: userId
        });

        await newFile.save();

        res.json({
            success: true,
            message: "Upload success",
            file: newFile
        });

    } catch (error) {

        res.status(500).json({
            success: false,
            message: error.message
        });

    }
};