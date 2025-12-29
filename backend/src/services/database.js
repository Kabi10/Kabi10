const { supabase, supabaseAdmin } = require('../config/supabase');

class DatabaseService {
  constructor() {
    this.client = supabase;
    this.adminClient = supabaseAdmin;
  }

  // User operations
  async createUser(userData) {
    const { data, error } = await this.adminClient
      .from('users')
      .insert(userData)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async getUserById(id) {
    const { data, error } = await this.client
      .from('users')
      .select('*')
      .eq('id', id)
      .single();

    if (error) throw error;
    return data;
  }

  async getUserByPhone(phone) {
    const { data, error } = await this.client
      .from('users')
      .select('*')
      .eq('phone', phone)
      .single();

    if (error && error.code !== 'PGRST116') throw error; // PGRST116 = no rows returned
    return data;
  }

  async updateUser(id, updates) {
    const { data, error } = await this.client
      .from('users')
      .update(updates)
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  // Product operations
  async createProduct(productData) {
    const { data, error } = await this.client
      .from('products')
      .insert(productData)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async getProducts(filters = {}) {
    let query = this.client
      .from('products')
      .select(`
        *,
        users:user_id (
          id,
          name,
          phone,
          location
        )
      `);

    // Apply filters
    if (filters.category) {
      query = query.eq('category', filters.category);
    }
    if (filters.location) {
      query = query.ilike('location', `%${filters.location}%`);
    }
    if (filters.available) {
      query = query.eq('available', filters.available);
    }
    if (filters.user_id) {
      query = query.eq('user_id', filters.user_id);
    }

    // Sorting
    query = query.order('created_at', { ascending: false });

    const { data, error } = await query;
    if (error) throw error;
    return data;
  }

  async getProductById(id) {
    const { data, error } = await this.client
      .from('products')
      .select(`
        *,
        users:user_id (
          id,
          name,
          phone,
          location
        )
      `)
      .eq('id', id)
      .single();

    if (error) throw error;
    return data;
  }

  async updateProduct(id, updates) {
    const { data, error } = await this.client
      .from('products')
      .update(updates)
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async deleteProduct(id) {
    const { error } = await this.client
      .from('products')
      .delete()
      .eq('id', id);

    if (error) throw error;
    return true;
  }

  // Transaction operations
  async createTransaction(transactionData) {
    const { data, error } = await this.client
      .from('transactions')
      .insert(transactionData)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  async getTransactionsByUser(userId) {
    const { data, error } = await this.client
      .from('transactions')
      .select(`
        *,
        products:product_id (
          id,
          name,
          price,
          unit
        ),
        buyer:buyer_id (
          id,
          name,
          phone
        ),
        seller:seller_id (
          id,
          name,
          phone
        )
      `)
      .or(`buyer_id.eq.${userId},seller_id.eq.${userId}`)
      .order('created_at', { ascending: false });

    if (error) throw error;
    return data;
  }

  async updateTransaction(id, updates) {
    const { data, error } = await this.client
      .from('transactions')
      .update(updates)
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;
    return data;
  }

  // File upload operations
  async uploadFile(bucket, path, file, options = {}) {
    const { data, error } = await this.client.storage
      .from(bucket)
      .upload(path, file, options);

    if (error) throw error;
    return data;
  }

  async getFileUrl(bucket, path) {
    const { data } = this.client.storage
      .from(bucket)
      .getPublicUrl(path);

    return data.publicUrl;
  }

  async deleteFile(bucket, path) {
    const { error } = await this.client.storage
      .from(bucket)
      .remove([path]);

    if (error) throw error;
    return true;
  }

  // Real-time subscriptions
  subscribeToProducts(callback) {
    return this.client
      .channel('products')
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'products' },
        callback,
      )
      .subscribe();
  }

  subscribeToTransactions(userId, callback) {
    return this.client
      .channel(`transactions:${userId}`)
      .on(
        'postgres_changes',
        {
          event: '*',
          schema: 'public',
          table: 'transactions',
          filter: `buyer_id=eq.${userId} or seller_id=eq.${userId}`,
        },
        callback,
      )
      .subscribe();
  }
}

module.exports = new DatabaseService();
