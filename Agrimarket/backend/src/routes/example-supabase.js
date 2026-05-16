const express = require("express");

const router = express.Router();
const DatabaseService = require("../services/database");
const AuthService = require("../services/auth");

// Example: Get all products
router.get("/products", async (req, res) => {
  try {
    const { category, location, available } = req.query;
    const filters = {};

    if (category) filters.category = category;
    if (location) filters.location = location;
    if (available !== undefined) filters.available = available === "true";

    const products = await DatabaseService.getProducts(filters);

    res.json({
      success: true,
      data: products,
      count: products.length,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Example: Create a new product
router.post("/products", async (req, res) => {
  try {
    // In a real app, you'd get user_id from JWT token
    const productData = {
      ...req.body,
      user_id: req.user?.id, // Assuming you have auth middleware
    };

    const product = await DatabaseService.createProduct(productData);

    res.status(201).json({
      success: true,
      data: product,
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Example: Send OTP for login
router.post("/auth/send-otp", async (req, res) => {
  try {
    const { phone } = req.body;

    if (!phone) {
      return res.status(400).json({
        success: false,
        error: "Phone number is required",
      });
    }

    const result = await AuthService.sendOTP(phone, "login");

    res.json({
      success: true,
      message: "OTP sent successfully",
      // Remove this in production:
      ...(process.env.NODE_ENV === "development" && { otp: result.otp }),
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Example: Verify OTP and login
router.post("/auth/verify-otp", async (req, res) => {
  try {
    const { phone, otp } = req.body;

    if (!phone || !otp) {
      return res.status(400).json({
        success: false,
        error: "Phone and OTP are required",
      });
    }

    // Verify OTP
    await AuthService.verifyOTP(phone, otp, "login");

    // Login user
    const result = await AuthService.loginWithPhone(phone);

    res.json({
      success: true,
      data: result,
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      error: error.message,
    });
  }
});

// Example: Upload product image
router.post("/products/:id/images", async (req, res) => {
  try {
    const { id } = req.params;
    const file = req.file; // Assuming you're using multer

    if (!file) {
      return res.status(400).json({
        success: false,
        error: "No file uploaded",
      });
    }

    // Upload to Supabase Storage
    const fileName = `${id}/${Date.now()}-${file.originalname}`;
    const uploadResult = await DatabaseService.uploadFile(
      "product-images",
      fileName,
      file.buffer,
      {
        contentType: file.mimetype,
        upsert: false,
      },
    );

    // Get public URL
    const publicUrl = await DatabaseService.getFileUrl(
      "product-images",
      fileName,
    );

    res.json({
      success: true,
      data: {
        path: uploadResult.path,
        url: publicUrl,
      },
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message,
    });
  }
});

// Example: Real-time subscription setup (for WebSocket or SSE)
router.get("/products/subscribe", (req, res) => {
  // Set up Server-Sent Events
  res.writeHead(200, {
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache",
    Connection: "keep-alive",
    "Access-Control-Allow-Origin": "*",
  });

  // Subscribe to product changes
  const subscription = DatabaseService.subscribeToProducts((payload) => {
    res.write(`data: ${JSON.stringify(payload)}\n\n`);
  });

  // Clean up on client disconnect
  req.on("close", () => {
    subscription.unsubscribe();
  });
});

module.exports = router;
