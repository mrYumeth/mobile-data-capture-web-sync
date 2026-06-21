import 'dart:convert';

import 'package:http/http.dart' as http;

import 'api_config.dart';

class ApiService {
  Future<List<Map<String, dynamic>>> getList(String endpoint) async {
    final uri = Uri.parse('${ApiConfig.baseUrl}$endpoint');

    final response = await http.get(uri).timeout(const Duration(seconds: 15));

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
