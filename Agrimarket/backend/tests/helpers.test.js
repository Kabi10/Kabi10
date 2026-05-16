const {
  isValidSriLankanPhone,
  generateOTP,
  hashPassword,
  verifyPassword,
  formatCurrency,
  maskPhoneNumber,
} = require("../src/utils/helpers");

describe("Utility Helpers", () => {
  describe("isValidSriLankanPhone", () => {
    test("should return true for valid Sri Lankan numbers", () => {
      expect(isValidSriLankanPhone("+94771234567")).toBe(true);
      expect(isValidSriLankanPhone("+94711234567")).toBe(true);
    });

    test("should return false for invalid numbers", () => {
      expect(isValidSriLankanPhone("123")).toBe(false);
      expect(isValidSriLankanPhone("abc")).toBe(false);
      expect(isValidSriLankanPhone("")).toBe(false);
      expect(isValidSriLankanPhone("0771234567")).toBe(false); // Helper expects +94 prefix
    });
  });

  describe("generateOTP", () => {
    test("should generate a 6-digit OTP", () => {
      const otp = generateOTP();
      expect(otp).toHaveLength(6);
      expect(otp).toMatch(/^\d{6}$/);
    });
  });

  describe("Currency Formatting", () => {
    test("should format LKR correctly", () => {
      // Note: The spaces in the expected output might depend on the locale implementation
      // We'll be flexible or precise depending on what the implementation returns
      // The implementation uses Intl.NumberFormat('en-LK', { style: 'currency', currency: 'LKR' })
      // This usually outputs "LKR 1,000.00"
      const formatted = formatCurrency(1000);
      expect(formatted).toContain("LKR");
      expect(formatted).toContain("1,000.00");
    });
  });

  describe("Phone Masking", () => {
    test("should mask phone number correctly", () => {
      expect(maskPhoneNumber("+94771234567")).toBe("+94*******67");
    });
  });

  describe("Password Hashing", () => {
    test("should hash and validate password", () => {
      const password = "password123";
      const { hash, salt } = hashPassword(password);

      expect(hash).toBeDefined();
      expect(salt).toBeDefined();
      expect(hash).not.toBe(password);

      const isValid = verifyPassword(password, hash, salt);
      expect(isValid).toBe(true);

      const isInvalid = verifyPassword("wrongpassword", hash, salt);
      expect(isInvalid).toBe(false);
    });
  });
});
