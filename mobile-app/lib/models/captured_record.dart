import 'dart:convert';

class CapturedRecord {
  final int id;
  final int customerId;
  final int locationId;
  final int categoryId;
  final String customerName;
  final String locationName;
  final String categoryName;
  final String description;
  final double? latitude;
  final double? longitude;
  final String? imagePath;
  final List<String> imagePaths;
  final String capturedAt;
  final String syncStatus;
  final int? serverId;

  const CapturedRecord({
    required this.id,
    required this.customerId,
    required this.locationId,
    required this.categoryId,
    required this.customerName,
    required this.locationName,
    required this.categoryName,
    required this.description,
    required this.latitude,
    required this.longitude,
    required this.imagePath,
    required this.imagePaths,
    required this.capturedAt,
    required this.syncStatus,
    required this.serverId,
  });

  factory CapturedRecord.fromMap(Map<String, dynamic> map) {
    final parsedImagePaths = _parseImagePaths(map['image_path'] as String?);

    return CapturedRecord(
      id: map['id'] as int,
      customerId: map['customer_id'] as int,
      locationId: map['location_id'] as int,
      categoryId: map['category_id'] as int,
      customerName: map['customer_name'] as String,
      locationName: map['location_name'] as String,
      categoryName: map['category_name'] as String,
      description: map['description'] as String? ?? '',
      latitude: map['latitude'] as double?,
      longitude: map['longitude'] as double?,
      imagePath: parsedImagePaths.isEmpty ? null : parsedImagePaths.first,
      imagePaths: parsedImagePaths,
      capturedAt: map['captured_at'] as String,
      syncStatus: map['sync_status'] as String,
      serverId: map['server_id'] as int?,
    );
  }

  static List<String> _parseImagePaths(String? value) {
    if (value == null || value.trim().isEmpty) {
      return [];
    }

    try {
      final decodedValue = jsonDecode(value);

      if (decodedValue is List) {
        return decodedValue
            .map((item) => item.toString())
            .where((item) => item.trim().isNotEmpty)
            .toList();
      }
    } catch (_) {
      // Old records store one normal image path, not JSON.
    }

    return [value];
  }
}
