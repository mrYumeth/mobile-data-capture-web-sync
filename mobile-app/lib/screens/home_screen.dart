import 'master_data_screen.dart';

import 'package:flutter/material.dart';

import 'login_screen.dart';

import 'data_capture_screen.dart';

import 'local_records_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  void _showComingSoon(BuildContext context, String featureName) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('$featureName will be implemented in Phase 1.')),
    );
  }

  void _logout(BuildContext context) {
    Navigator.of(
      context,
    ).pushReplacement(MaterialPageRoute(builder: (_) => const LoginScreen()));
  }

  @override
  Widget build(BuildContext context) {
    final actions = [
      _HomeAction(
        title: 'Capture New Record',
        subtitle: 'Capture customer, location, notes, GPS and image',
        icon: Icons.add_location_alt_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const DataCaptureScreen()));
        },
      ),
      _HomeAction(
        title: 'View Local Records',
        subtitle: 'View records saved locally in SQLite',
        icon: Icons.list_alt_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const LocalRecordsScreen()));
        },
      ),
      _HomeAction(
        title: 'Sync Pending Records',
        subtitle: 'Upload pending records when internet is available',
        icon: Icons.sync_outlined,
        onTap: () => _showComingSoon(context, 'Sync feature'),
      ),
      _HomeAction(
        title: 'Master Data',
        subtitle: 'View locally stored customers, locations and categories',
        icon: Icons.storage_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const MasterDataScreen()));
        },
      ),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Mobile Data Capture'),
        centerTitle: true,
        actions: [
          IconButton(
            tooltip: 'Logout',
            onPressed: () => _logout(context),
            icon: const Icon(Icons.logout_outlined),
          ),
        ],
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: actions.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final item = actions[index];

          return Card(
            elevation: 1,
            child: ListTile(
              leading: Icon(item.icon, size: 32),
              title: Text(
                item.title,
                style: const TextStyle(fontWeight: FontWeight.w600),
              ),
              subtitle: Text(item.subtitle),
              trailing: const Icon(Icons.chevron_right),
              onTap: item.onTap,
            ),
          );
        },
      ),
    );
  }
}

class _HomeAction {
  final String title;
  final String subtitle;
  final IconData icon;
  final VoidCallback onTap;

  const _HomeAction({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.onTap,
  });
}
