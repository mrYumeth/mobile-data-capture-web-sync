const { createClient } = require('@supabase/supabase-js');

let cachedSupabaseClient = null;

function isSupabaseStorageConfigured() {
  return Boolean(
    process.env.SUPABASE_URL && process.env.SUPABASE_SERVICE_ROLE_KEY
  );
}

function getSupabaseClient() {
  if (!isSupabaseStorageConfigured()) {
    return null;
  }

  if (!cachedSupabaseClient) {
    cachedSupabaseClient = createClient(
      process.env.SUPABASE_URL,
      process.env.SUPABASE_SERVICE_ROLE_KEY
    );
  }

  return cachedSupabaseClient;
}

const storageBucket =
  process.env.SUPABASE_STORAGE_BUCKET || 'captured-images';

module.exports = {
  getSupabaseClient,
  isSupabaseStorageConfigured,
  storageBucket,
};