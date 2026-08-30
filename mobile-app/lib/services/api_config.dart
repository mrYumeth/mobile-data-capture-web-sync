/* Following is for local development 
class ApiConfig {
  static const String baseUrl = 'http://192.168.43.222:5000';
} */

/* Deployment 
class ApiConfig {
  static const String baseUrl = 'https://fieldsync-backend-api.onrender.com';
} */

class ApiConfig {
  static const String baseUrl = String.fromEnvironment(
    'FIELDSYNC_API_BASE_URL',
    defaultValue: 'https://fieldsync-backend-api.onrender.com',
  );
}
