import 'dart:convert';

import 'package:http/http.dart' as http;

import 'api_config.dart';
import 'auth_service.dart';

class ApiService {
  static const Duration requestTimeout = Duration(seconds: 90);

  Future<List<Map<String, dynamic>>> getList(String endpoint) async {
    final uri = Uri.parse('${ApiConfig.baseUrl}$endpoint');
    final token = await AuthService.getStoredToken();

    final response = await http
        .get(
          uri,
          headers: {if (token != null) 'Authorization': 'Bearer $token'},
        )
        .timeout(requestTimeout);

    if (response.statusCode == 401) {
      await AuthService.clearStoredToken();
      throw Exception('Session expired. Please login again.');
    }

    if (response.statusCode != 200) {
      throw Exception(
        'Request failed with status ${response.statusCode}: ${response.body}',
      );
    }

    final decodedData = jsonDecode(response.body);

    if (decodedData is! List) {
      throw Exception('Invalid API response format');
    }

    return decodedData
        .map((item) => Map<String, dynamic>.from(item as Map))
        .toList();
  }
}
