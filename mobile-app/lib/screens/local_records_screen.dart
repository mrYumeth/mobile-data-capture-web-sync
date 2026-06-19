import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../database/local_database_service.dart';
import '../models/captured_record.dart';
import 'local_record_detail_screen.dart';

class LocalRecordsScreen extends StatefulWidget {
  const LocalRecordsScreen({super.key});

  @override
  State<LocalRecordsScreen> createState() => _LocalRecordsScreenState();
}

class _LocalRecordsScreenState extends State<LocalRecordsScreen> {
  final LocalDatabaseService _databaseService = LocalDatabaseService.instance;

  late Future<List<CapturedRecord>> _recordsFuture;

  @override
  void initState() {
    super.initState();
    _loadRecords();
  }

  void _loadRecords() {
    _recordsFuture = _databaseService.getCapturedRecords();
  }

  Future<void> _refreshRecords() async {
    setState(() {
      _loadRecords();
    });
  }

  String _formatDateTime(String value) {
    try {
      final dateTime = DateTime.parse(value);
      return DateFormat('yyyy-MM-dd hh:mm a').format(dateTime);
    } catch (_) {
      return value;
    }
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'Synced':
        return Colors.green;
      case 'Sync Failed':
        return Colors.red;
      default:
        return Colors.orange;
    }
  }

  void _openRecordDetails(CapturedRecord record) {
    Navigator.of(context)
        .push(
          MaterialPageRoute(
            builder: (_) => LocalRecordDetailScreen(recordId: record.id),
          ),
        )
        .then((_) => _refreshRecords());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Local Records')),
      body: FutureBuilder<List<CapturedRecord>>(
        future: _recordsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  'Error loading local records: ${snapshot.error}',
                  textAlign: TextAlign.center,
                ),
              ),
            );
          }

          final records = snapshot.data ?? [];

          if (records.isEmpty) {
            return const Center(child: Text('No local records found.'));
          }

          return RefreshIndicator(
            onRefresh: _refreshRecords,
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: records.length,
              separatorBuilder: (context, index) {
                return const SizedBox(height: 8);
              },
              itemBuilder: (context, index) {
                final record = records[index];
                final statusColor = _getStatusColor(record.syncStatus);

                return Card(
                  child: ListTile(
                    leading: CircleAvatar(child: Text(record.id.toString())),
                    title: Text(record.customerName),
                    subtitle: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('${record.locationName} • ${record.categoryName}'),
                        const SizedBox(height: 4),
                        Text(_formatDateTime(record.capturedAt)),
                        const SizedBox(height: 4),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 4,
                          ),
                          decoration: BoxDecoration(
                            color: statusColor.withValues(alpha: 0.12),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Text(
                            record.syncStatus,
                            style: TextStyle(
                              color: statusColor,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ],
                    ),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => _openRecordDetails(record),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }
}
