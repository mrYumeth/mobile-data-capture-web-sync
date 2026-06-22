import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;

import '../database/local_database_service.dart';
import '../models/captured_record.dart';
import 'api_config.dart';

class RecordSyncService {
  final LocalDatabaseService _databaseService = LocalDatabaseService.instance;

  Future<RecordSyncResult> syncPendingRecords() async {
    final pendingRecords = await _databaseService.getPendingSyncRecords();

    int syncedCount = 0;
    int failedCount = 0;

    for (final record in pendingRecords) {
      try {
        final serverId = await _uploadRecord(record);

        await _databaseService.markCapturedRecordAsSynced(
          localId: record.id,
          serverId: serverId,
        );

        syncedCount++;
      } catch (_) {
        await _databaseService.markCapturedRecordAsSyncFailed(
          localId: record.id,
        );

        failedCount++;
      }
    }

    return RecordSyncResult(
      totalCount: pendingRecords.length,
      syncedCount: syncedCount,
      failedCount: failedCount,
    );
  }

  Future<int> _uploadRecord(CapturedRecord record) async {
    final uri = Uri.parse('${ApiConfig.baseUrl}/api/captured-records');

    final request = http.MultipartRequest('POST', uri);

    request.fields['customer_id'] = record.customerId.toString();
    request.fields['location_id'] = record.locationId.toString();
    request.fields['category_id'] = record.categoryId.toString();
    request.fields['description'] = record.description;
    request.fields['captured_at'] = record.capturedAt;

    if (record.latitude != null) {
      request.fields['latitude'] = record.latitude.toString();
    }

    if (record.longitude != null) {
      request.fields['longitude'] = record.longitude.toString();
    }

    final imagePaths = record.imagePaths.isNotEmpty
        ? record.imagePaths
        : [
            if (record.imagePath != null && record.imagePath!.isNotEmpty)
              record.imagePath!,
          ];

    for (final imagePath in imagePaths) {
      final imageFile = File(imagePath);

      if (await imageFile.exists()) {
        request.files.add(
          await http.MultipartFile.fromPath('images', imageFile.path),
        );
      }
    }

    final streamedResponse = await request.send().timeout(
      const Duration(seconds: 30),
    );

    final responseBody = await streamedResponse.stream.bytesToString();

    if (streamedResponse.statusCode < 200 ||
        streamedResponse.statusCode >= 300) {
      throw Exception(
        'Upload failed with status ${streamedResponse.statusCode}: $responseBody',
      );
    }

    final decodedData = jsonDecode(responseBody);

    final serverId = decodedData['record']?['id'];

    if (serverId == null) {
      throw Exception('Server ID was not returned by backend.');
    }

    if (serverId is int) {
      return serverId;
    }

    if (serverId is num) {
      return serverId.toInt();
    }

    throw Exception('Invalid server ID returned by backend.');
  }
}

class RecordSyncResult {
  final int totalCount;
  final int syncedCount;
  final int failedCount;

  const RecordSyncResult({
    required this.totalCount,
    required this.syncedCount,
    required this.failedCount,
  });
}
