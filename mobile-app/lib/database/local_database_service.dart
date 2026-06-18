import 'package:path/path.dart' as path;
import 'package:sqflite/sqflite.dart';

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
}
