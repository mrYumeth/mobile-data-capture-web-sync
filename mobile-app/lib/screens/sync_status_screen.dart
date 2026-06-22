import 'package:flutter/material.dart';

import '../database/local_database_service.dart';
import '../services/record_sync_service.dart';

class SyncStatusScreen extends StatefulWidget {
  const SyncStatusScreen({super.key});

  @override
  State<SyncStatusScreen> createState() => _SyncStatusScreenState();
}

class _SyncStatusScreenState extends State<SyncStatusScreen> {
  final LocalDatabaseService _databaseService = LocalDatabaseService.instance;
  final RecordSyncService _recordSyncService = RecordSyncService();

  late Future<Map<String, int>> _syncCountsFuture;

  bool _isSyncing = false;

  @override
  void initState() {
    super.initState();
    _loadSyncCounts();
  }

  void _loadSyncCounts() {
    _syncCountsFuture = _databaseService.getSyncStatusCounts();
  }

  Future<void> _refreshCounts() async {
    setState(() {
      _loadSyncCounts();
    });
  }

  Future<void> _syncPendingRecords() async {
    setState(() {
      _isSyncing = true;
    });

    try {
      final result = await _recordSyncService.syncPendingRecords();

      setState(() {
        _loadSyncCounts();
      });

      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Sync completed. Total: ${result.totalCount}, '
            'Synced: ${result.syncedCount}, '
            'Failed: ${result.failedCount}.',
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Sync failed: $error'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isSyncing = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sync Status'),
        actions: [
          IconButton(
            tooltip: 'Refresh',
            onPressed: _isSyncing ? null : _refreshCounts,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: FutureBuilder<Map<String, int>>(
        future: _syncCountsFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          if (snapshot.hasError) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  'Error loading sync status: ${snapshot.error}',
                  textAlign: TextAlign.center,
                ),
              ),
            );
          }

          final counts =
              snapshot.data ??
              {'total': 0, 'pending': 0, 'synced': 0, 'failed': 0};

          return RefreshIndicator(
            onRefresh: _refreshCounts,
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                _StatusSummaryCard(
                  title: 'Total Local Records',
                  count: counts['total'] ?? 0,
                  icon: Icons.folder_copy_outlined,
                  color: Colors.blue,
                ),
                const SizedBox(height: 12),
                _StatusSummaryCard(
                  title: 'Pending Sync',
                  count: counts['pending'] ?? 0,
                  icon: Icons.schedule_outlined,
                  color: Colors.orange,
                ),
                const SizedBox(height: 12),
                _StatusSummaryCard(
                  title: 'Synced',
                  count: counts['synced'] ?? 0,
                  icon: Icons.check_circle_outline,
                  color: Colors.green,
                ),
                const SizedBox(height: 12),
                _StatusSummaryCard(
                  title: 'Sync Failed',
                  count: counts['failed'] ?? 0,
                  icon: Icons.error_outline,
                  color: Colors.red,
                ),
                const SizedBox(height: 24),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        const Text(
                          'Upload Pending Records',
                          style: TextStyle(fontWeight: FontWeight.w700),
                        ),
                        const SizedBox(height: 8),
                        const Text(
                          'This will upload all pending or previously failed '
                          'local records to the backend API. Successfully '
                          'uploaded records will be marked as Synced.',
                        ),
                        const SizedBox(height: 16),
                        FilledButton.icon(
                          onPressed: _isSyncing ? null : _syncPendingRecords,
                          icon: _isSyncing
                              ? const SizedBox(
                                  height: 18,
                                  width: 18,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Icon(Icons.sync_outlined),
                          label: Text(
                            _isSyncing ? 'Syncing...' : 'Sync Pending Records',
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _StatusSummaryCard extends StatelessWidget {
  final String title;
  final int count;
  final IconData icon;
  final Color color;

  const _StatusSummaryCard({
    required this.title,
    required this.count,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: color.withValues(alpha: 0.12),
          child: Icon(icon, color: color),
        ),
        title: Text(title),
        trailing: Text(
          count.toString(),
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
      ),
    );
  }
}
