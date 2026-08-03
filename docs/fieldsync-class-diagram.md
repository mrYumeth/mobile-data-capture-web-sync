```mermaid

classDiagram
direction LR

%% =====================================================
%% DOMAIN CLASSES
%% =====================================================

class Tenant {
    +int id
    +String name
    +String slug
    +DateTime createdAt
    +DateTime updatedAt
}

class User {
    +int id
    +int tenantId
    +String keycloakUserId
    +String fullName
    +String username
    +String email
    +UserRole role
    +bool webAccess
    +bool mobileAccess
    +bool isActive
    +bool isConfirmed
    +canAccessWeb() bool
    +canAccessMobile() bool
    +isAdministrator() bool
}

class Customer {
    +int id
    +int tenantId
    +String name
    +String contactPerson
    +String email
    +String phone
    +String address
    +bool isActive
    +updateDetails() void
}

class Location {
    +int id
    +int tenantId
    +String name
    +String address
    +double latitude
    +double longitude
    +bool isActive
    +updateCoordinates() void
}

class Category {
    +int id
    +int tenantId
    +String name
    +String description
    +bool isActive
    +updateDetails() void
}

class CapturedRecord {
    +int id
    +int tenantId
    +int customerId
    +int locationId
    +int categoryId
    +int capturedByUserId
    +String description
    +double latitude
    +double longitude
    +String imagePath
    +String imageUrl
    +DateTime capturedAt
    +DateTime syncedAt
    +SyncStatus syncStatus
    +markPending() void
    +markSynced() void
    +markFailed() void
}

class AuthSession {
    +String accessToken
    +String refreshToken
    +DateTime expiresAt
    +User currentUser
    +isExpired() bool
}

%% =====================================================
%% ENUMERATIONS
%% =====================================================

class UserRole {
    <<enumeration>>
    ADMIN
    USER
}

class SyncStatus {
    <<enumeration>>
    PENDING
    SYNCED
    FAILED
}

%% =====================================================
%% MOBILE APPLICATION SERVICES
%% =====================================================

class AuthService {
    +loginWithKeycloak() AuthResult
    +refreshSession() AuthSession
    +getCurrentUser() User
    +getAccessToken() String
    +logout() void
    +clearSession() void
}

class KeycloakMobileService {
    +login() TokenResponse
    +refreshToken(refreshToken) TokenResponse
    +logout(idToken) void
}

class ApiService {
    +getCustomers() List~Customer~
    +getLocations() List~Location~
    +getCategories() List~Category~
    +uploadRecord(record) ApiResult
    +uploadImage(imagePath) String
    +getCurrentUser() User
}

class MasterDataSyncService {
    +syncMasterData() SyncResult
    +downloadCustomers() List~Customer~
    +downloadLocations() List~Location~
    +downloadCategories() List~Category~
}

class RecordSyncService {
    +syncPendingRecords() SyncResult
    +uploadRecord(record) bool
    +retryFailedRecords() SyncResult
    +updateLocalSyncStatus(recordId, status) void
}

class LocalDatabaseService {
    +saveRecord(record) int
    +getLocalRecords() List~CapturedRecord~
    +getPendingRecords() List~CapturedRecord~
    +getPendingRecordCount() int
    +replaceCustomers(customers) void
    +replaceLocations(locations) void
    +replaceCategories(categories) void
    +updateSyncStatus(recordId, status) void
}

class SecureStorageService {
    +saveAccessToken(token) void
    +saveRefreshToken(token) void
    +readAccessToken() String
    +readRefreshToken() String
    +clearTokens() void
}

class CaptureService {
    +captureImage() String
    +captureLocation() GeoPosition
    +createRecord() CapturedRecord
    +saveOffline(record) int
}

%% =====================================================
%% BACKEND SERVICES
%% =====================================================

class AuthenticationMiddleware {
    +validateToken(token) AuthenticatedUser
    +validateIssuer(token) bool
    +validateClient(token) bool
    +rejectUnauthorized() Response
}

class TenantAuthorizationService {
    +resolveTenant(user) int
    +authorizeResource(user, resource) bool
    +applyTenantFilter(query, tenantId) Query
}

class UserManagementService {
    +createUser(userData) User
    +updateUser(userId, userData) User
    +deleteUser(userId) void
    +resetPassword(userId) void
    +assignApplicationAccess(userId) void
}

class MasterDataService {
    +getCustomers(tenantId) List~Customer~
    +getLocations(tenantId) List~Location~
    +getCategories(tenantId) List~Category~
    +createCustomer(data) Customer
    +createLocation(data) Location
    +createCategory(data) Category
}

class CapturedRecordService {
    +createRecord(data) CapturedRecord
    +getRecords(tenantId) List~CapturedRecord~
    +getRecordById(recordId, tenantId) CapturedRecord
    +deleteRecord(recordId, tenantId) void
}

class ImageStorageService {
    +uploadImage(file) String
    +createSignedUrl(path) String
    +deleteImage(path) void
}

class EmailService {
    +sendPasswordSetupEmail(user) void
    +sendApplicationAccessEmail(user) void
}

class DatabaseRepository {
    +findUsers(tenantId) List~User~
    +findMasterData(tenantId) Object
    +findRecords(tenantId) List~CapturedRecord~
    +save(entity) Object
    +update(entity) Object
    +delete(entityId) void
}

%% =====================================================
%% DOMAIN RELATIONSHIPS
%% =====================================================

Tenant "1" *-- "0..*" User : contains
Tenant "1" *-- "0..*" Customer : owns
Tenant "1" *-- "0..*" Location : owns
Tenant "1" *-- "0..*" Category : owns
Tenant "1" *-- "0..*" CapturedRecord : owns

User "1" --> "0..*" CapturedRecord : captures
Customer "1" --> "0..*" CapturedRecord : associated with
Location "1" --> "0..*" CapturedRecord : captured at
Category "1" --> "0..*" CapturedRecord : classifies

User --> UserRole
CapturedRecord --> SyncStatus
AuthSession --> User

%% =====================================================
%% MOBILE SERVICE DEPENDENCIES
%% =====================================================

AuthService --> KeycloakMobileService : authenticates through
AuthService --> SecureStorageService : stores tokens
AuthService --> ApiService : retrieves user profile
AuthService --> AuthSession : creates

MasterDataSyncService --> ApiService : downloads data
MasterDataSyncService --> LocalDatabaseService : updates cache

RecordSyncService --> ApiService : uploads records
RecordSyncService --> LocalDatabaseService : reads and updates records

CaptureService --> LocalDatabaseService : saves records
CaptureService --> CapturedRecord : creates

LocalDatabaseService --> Customer : stores
LocalDatabaseService --> Location : stores
LocalDatabaseService --> Category : stores
LocalDatabaseService --> CapturedRecord : stores

%% =====================================================
%% BACKEND SERVICE DEPENDENCIES
%% =====================================================

AuthenticationMiddleware --> TenantAuthorizationService : provides identity
TenantAuthorizationService --> DatabaseRepository : applies tenant scope

UserManagementService --> DatabaseRepository : stores profiles
UserManagementService --> EmailService : sends invitations

MasterDataService --> DatabaseRepository : accesses master data
CapturedRecordService --> DatabaseRepository : accesses records
CapturedRecordService --> ImageStorageService : stores images

ApiService ..> AuthenticationMiddleware : bearer token request