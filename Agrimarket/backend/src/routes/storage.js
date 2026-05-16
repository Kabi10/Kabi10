const express = require("express");
const multer = require("multer");
const sharp = require("sharp");
const { v4: uuidv4 } = require("uuid");
const { supabaseAdmin } = require("../config/supabase");
const logger = require("../utils/logger");

const router = express.Router();

const BUCKET = "listing-images";
const MAX_SIZE = 5 * 1024 * 1024; // 5MB
const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"];

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_SIZE },
  fileFilter: (_req, file, cb) => {
    if (ALLOWED_TYPES.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error("Only JPEG, PNG, and WebP images are allowed"));
    }
  },
});

// Wrap multer to return clean JSON errors for size/type violations
const handleUpload = (req, res, next) => {
  upload.single("image")(req, res, (err) => {
    if (err instanceof multer.MulterError) {
      if (err.code === "LIMIT_FILE_SIZE") {
        return res
          .status(400)
          .json({ success: false, message: "Image exceeds 5MB limit" });
      }
      return res.status(400).json({ success: false, message: err.message });
    }
    if (err) {
      return res.status(400).json({ success: false, message: err.message });
    }
    next();
  });
};

/**
 * POST /upload
 * Accepts multipart image, processes with Sharp, uploads to Supabase Storage.
 * Returns { success, data: { url, path } }
 */
router.post("/upload", handleUpload, async (req, res) => {
  try {
    if (!req.file) {
      return res
        .status(400)
        .json({ success: false, message: "No image provided" });
    }

    if (!supabaseAdmin) {
      logger.error("supabaseAdmin not configured – cannot upload to storage");
      return res
        .status(500)
        .json({ success: false, message: "Storage service unavailable" });
    }

    const userId = req.user?.userId || "anonymous";

    // Normalize to JPEG 85% quality
    let processed;
    try {
      processed = await sharp(req.file.buffer).jpeg({ quality: 85 }).toBuffer();
    } catch (sharpErr) {
      logger.warn(
        "Sharp processing failed, using original buffer",
        sharpErr.message,
      );
      processed = req.file.buffer;
    }

    const filePath = `${userId}/${uuidv4()}.jpg`;

    const { error: uploadError } = await supabaseAdmin.storage
      .from(BUCKET)
      .upload(filePath, processed, {
        contentType: "image/jpeg",
        upsert: false,
      });

    if (uploadError) {
      logger.error("Supabase storage upload failed:", uploadError.message);
      return res.status(500).json({ success: false, message: "Upload failed" });
    }

    const { data: urlData } = supabaseAdmin.storage
      .from(BUCKET)
      .getPublicUrl(filePath);

    res.json({
      success: true,
      data: {
        url: urlData.publicUrl,
        path: filePath,
      },
    });
  } catch (error) {
    logger.error("Storage upload error:", error);
    res.status(500).json({ success: false, message: "Internal server error" });
  }
});

/**
 * DELETE /delete
 * Deletes an image from Supabase Storage.
 * Body: { path: "userId/uuid.jpg" }
 * Only the owning user can delete their own images.
 */
router.delete("/delete", async (req, res) => {
  try {
    const { path } = req.body;

    if (!path || typeof path !== "string") {
      return res
        .status(400)
        .json({ success: false, message: "path is required" });
    }

    const userId = req.user.userId;
    if (!path.startsWith(userId + "/")) {
      return res.status(403).json({
        success: false,
        message: "Forbidden: cannot delete another user's image",
      });
    }

    if (!supabaseAdmin) {
      logger.error("supabaseAdmin not configured – cannot delete from storage");
      return res
        .status(500)
        .json({ success: false, message: "Storage service unavailable" });
    }

    const { error } = await supabaseAdmin.storage.from(BUCKET).remove([path]);

    if (error) {
      logger.error("Supabase storage delete failed:", error.message);
      return res.status(400).json({ success: false, message: error.message });
    }

    res.json({ success: true, message: "Image deleted" });
  } catch (error) {
    logger.error("Storage delete error:", error);
    res.status(500).json({ success: false, message: "Internal server error" });
  }
});

module.exports = router;
