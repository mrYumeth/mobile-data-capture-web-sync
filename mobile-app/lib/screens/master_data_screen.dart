import 'package:flutter/material.dart';

import '../database/local_database_service.dart';
import '../models/master_data_item.dart';
import '../services/master_data_sync_service.dart';

class MasterDataScreen extends StatefulWidget {
  const MasterDataScreen({super.key});

  @override
  State<MasterDataScreen> createState() => _MasterDataScreenState();
}

class _MasterDataScreenState extends State<MasterDataScreen> {
  final _databaseService = LocalDatabaseService.instance;
  final _syncService = MasterDataSyncService();

  bool _isSyncing = false;

  late Future<List<MasterDataItem>> _customersFuture;
  late Future<List<MasterDataItem>> _locationsFuture;
  late Future<List<MasterDataItem>> _categoriesFuture;

  @override
  void initState() {
    super.initState();
    _loadMasterData();
  }

  void _loadMasterData() {
    _customersFuture = _databaseService.getCustomers();
    _locationsFuture = _databaseService.getLocations();
    _categoriesFuture = _databaseService.getCategories();
  }

  Future<void> _refreshData() async {
    setState(() {
      _loadMasterData();
    });
  }

  Future<void> _syncMasterDataFromServer() async {
    setState(() {
      _isSyncing = true;
    });

    try {
      final result = await _syncService.syncMasterData();

      setState(() {
        _loadMasterData();
      });

      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Master data synced: ${result.customerCount} customers, '
            '${result.locationCount} locations, '
            '${result.categoryCount} categories.',
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Master data sync failed: $error'),
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
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Master Data'),
          actions: [
            IconButton(
              tooltip: 'Sync from server',
              onPressed: _isSyncing ? null : _syncMasterDataFromServer,
              icon: _isSyncing
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: Colors.white,
                      ),
                    )
                  : const Icon(Icons.cloud_sync),
            ),
          ],
          bottom: const TabBar(
            tabs: [
              Tab(text: 'Customers'),
              Tab(text: 'Locations'),
              Tab(text: 'Categories'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _MasterDataList(
              future: _customersFuture,
              emptyMessage: 'No customers found.',
              onRefresh: _refreshData,
            ),
            _MasterDataList(
              future: _locationsFuture,
              emptyMessage: 'No locations found.',
              onRefresh: _refreshData,
            ),
            _MasterDataList(
              future: _categoriesFuture,
              emptyMessage: 'No categories found.',
              onRefresh: _refreshData,
            ),
          ],
        ),
      ),
    );
  }
}

class _MasterDataList extends StatelessWidget {
  final Future<List<MasterDataItem>> future;
  final String emptyMessage;
  final Future<void> Function() onRefresh;

  const _MasterDataList({
    required this.future,
    required this.emptyMessage,
    required this.onRefresh,
  });

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<MasterDataItem>>(
      future: future,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (snapshot.hasError) {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Text(
                'Error loading data: ${snapshot.error}',
                textAlign: TextAlign.center,
              ),
            ),
          );
        }

        final items = snapshot.data ?? [];

        if (items.isEmpty) {
          return Center(child: Text(emptyMessage));
        }

        return RefreshIndicator(
          onRefresh: onRefresh,
          child: ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: items.length,
            separatorBuilder: (_, __) => const SizedBox(height: 8),
            itemBuilder: (context, index) {
              final item = items[index];

              return Card(
                child: ListTile(
                  leading: CircleAvatar(child: Text(item.id.toString())),
                  title: Text(item.name),
                  subtitle: item.subtitle == null || item.subtitle!.isEmpty
                      ? null
                      : Text(item.subtitle!),
                ),
              );
            },
          ),
        );
      },
    );
  }
}
