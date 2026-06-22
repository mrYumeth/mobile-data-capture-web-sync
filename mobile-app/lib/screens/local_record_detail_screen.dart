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

  void _openImagePreview(String imagePath) {
    showDialog(
      context: context,
      builder: (context) {
        return Dialog(
          insetPadding: const EdgeInsets.all(16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(20),
            child: Stack(
              children: [
                InteractiveViewer(
                  child: Image.file(File(imagePath), fit: BoxFit.contain),
                ),
                Positioned(
                  top: 8,
                  right: 8,
                  child: IconButton.filled(
                    onPressed: () => Navigator.of(context).pop(),
                    icon: const Icon(Icons.close),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
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
          final imagePaths = record.imagePaths;

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
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w800,
                        ),
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
                            fontWeight: FontWeight.w700,
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

              _CapturedImagesCard(
                imagePaths: imagePaths,
                onImageTap: _openImagePreview,
              ),
            ],
          );
        },
      ),
    );
  }
}

class _CapturedImagesCard extends StatelessWidget {
  final List<String> imagePaths;
  final void Function(String imagePath) onImageTap;

  const _CapturedImagesCard({
    required this.imagePaths,
    required this.onImageTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.photo_library_outlined),
                const SizedBox(width: 8),
                Text(
                  'Captured Images',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 6),
            Text(
              imagePaths.isEmpty
                  ? 'No images available.'
                  : '${imagePaths.length} image${imagePaths.length == 1 ? '' : 's'} captured.',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            const SizedBox(height: 14),

            if (imagePaths.isEmpty)
              const Text('No images available.')
            else
              GridView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: imagePaths.length,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  crossAxisSpacing: 10,
                  mainAxisSpacing: 10,
                  childAspectRatio: 1,
                ),
                itemBuilder: (context, index) {
                  final imagePath = imagePaths[index];
                  final imageFile = File(imagePath);

                  if (!imageFile.existsSync()) {
                    return Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        border: Border.all(color: Colors.red),
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: Text(
                        'Image file not found:\n$imagePath',
                        style: const TextStyle(fontSize: 11),
                      ),
                    );
                  }

                  return InkWell(
                    onTap: () => onImageTap(imagePath),
                    borderRadius: BorderRadius.circular(14),
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(14),
                      child: Stack(
                        fit: StackFit.expand,
                        children: [
                          Image.file(imageFile, fit: BoxFit.cover),
                          Positioned(
                            left: 8,
                            bottom: 8,
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 8,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: Colors.black.withValues(alpha: 0.60),
                                borderRadius: BorderRadius.circular(999),
                              ),
                              child: Text(
                                '${index + 1}',
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.w700,
                                  fontSize: 12,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
          ],
        ),
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
            Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
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
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
