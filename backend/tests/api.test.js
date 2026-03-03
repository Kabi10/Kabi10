const request = require("supertest");
const app = require("../src/server");
const db = require("../src/database/connection");
const smsService = require("../src/services/smsService");

// Mock dependencies
jest.mock("../src/database/connection", () => ({
  query: jest.fn(),
  end: jest.fn(),
}));

jest.mock("../src/services/smsService", () => ({
  sendSMS: jest.fn(),
  isMockMode: jest.fn().mockReturnValue(true),
}));

// Mock logger to keep test output clean
jest.mock("../src/utils/logger", () => ({
  info: jest.fn(),
  error: jest.fn(),
  debug: jest.fn(),
}));

describe("API Endpoints", () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe("GET /health", () => {
    it("should return 200 OK", async () => {
      // Mock db.healthCheck for the detailed health check if called,
      // but /health is simple.
      // Wait, /health calls db.healthCheck? No, /health/database does.
      // /health calls nothing.

      const res = await request(app).get("/health");
      expect(res.statusCode).toBe(200);
      expect(res.body.status).toBe("healthy");
    });
  });

  describe("POST /api/v1/auth/send-otp", () => {
    it("should validate phone number format", async () => {
      const res = await request(app)
        .post("/api/v1/auth/send-otp")
        .send({ phoneNumber: "123" }); // Invalid

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it("should send OTP for valid number", async () => {
      // Mock DB response for rate limiting (no recent OTPs)
      db.query.mockResolvedValueOnce({ rows: [] });
      // Mock DB insert - must return the OTP id
      db.query.mockResolvedValueOnce({ rows: [{ id: "test-otp-123" }] });

      // Mock SMS success
      smsService.sendSMS.mockResolvedValue(true);

      const res = await request(app)
        .post("/api/v1/auth/send-otp")
        .send({ phoneNumber: "+94771234567" });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);

      // Verify mocks called
      expect(db.query).toHaveBeenCalledTimes(2); // Rate limit check + Insert
      expect(smsService.sendSMS).toHaveBeenCalled();
    });
  });
});
