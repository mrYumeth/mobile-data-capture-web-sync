import '../database/local_database_service.dart';
import 'api_service.dart';

class MasterDataSyncService {
  final ApiService _apiService = ApiService();
  final LocalDatabaseService _databaseService = LocalDatabaseService.instance;

  Future<MasterDataSyncResult> syncMasterData() async {
    final customers = await _apiService.getList('/api/customers');
    final locations = await _apiService.getList('/api/locations');
    final categories = await _apiService.getList('/api/categories');

    await _databaseService.upsertCustomers(customers);
    await _databaseService.upsertLocations(locations);
    await _databaseService.upsertCategories(categories);

    return MasterDataSyncResult(
      customerCount: customers.length,
      locationCount: locations.length,
      categoryCount: categories.length,
    );
  }
}

class MasterDataSyncResult {
  final int customerCount;
  final int locationCount;
  final int categoryCount;

  const MasterDataSyncResult({
    required this.customerCount,
    required this.locationCount,
    required this.categoryCount,
  });
}
