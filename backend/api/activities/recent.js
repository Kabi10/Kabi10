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
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const { userId, limit = 10 } = req.query;

  if (!userId) {
    return res.status(400).json({ error: 'userId is required' });
  }

  try {
    // Get activities from the last 24 hours
    const twentyFourHoursAgo = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();

    const { data: activities, error } = await supabase
      .from('activities')
      .select('*')
      .eq('user_id', userId)
      .eq('status', 'ACTIVE')
      .gte('timestamp', twentyFourHoursAgo)
      .order('timestamp', { ascending: false })
      .limit(parseInt(limit));

    if (error) {
      console.error('Database error:', error);
      return res.status(500).json({ error: 'Failed to fetch recent activities' });
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

    return res.status(200).json({
      activities: transformedActivities,
      totalCount: transformedActivities.length,
      page: 1,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
      lastUpdated: new Date().toISOString()
    });

  } catch (error) {
    console.error('Error fetching recent activities:', error);
    return res.status(500).json({ error: 'Failed to fetch recent activities' });
  }
}