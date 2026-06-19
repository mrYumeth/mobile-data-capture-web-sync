import 'dart:io';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../database/local_database_service.dart';
import '../models/captured_record.dart';

class LocalRecordDetailScreen extends StatefulWidget {
  final int recordId;

  const LocalRecordDetailScreen({super.key, required this.recordId});

  @override
  State<LocalRecordDetailScreen> createState() =>
      _LocalRecordDetailScreenState();
}

class _LocalRecordDetailScreenState extends State<LocalRecordDetailScreen> {
  final LocalDatabaseService _databaseService = LocalDatabaseService.instance;

  late Future<CapturedRecord?> _recordFuture;

  @override
  void initState() {
    super.initState();
    _recordFuture = _databaseService.getCapturedRecordById(widget.recordId);
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Record Details')),
      body: FutureBuilder<CapturedRecord?>(
        future: _recordFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  'Error loading record: ${snapshot.error}',
                  textAlign: TextAlign.center,
                ),
              ),
            );
          }

          final record = snapshot.data;

          if (record == null) {
            return const Center(child: Text('Record not found.'));
          }

          final statusColor = _getStatusColor(record.syncStatus);
          final imagePath = record.imagePath;

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Record #${record.id}',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 10,
                          vertical: 6,
                        ),
                        decoration: BoxDecoration(
                          color: statusColor.withValues(alpha: 0.12),
                          borderRadius: BorderRadius.circular(16),
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
                ),
              ),
              const SizedBox(height: 12),
              _DetailCard(
                title: 'Master Data',
                children: [
                  _DetailRow(label: 'Customer', value: record.customerName),
                  _DetailRow(label: 'Location', value: record.locationName),
                  _DetailRow(label: 'Category', value: record.categoryName),
                ],
              ),
              const SizedBox(height: 12),
              _DetailCard(
                title: 'Captured Information',
                children: [
                  _DetailRow(
                    label: 'Captured At',
                    value: _formatDateTime(record.capturedAt),
                  ),
                  _DetailRow(label: 'Description', value: record.description),
                ],
              ),
              const SizedBox(height: 12),
              _DetailCard(
                title: 'GPS Coordinates',
                children: [
                  _DetailRow(
                    label: 'Latitude',
                    value: record.latitude?.toString() ?? 'Not available',
                  ),
                  _DetailRow(
                    label: 'Longitude',
                    value: record.longitude?.toString() ?? 'Not available',
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Captured Image',
                        style: TextStyle(fontWeight: FontWeight.w700),
                      ),
                      const SizedBox(height: 12),
                      if (imagePath == null || imagePath.isEmpty)
                        const Text('No image available.')
                      else if (!File(imagePath).existsSync())
                        Text('Image file not found:\n$imagePath')
                      else
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: Image.file(
                            File(imagePath),
                            height: 260,
                            width: double.infinity,
                            fit: BoxFit.cover,
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _DetailCard extends StatelessWidget {
  final String title;
  final List<Widget> children;

  const _DetailCard({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
            const SizedBox(height: 12),
            ...children,
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  final String label;
  final String value;

  const _DetailRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 110,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
