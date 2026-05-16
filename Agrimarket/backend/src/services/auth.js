const jwt = require("jsonwebtoken");
const bcrypt = require("bcryptjs");
const { supabase, supabaseAdmin } = require("../config/supabase");

class AuthService {
  constructor() {
    this.client = supabase;
    this.adminClient = supabaseAdmin;
  }

  // Generate OTP
  generateOTP(length = 6) {
    return Math.floor(Math.random() * 10 ** length)
      .toString()
      .padStart(length, "0");
  }

  // Send OTP (integrate with your SMS service)
  async sendOTP(phone, purpose = "login") {
    const otp = this.generateOTP();
    const expiresAt = new Date(Date.now() + 5 * 60 * 1000); // 5 minutes

    // Store OTP in database
    const { data, error } = await this.adminClient
      .from("otp_verifications")
      .insert({
        phone,
        otp_code: otp,
        purpose,
        expires_at: expiresAt.toISOString(),
      })
      .select()
      .single();

    if (error) throw error;

    // TODO: Integrate with your SMS service here
    // For now, return OTP for testing (remove in production)
    if (process.env.NODE_ENV === "development") {
      console.log(`OTP for ${phone}: ${otp}`);
      return { success: true, otp }; // Remove this in production
    }

    return { success: true };
  }

  // Verify OTP
  async verifyOTP(phone, otp, purpose = "login") {
    const { data, error } = await this.adminClient
      .from("otp_verifications")
      .select("*")
      .eq("phone", phone)
      .eq("otp_code", otp)
      .eq("purpose", purpose)
      .eq("verified", false)
      .gte("expires_at", new Date().toISOString())
      .order("created_at", { ascending: false })
      .limit(1)
      .single();

    if (error || !data) {
      throw new Error("Invalid or expired OTP");
    }

    // Mark OTP as verified
    await this.adminClient
      .from("otp_verifications")
      .update({ verified: true })
      .eq("id", data.id);

    return true;
  }

  // Register user with phone
  async registerWithPhone(phone, userData) {
    try {
      // Check if user already exists
      const existingUser = await this.getUserByPhone(phone);
      if (existingUser) {
        throw new Error("User already exists");
      }

      // Create user in Supabase Auth (using phone as email for compatibility)
      const { data: authData, error: authError } =
        await this.adminClient.auth.admin.createUser({
          phone,
          phone_confirmed: true,
          user_metadata: userData,
        });

      if (authError) throw authError;

      // Create user profile
      const { data: profileData, error: profileError } = await this.adminClient
        .from("users")
        .insert({
          id: authData.user.id,
          phone,
          ...userData,
        })
        .select()
        .single();

      if (profileError) throw profileError;

      return {
        user: profileData,
        session: authData.session,
      };
    } catch (error) {
      throw error;
    }
  }

  // Login with phone
  async loginWithPhone(phone) {
    try {
      // Get user profile
      const user = await this.getUserByPhone(phone);
      if (!user) {
        throw new Error("User not found");
      }

      // Generate custom JWT token
      const token = jwt.sign(
        {
          sub: user.id,
          phone: user.phone,
          role: "authenticated",
        },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || "24h" },
      );

      return {
        user,
        token,
        expires_in: 24 * 60 * 60, // 24 hours in seconds
      };
    } catch (error) {
      throw error;
    }
  }

  // Get user by phone
  async getUserByPhone(phone) {
    const { data, error } = await this.adminClient
      .from("users")
      .select("*")
      .eq("phone", phone)
      .single();

    if (error && error.code !== "PGRST116") throw error;
    return data;
  }

  // Get user by ID
  async getUserById(id) {
    const { data, error } = await this.adminClient
      .from("users")
      .select("*")
      .eq("id", id)
      .single();

    if (error) throw error;
    return data;
  }

  // Verify JWT token
  async verifyToken(token) {
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      const user = await this.getUserById(decoded.sub);

      if (!user || !user.is_active) {
        throw new Error("User not found or inactive");
      }

      return { user, decoded };
    } catch (error) {
      throw new Error("Invalid token");
    }
  }

  // Refresh token
  async refreshToken(token) {
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET, {
        ignoreExpiration: true,
      });
      const user = await this.getUserById(decoded.sub);

      if (!user || !user.is_active) {
        throw new Error("User not found or inactive");
      }

      // Generate new token
      const newToken = jwt.sign(
        {
          sub: user.id,
          phone: user.phone,
          role: "authenticated",
        },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || "24h" },
      );

      return {
        user,
        token: newToken,
        expires_in: 24 * 60 * 60,
      };
    } catch (error) {
      throw new Error("Invalid refresh token");
    }
  }

  // Update user profile
  async updateProfile(userId, updates) {
    const { data, error } = await this.adminClient
      .from("users")
      .update(updates)
      .eq("id", userId)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  // Deactivate user
  async deactivateUser(userId) {
    const { data, error } = await this.adminClient
      .from("users")
      .update({ is_active: false })
      .eq("id", userId)
      .select()
      .single();

    if (error) throw error;
    return data;
  }
}

module.exports = new AuthService();
