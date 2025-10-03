import { createClient } from '@supabase/supabase-js';

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_ANON_KEY;

if (!supabaseUrl || !supabaseKey) {
  throw new Error('Missing Supabase environment variables');
}

const supabase = createClient(supabaseUrl, supabaseKey);

export default async function handler(req, res) {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  try {
    if (req.method === 'GET') {
      return await getActivities(req, res);
    } else if (req.method === 'POST') {
      return await createActivity(req, res);
    } else {
      return res.status(405).json({ error: 'Method not allowed' });
    }
  } catch (error) {
    console.error('Activities API error:', error);
    return res.status(500).json({ 
      error: 'Internal server error',
      message: error.message 
    });
  }
}

async function getActivities(req, res) {
  const {
    userId,
    activityType,
    status = 'ACTIVE',
    priority,
    isRead,
    isActionable,
    fromDate,
    toDate,
    page = 1,
    limit = 20,
    sortBy = 'timestamp',
    sortOrder = 'desc',
    language = 'en'
  } = req.query;

  try {
    let query = supabase
      .from('activities')
      .select('*');

    // Apply filters
    if (userId) {
      query = query.eq('user_id', userId);
    }

    if (activityType) {
      query = query.eq('activity_type', activityType);
    }

    if (status) {
      query = query.eq('status', status);
    }

    if (priority) {
      query = query.eq('priority', priority);
    }

    if (isRead !== undefined) {
      query = query.eq('is_read', isRead === 'true');
    }

    if (isActionable !== undefined) {
      query = query.eq('is_actionable', isActionable === 'true');
    }

    if (fromDate) {
      query = query.gte('timestamp', fromDate);
    }

    if (toDate) {
      query = query.lte('timestamp', toDate);
    }

    // Apply sorting
    const ascending = sortOrder === 'asc';
    query = query.order(sortBy, { ascending });

    // Apply pagination
    const offset = (parseInt(page) - 1) * parseInt(limit);
    query = query.range(offset, offset + parseInt(limit) - 1);

    const { data: activities, error, count } = await query;

    if (error) {
      console.error('Database error:', error);
      return res.status(500).json({ error: 'Failed to fetch activities' });
    }

    // Transform data to match Android model
    const transformedActivities = (activities || []).map(activity => ({
      id: activity.id,
      userId: activity.user_id,
      activityType: activity.activity_type,
      title: activity.title,
      titleTamil: activity.title_tamil || '',
      titleSinhala: activity.title_sinhala || '',
      description: activity.description,
      descriptionTamil: activity.description_tamil || '',
      descriptionSinhala: activity.description_sinhala || '',
      relatedEntityType: activity.related_entity_type,
      relatedEntityId: activity.related_entity_id,
      priority: activity.priority,
      status: activity.status,
      isRead: activity.is_read,
      isActionable: activity.is_actionable,
      timestamp: activity.timestamp,
      expiresAt: activity.expires_at,
      metadata: activity.metadata || {}
    }));

    // Get total count for pagination
    const { count: totalCount } = await supabase
      .from('activities')
      .select('*', { count: 'exact', head: true });

    const totalPages = Math.ceil((totalCount || 0) / parseInt(limit));
    const hasNext = parseInt(page) < totalPages;
    const hasPrevious = parseInt(page) > 1;

    return res.status(200).json({
      activities: transformedActivities,
      totalCount: totalCount || 0,
      page: parseInt(page),
      totalPages,
      hasNext,
      hasPrevious,
      lastUpdated: new Date().toISOString()
    });

  } catch (error) {
    console.error('Error fetching activities:', error);
    return res.status(500).json({ error: 'Failed to fetch activities' });
  }
}

async function createActivity(req, res) {
  const {
    userId,
    activityType,
    title,
    titleTamil = '',
    titleSinhala = '',
    description,
    descriptionTamil = '',
    descriptionSinhala = '',
    relatedEntityType,
    relatedEntityId,
    priority = 'NORMAL',
    isActionable = false,
    expiresAt,
    metadata = {}
  } = req.body;

  if (!userId || !activityType || !title || !description) {
    return res.status(400).json({ 
      error: 'Missing required fields: userId, activityType, title, description' 
    });
  }

  try {
    const { data: activity, error } = await supabase
      .from('activities')
      .insert([{
        user_id: userId,
        activity_type: activityType,
        title,
        title_tamil: titleTamil,
        title_sinhala: titleSinhala,
        description,
        description_tamil: descriptionTamil,
        description_sinhala: descriptionSinhala,
        related_entity_type: relatedEntityType,
        related_entity_id: relatedEntityId,
        priority,
        status: 'ACTIVE',
        is_read: false,
        is_actionable: isActionable,
        timestamp: new Date().toISOString(),
        expires_at: expiresAt,
        metadata
      }])
      .select()
      .single();

    if (error) {
      console.error('Database error:', error);
      return res.status(500).json({ error: 'Failed to create activity' });
    }

    // Transform response to match Android model
    const transformedActivity = {
      id: activity.id,
      userId: activity.user_id,
      activityType: activity.activity_type,
      title: activity.title,
      titleTamil: activity.title_tamil || '',
      titleSinhala: activity.title_sinhala || '',
      description: activity.description,
      descriptionTamil: activity.description_tamil || '',
      descriptionSinhala: activity.description_sinhala || '',
      relatedEntityType: activity.related_entity_type,
      relatedEntityId: activity.related_entity_id,
      priority: activity.priority,
      status: activity.status,
      isRead: activity.is_read,
      isActionable: activity.is_actionable,
      timestamp: activity.timestamp,
      expiresAt: activity.expires_at,
      metadata: activity.metadata || {}
    };

    return res.status(201).json(transformedActivity);

  } catch (error) {
    console.error('Error creating activity:', error);
    return res.status(500).json({ error: 'Failed to create activity' });
  }
}