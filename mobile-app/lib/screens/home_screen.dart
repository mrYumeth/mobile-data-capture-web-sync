import 'package:flutter/material.dart';

import '../app/app_theme.dart';
import '../services/auth_service.dart';
import 'data_capture_screen.dart';
import 'local_records_screen.dart';
import 'login_screen.dart';
import 'master_data_screen.dart';
import 'sync_status_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  Future<void> _logout(BuildContext context) async {
    await AuthService.clearSession();

    if (!context.mounted) return;

    Navigator.of(
      context,
    ).pushReplacement(MaterialPageRoute(builder: (_) => const LoginScreen()));
  }

  @override
  Widget build(BuildContext context) {
    final actions = [
      _HomeAction(
        title: 'Capture New Record',
        subtitle: 'Capture notes, GPS, image and master data',
        icon: Icons.add_location_alt_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const DataCaptureScreen()));
        },
      ),
      _HomeAction(
        title: 'View Local Records',
        subtitle: 'Browse records stored offline in SQLite',
        icon: Icons.list_alt_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const LocalRecordsScreen()));
        },
      ),
      _HomeAction(
        title: 'Sync Pending Records',
        subtitle: 'Upload pending records to the web app',
        icon: Icons.sync_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const SyncStatusScreen()));
        },
      ),
      _HomeAction(
        title: 'Master Data',
        subtitle: 'Customers, locations and categories',
        icon: Icons.storage_outlined,
        onTap: () {
          Navigator.of(
            context,
          ).push(MaterialPageRoute(builder: (_) => const MasterDataScreen()));
        },
      ),
    ];

    final isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: isDarkMode
                ? const [
                    AppTheme.charcoal,
                    AppTheme.graphite,
                    AppTheme.graphiteLight,
                  ]
                : const [
                    Color(0xFFFFFFFF),
                    AppTheme.lightBackground,
                    AppTheme.lightSurface,
                  ],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: CustomScrollView(
            slivers: [
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 18, 20, 10),
                  child: FutureBuilder<Map<String, dynamic>?>(
                    future: AuthService.getStoredUser(),
                    builder: (context, snapshot) {
                      return _DashboardHeader(
                        isDarkMode: isDarkMode,
                        user: snapshot.data,
                        onLogout: () => _logout(context),
                      );
                    },
                  ),
                ),
              ),
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(20, 10, 20, 24),
                sliver: SliverList.separated(
                  itemCount: actions.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 14),
                  itemBuilder: (context, index) {
                    final item = actions[index];

                    return _DashboardActionCard(
                      action: item,
                      isDarkMode: isDarkMode,
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _DashboardHeader extends StatelessWidget {
  final bool isDarkMode;
  final VoidCallback onLogout;
  final Map<String, dynamic>? user;

  const _DashboardHeader({
    required this.isDarkMode,
    required this.onLogout,
    required this.user,
  });

  String get _displayName {
    final fullName = user?['fullName'] ?? user?['full_name'];
    final username = user?['username'];

    if (fullName != null && fullName.toString().trim().isNotEmpty) {
      return fullName.toString();
    }

    if (username != null && username.toString().trim().isNotEmpty) {
      return username.toString();
    }

    return 'User';
  }

  String get _displayRole {
    final role = user?['role'];

    if (role == null || role.toString().trim().isEmpty) {
      return 'User';
    }

    return role.toString().replaceAll('_', ' ').toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: isDarkMode
            ? Colors.white.withValues(alpha: 0.08)
            : Colors.white.withValues(alpha: 0.82),
        borderRadius: BorderRadius.circular(30),
        border: Border.all(
          color: isDarkMode
              ? Colors.white.withValues(alpha: 0.12)
              : Colors.black.withValues(alpha: 0.06),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.12),
            blurRadius: 32,
            offset: const Offset(0, 18),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                height: 58,
                width: 58,
                padding: const EdgeInsets.all(7),
                decoration: BoxDecoration(
                  color: Colors.white,
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: AppTheme.pink.withValues(alpha: 0.25),
                      blurRadius: 24,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: Image.asset(
                  'assets/images/logo.png',
                  fit: BoxFit.contain,
                  errorBuilder: (_, __, ___) {
                    return const Icon(
                      Icons.assignment_turned_in_outlined,
                      color: AppTheme.pink,
                    );
                  },
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'FieldSync',
                      style: Theme.of(context).textTheme.headlineSmall
                          ?.copyWith(
                            color: isDarkMode ? Colors.white : AppTheme.text,
                            fontWeight: FontWeight.w900,
                          ),
                    ),
                    Text(
                      'Mobile Data Capture',
                      style: TextStyle(
                        color: isDarkMode
                            ? Colors.white.withValues(alpha: 0.70)
                            : AppTheme.mutedText,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Logged in as $_displayName',
                      style: TextStyle(
                        color: isDarkMode
                            ? Colors.white.withValues(alpha: 0.82)
                            : AppTheme.text,
                        fontWeight: FontWeight.w700,
                        fontSize: 13,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'Role: $_displayRole',
                      style: TextStyle(
                        color: isDarkMode
                            ? Colors.white.withValues(alpha: 0.62)
                            : AppTheme.mutedText,
                        fontWeight: FontWeight.w600,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              IconButton.filledTonal(
                tooltip: 'Logout',
                onPressed: onLogout,
                icon: const Icon(Icons.logout_outlined),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Text(
            'Offline field records, GPS, images and cloud sync in one mobile workflow.',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              color: isDarkMode
                  ? Colors.white.withValues(alpha: 0.88)
                  : AppTheme.text,
              height: 1.35,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 18),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: const [
              _StatusPill(
                label: 'SQLite Offline',
                icon: Icons.storage_outlined,
              ),
              _StatusPill(
                label: 'GPS Capture',
                icon: Icons.location_on_outlined,
              ),
              _StatusPill(
                label: 'Image Upload',
                icon: Icons.camera_alt_outlined,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _StatusPill extends StatelessWidget {
  final String label;
  final IconData icon;

  const _StatusPill({required this.label, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
      decoration: BoxDecoration(
        color: AppTheme.pink.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: AppTheme.pink.withValues(alpha: 0.18)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: AppTheme.pink, size: 17),
          const SizedBox(width: 7),
          Text(
            label,
            style: const TextStyle(
              color: AppTheme.pink,
              fontWeight: FontWeight.w800,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}

class _DashboardActionCard extends StatelessWidget {
  final _HomeAction action;
  final bool isDarkMode;

  const _DashboardActionCard({required this.action, required this.isDarkMode});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: isDarkMode
          ? Colors.white.withValues(alpha: 0.08)
          : Colors.white.withValues(alpha: 0.88),
      borderRadius: BorderRadius.circular(24),
      child: InkWell(
        onTap: action.onTap,
        borderRadius: BorderRadius.circular(24),
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(24),
            border: Border.all(
              color: isDarkMode
                  ? Colors.white.withValues(alpha: 0.10)
                  : Colors.black.withValues(alpha: 0.06),
            ),
          ),
          child: Row(
            children: [
              Container(
                height: 54,
                width: 54,
                decoration: BoxDecoration(
                  color: AppTheme.pink.withValues(alpha: 0.14),
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Icon(action.icon, color: AppTheme.pink, size: 28),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      action.title,
                      style: TextStyle(
                        color: isDarkMode ? Colors.white : AppTheme.text,
                        fontWeight: FontWeight.w800,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      action.subtitle,
                      style: TextStyle(
                        color: isDarkMode
                            ? Colors.white.withValues(alpha: 0.66)
                            : AppTheme.mutedText,
                        height: 1.35,
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                Icons.chevron_right,
                color: isDarkMode
                    ? Colors.white.withValues(alpha: 0.55)
                    : AppTheme.mutedText,
              ),
            ],
          ),
        ),
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
