import 'dart:convert';

import 'package:path/path.dart' as path;
import 'package:sqflite/sqflite.dart';

import '../models/captured_record.dart';
import '../models/master_data_item.dart';

class LocalDatabaseService {
  static final LocalDatabaseService instance = LocalDatabaseService._internal();

  static Database? _database;

  LocalDatabaseService._internal();

  Future<Database> get database async {
    if (_database != null) {
      return _database!;
    }

    _database = await _initializeDatabase();
    return _database!;
  }

  Future<Database> _initializeDatabase() async {
    final databasePath = await getDatabasesPath();
    final fullPath = path.join(databasePath, 'mobile_data_capture.db');

    return openDatabase(fullPath, version: 1, onCreate: _createDatabase);
  }

  Future<void> _createDatabase(Database db, int version) async {
    await db.execute('''
      CREATE TABLE customers (
        id INTEGER PRIMARY KEY,
        name TEXT NOT NULL,
        phone TEXT,
        email TEXT,
        address TEXT,
        created_at TEXT,
        updated_at TEXT
      )
    ''');

    await db.execute('''
      CREATE TABLE locations (
        id INTEGER PRIMARY KEY,
        name TEXT NOT NULL,
        address TEXT,
        created_at TEXT,
        updated_at TEXT
      )
    ''');

    await db.execute('''
      CREATE TABLE categories (
        id INTEGER PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        created_at TEXT,
        updated_at TEXT
      )
    ''');

    await db.execute('''
      CREATE TABLE captured_records (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        location_id INTEGER NOT NULL,
        category_id INTEGER NOT NULL,
        description TEXT,
        latitude REAL,
        longitude REAL,
        image_path TEXT,
        captured_at TEXT NOT NULL,
        sync_status TEXT NOT NULL DEFAULT 'Pending Sync',
        server_id INTEGER,
        created_at TEXT,
        updated_at TEXT
      )
    ''');

    await _seedInitialMasterData(db);
  }

  Future<void> _seedInitialMasterData(Database db) async {
    final now = DateTime.now().toIso8601String();

    final batch = db.batch();

    batch.insert('customers', {
      'id': 1,
      'name': 'ABC Traders',
      'phone': '0771234567',
      'email': 'abc@example.com',
      'address': 'Colombo',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('customers', {
      'id': 2,
      'name': 'Green Field Supplies',
      'phone': '0719876543',
      'email': 'greenfield@example.com',
      'address': 'Kandy',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('customers', {
      'id': 3,
      'name': 'Metro Retailers',
      'phone': '0765554444',
      'email': 'metro@example.com',
      'address': 'Galle',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('locations', {
      'id': 1,
      'name': 'Colombo Warehouse',
      'address': 'No. 10, Main Street, Colombo',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('locations', {
      'id': 2,
      'name': 'Kandy Branch',
      'address': 'No. 25, Hill Road, Kandy',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('locations', {
      'id': 3,
      'name': 'Galle Distribution Center',
      'address': 'No. 45, Fort Road, Galle',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('categories', {
      'id': 1,
      'name': 'Inspection',
      'description': 'General field inspection record',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('categories', {
      'id': 2,
      'name': 'Delivery Check',
      'description': 'Delivery verification and condition check',
      'created_at': now,
      'updated_at': now,
    });

    batch.insert('categories', {
      'id': 3,
      'name': 'Maintenance',
      'description': 'Maintenance or repair related record',
      'created_at': now,
      'updated_at': now,
    });

    await batch.commit(noResult: true);
  }

  Future<List<MasterDataItem>> getCustomers() async {
    final db = await database;

    final result = await db.query('customers', orderBy: 'name ASC');

    return result.map((row) {
      return MasterDataItem(
        id: row['id'] as int,
        name: row['name'] as String,
        subtitle: row['phone'] as String?,
      );
    }).toList();
  }

  Future<List<MasterDataItem>> getLocations() async {
    final db = await database;

    final result = await db.query('locations', orderBy: 'name ASC');

    return result.map((row) {
      return MasterDataItem(
        id: row['id'] as int,
        name: row['name'] as String,
        subtitle: row['address'] as String?,
      );
    }).toList();
  }

  Future<List<MasterDataItem>> getCategories() async {
    final db = await database;

    final result = await db.query('categories', orderBy: 'name ASC');

    return result.map((row) {
      return MasterDataItem(
        id: row['id'] as int,
        name: row['name'] as String,
        subtitle: row['description'] as String?,
      );
    }).toList();
  }

  Future<int> insertCapturedRecord({
    required int customerId,
    required int locationId,
    required int categoryId,
    required String description,
    required double latitude,
    required double longitude,
    required List<String> imagePaths,
  }) async {
    final db = await database;
    final now = DateTime.now().toIso8601String();

    return db.insert('captured_records', {
      'customer_id': customerId,
      'location_id': locationId,
      'category_id': categoryId,
      'description': description,
      'latitude': latitude,
      'longitude': longitude,
      'image_path': jsonEncode(imagePaths),
      'captured_at': now,
      'sync_status': 'Pending Sync',
      'server_id': null,
      'created_at': now,
      'updated_at': now,
    });
  }

  Future<List<CapturedRecord>> getCapturedRecords() async {
    final db = await database;

    final result = await db.rawQuery('''
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_path,
        cr.captured_at,
        cr.sync_status,
        cr.server_id
      FROM captured_records cr
      INNER JOIN customers c ON c.id = cr.customer_id
      INNER JOIN locations l ON l.id = cr.location_id
      INNER JOIN categories cat ON cat.id = cr.category_id
      ORDER BY cr.id DESC
    ''');

    return result.map((row) => CapturedRecord.fromMap(row)).toList();
  }

  Future<CapturedRecord?> getCapturedRecordById(int id) async {
    final db = await database;

    final result = await db.rawQuery(
      '''
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_path,
        cr.captured_at,
        cr.sync_status,
        cr.server_id
      FROM captured_records cr
      INNER JOIN customers c ON c.id = cr.customer_id
      INNER JOIN locations l ON l.id = cr.location_id
      INNER JOIN categories cat ON cat.id = cr.category_id
      WHERE cr.id = ?
      LIMIT 1
      ''',
      [id],
    );

    if (result.isEmpty) {
      return null;
    }

    return CapturedRecord.fromMap(result.first);
  }

  Future<int> getCapturedRecordCount() async {
    final db = await database;

    final result = await db.rawQuery(
      'SELECT COUNT(*) AS count FROM captured_records',
    );

    return Sqflite.firstIntValue(result) ?? 0;
  }

  Future<int> getCapturedRecordCountByStatus(String status) async {
    final db = await database;

    final result = await db.rawQuery(
      'SELECT COUNT(*) AS count FROM captured_records WHERE sync_status = ?',
      [status],
    );

    return Sqflite.firstIntValue(result) ?? 0;
  }

  Future<Map<String, int>> getSyncStatusCounts() async {
    final total = await getCapturedRecordCount();
    final pending = await getCapturedRecordCountByStatus('Pending Sync');
    final synced = await getCapturedRecordCountByStatus('Synced');
    final failed = await getCapturedRecordCountByStatus('Sync Failed');

    return {
      'total': total,
      'pending': pending,
      'synced': synced,
      'failed': failed,
    };
  }

  Future<void> upsertCustomers(List<Map<String, dynamic>> customers) async {
    final db = await database;
    final batch = db.batch();

    for (final customer in customers) {
      batch.insert('customers', {
        'id': customer['id'],
        'name': customer['name'],
        'phone': customer['phone'],
        'email': customer['email'],
        'address': customer['address'],
        'created_at': customer['created_at'],
        'updated_at': customer['updated_at'],
      }, conflictAlgorithm: ConflictAlgorithm.replace);
    }

    await batch.commit(noResult: true);
  }

  Future<void> upsertLocations(List<Map<String, dynamic>> locations) async {
    final db = await database;
    final batch = db.batch();

    for (final location in locations) {
      batch.insert('locations', {
        'id': location['id'],
        'name': location['name'],
        'address': location['address'],
        'created_at': location['created_at'],
        'updated_at': location['updated_at'],
      }, conflictAlgorithm: ConflictAlgorithm.replace);
    }

    await batch.commit(noResult: true);
  }

  Future<void> upsertCategories(List<Map<String, dynamic>> categories) async {
    final db = await database;
    final batch = db.batch();

    for (final category in categories) {
      batch.insert('categories', {
        'id': category['id'],
        'name': category['name'],
        'description': category['description'],
        'created_at': category['created_at'],
        'updated_at': category['updated_at'],
      }, conflictAlgorithm: ConflictAlgorithm.replace);
    }

    await batch.commit(noResult: true);
  }

  Future<List<CapturedRecord>> getPendingSyncRecords() async {
    final db = await database;

    final result = await db.rawQuery('''
      SELECT
        cr.id,
        cr.customer_id,
        cr.location_id,
        cr.category_id,
        c.name AS customer_name,
        l.name AS location_name,
        cat.name AS category_name,
        cr.description,
        cr.latitude,
        cr.longitude,
        cr.image_path,
        cr.captured_at,
        cr.sync_status,
        cr.server_id
      FROM captured_records cr
      INNER JOIN customers c ON c.id = cr.customer_id
      INNER JOIN locations l ON l.id = cr.location_id
      INNER JOIN categories cat ON cat.id = cr.category_id
      WHERE cr.sync_status = 'Pending Sync'
         OR cr.sync_status = 'Sync Failed'
      ORDER BY cr.id ASC
    ''');

    return result.map((row) => CapturedRecord.fromMap(row)).toList();
  }

  Future<void> markCapturedRecordAsSynced({
    required int localId,
    required int serverId,
  }) async {
    final db = await database;
    final now = DateTime.now().toIso8601String();

    await db.update(
      'captured_records',
      {'sync_status': 'Synced', 'server_id': serverId, 'updated_at': now},
      where: 'id = ?',
      whereArgs: [localId],
    );
  }

  Future<void> markCapturedRecordAsSyncFailed({required int localId}) async {
    final db = await database;
    final now = DateTime.now().toIso8601String();

    await db.update(
      'captured_records',
      {'sync_status': 'Sync Failed', 'updated_at': now},
      where: 'id = ?',
      whereArgs: [localId],
    );
  }
}
