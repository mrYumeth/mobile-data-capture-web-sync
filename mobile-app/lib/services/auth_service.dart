import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

import 'api_config.dart';

class AuthResult {
  final bool isSuccess;
  final String message;

  const AuthResult({required this.isSuccess, required this.message});
}

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

  Future<AuthResult> login({
    required String username,
    required String password,
  }) async {
    try {
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

      final decodedData = _decodeResponse(response.body);

      if (response.statusCode != 200) {
        return AuthResult(
          isSuccess: false,
          message:
              decodedData['message']?.toString() ??
              'Invalid username or password.',
        );
      }

      await _saveAuthSession(response.body);

      return const AuthResult(isSuccess: true, message: 'Login successful');
    } catch (error) {
      return AuthResult(
        isSuccess: false,
        message: 'Login failed. Please check your connection and try again.',
      );
    }
  }

  Future<AuthResult> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    try {
      final token = await getStoredToken();

      if (token == null || token.isEmpty) {
        return const AuthResult(
          isSuccess: false,
          message: 'Session expired. Please login again.',
        );
      }

      final uri = Uri.parse('${ApiConfig.baseUrl}/api/auth/change-password');

      final response = await http
          .post(
            uri,
            headers: {
              'Content-Type': 'application/json',
              'Authorization': 'Bearer $token',
            },
            body: jsonEncode({
              'currentPassword': currentPassword,
              'newPassword': newPassword,
            }),
          )
          .timeout(const Duration(seconds: 90));

      final decodedData = _decodeResponse(response.body);

      if (response.statusCode == 401) {
        await clearSession();
      }

      if (response.statusCode != 200) {
        return AuthResult(
          isSuccess: false,
          message:
              decodedData['message']?.toString() ?? 'Password change failed.',
        );
      }

      return AuthResult(
        isSuccess: true,
        message:
            decodedData['message']?.toString() ??
            'Password changed successfully.',
      );
    } catch (error) {
      return AuthResult(
        isSuccess: false,
        message: 'Password change failed. Please try again.',
      );
    }
  }

  Map<String, dynamic> _decodeResponse(String responseBody) {
    try {
      final decodedData = jsonDecode(responseBody);

      if (decodedData is Map<String, dynamic>) {
        return decodedData;
      }

      return {};
    } catch (_) {
      return {};
    }
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
