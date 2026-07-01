import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

import 'api_config.dart';

class AuthService {
  static const FlutterSecureStorage _secureStorage = FlutterSecureStorage();

  static const String _tokenKey = 'fieldsync_auth_token';
  static const String _userKey = 'fieldsync_auth_user';

  static Future<String?> getStoredToken() async {
    return _secureStorage.read(key: _tokenKey);
  }

  static Future<Map<String, dynamic>?> getStoredUser() async {
    final userJson = await _secureStorage.read(key: _userKey);

    if (userJson == null || userJson.isEmpty) {
      return null;
    }

    return Map<String, dynamic>.from(jsonDecode(userJson));
  }

  static Future<void> clearSession() async {
    await _secureStorage.delete(key: _tokenKey);
    await _secureStorage.delete(key: _userKey);
  }

  static Future<void> clearStoredToken() async {
    await clearSession();
  }

  Future<bool> login({
    required String username,
    required String password,
  }) async {
    final uri = Uri.parse('${ApiConfig.baseUrl}/api/auth/login');

    final response = await http
        .post(
          uri,
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'username': username.trim(),
            'password': password,
            'clientType': 'mobile',
          }),
        )
        .timeout(const Duration(seconds: 90));

    if (response.statusCode != 200) {
      return false;
    }

    await _saveAuthSession(response.body);
    return true;
  }

  Future<bool> register({
    required String fullName,
    required String username,
    required String password,
    String? email,
  }) async {
    final uri = Uri.parse('${ApiConfig.baseUrl}/api/auth/register');

    final response = await http
        .post(
          uri,
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode({
            'fullName': fullName.trim(),
            'username': username.trim(),
            'email': email?.trim(),
            'password': password,
            'clientType': 'mobile',
          }),
        )
        .timeout(const Duration(seconds: 90));

    if (response.statusCode != 201) {
      return false;
    }

    await _saveAuthSession(response.body);
    return true;
  }

  Future<void> _saveAuthSession(String responseBody) async {
    final decodedData = jsonDecode(responseBody);
    final token = decodedData['token'];
    final user = decodedData['user'];

    if (token is! String || token.isEmpty) {
      throw Exception('Invalid authentication token');
    }

    await _secureStorage.write(key: _tokenKey, value: token);
    await _secureStorage.write(key: _userKey, value: jsonEncode(user));
  }
}
