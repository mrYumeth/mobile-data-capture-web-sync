const { createClient } = require('@supabase/supabase-js');

const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_SERVICE_ROLE_KEY
);

const storageBucket =
  process.env.SUPABASE_STORAGE_BUCKET || 'captured-images';

module.exports = {
  supabase,
  storageBucket,
};