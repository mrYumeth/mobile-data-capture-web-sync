```mermaid

flowchart TB
 subgraph USERS["Tenant Users"]
    direction LR
        TA_ADMIN["Tenant A<br>Administrator"]
        TA_USER["Tenant A<br>User"]
        TB_ADMIN["Tenant B<br>Administrator"]
        TB_USER["Tenant B<br>User"]
  end
 subgraph CLIENTS["FieldSync Client Applications"]
    direction LR
        MOBILE["Flutter Mobile Application"]
        WEB["React Web Application"]
  end
 subgraph IAM["Identity and Access Management"]
    direction LR
        LOGIN["OpenID Connect<br>Login Request"]
        KEYCLOAK["Keycloak<br>Identity Provider"]
        TOKEN["Signed JWT Access Token"]
  end
 subgraph CLAIMS["Security Information in the Authenticated Session"]
    direction LR
        USER_ID["User Identity"]
        ROLE["Application Role<br>Admin or User"]
        TENANT_ID["Tenant Identity<br>Tenant A or Tenant B"]
        CLIENT_ACCESS["Client Access<br>Web or Mobile"]
  end
 subgraph BACKEND["Backend Security Pipeline"]
    direction LR
        API["Node.js and Express<br>REST API"]
        AUTH["JWT Authentication<br>Middleware"]
        ACCESS["Client and Role<br>Authorization"]
        TENANT_CONTEXT["Resolve Tenant Context"]
        TENANT_FILTER["Apply Tenant Filter<br>to Every Data Operation"]
  end
 subgraph SERVICES["Tenant-Aware Application Services"]
    direction LR
        USER_SERVICE["User Management"]
        MASTER_SERVICE["Customer, Location<br>and Category Management"]
        RECORD_SERVICE["Captured Record<br>Management"]
        SYNC_SERVICE["Mobile Synchronization"]
  end
 subgraph DATA_ACCESS["Tenant-Aware Data Access"]
    direction LR
        ORM["Prisma ORM / SQL Queries"]
        DATA_CONTROL["Tenant Filtering and<br>Database Access Controls"]
  end
 subgraph DATABASE["FieldSync PostgreSQL Database"]
    direction LR
        TENANT_A[("Tenant A Data<br><br>Users<br>Customers<br>Locations<br>Categories<br>Captured Records")]
        TENANT_B[("Tenant B Data<br><br>Users<br>Customers<br>Locations<br>Categories<br>Captured Records")]
  end
 subgraph LOCAL["Mobile Device Security"]
    direction LR
        SECURE_STORAGE["Secure Token Storage"]
        LOCAL_CACHE[("SQLite Local Cache")]
        AUTO_SYNC["Tenant-Specific<br>Automatic Master Data Sync"]
  end
    LOGIN --> KEYCLOAK
    KEYCLOAK --> TOKEN
    API --> AUTH
    AUTH --> ACCESS
    ACCESS --> TENANT_CONTEXT
    TENANT_CONTEXT --> TENANT_FILTER
    ORM --> DATA_CONTROL
    SECURE_STORAGE --> AUTO_SYNC
    AUTO_SYNC --> LOCAL_CACHE
    TA_USER --> MOBILE
    MOBILE -- Authenticate --> LOGIN
    TOKEN --> CLIENT_ACCESS
    TOKEN -- Bearer token --> API
    TOKEN -- Stored on device --> SECURE_STORAGE
    USER_ID --> AUTH
    TENANT_FILTER --> SYNC_SERVICE & ISOLATION@{ label: "Tenant Isolation Rule:<br>A request may access only records associated<br>with the authenticated user's tenant identity." }
    RECORD_SERVICE --> ORM
    DATA_CONTROL -- "tenant_id = Tenant A" --> TENANT_A
    DATA_CONTROL -- "tenant_id = Tenant B" --> TENANT_B
    SYNC_SERVICE -- Authenticated tenant master data --> AUTO_SYNC
    AUTH -. Invalid or expired token .-> DENIED["Request Rejected<br>401 Unauthenticated or<br>403 Unauthorized"]
    ACCESS -. Wrong role or client access .-> DENIED
    TENANT_FILTER -. Resource belongs to another tenant .-> DENIED
    TA_ADMIN ~~~ TA_USER
    TB_ADMIN ~~~ TB_USER
    TENANT_A ~~~ TENANT_B

    ISOLATION@{ shape: rect}
     TA_ADMIN:::tenantA
     TA_USER:::tenantA
     TB_ADMIN:::tenantB
     TB_USER:::tenantB
     MOBILE:::client
     WEB:::client
     LOGIN:::security
     KEYCLOAK:::security
     TOKEN:::security
     USER_ID:::claim
     ROLE:::claim
     TENANT_ID:::claim
     CLIENT_ACCESS:::claim
     API:::security
     AUTH:::security
     ACCESS:::security
     TENANT_CONTEXT:::security
     TENANT_FILTER:::security
     USER_SERVICE:::service
     MASTER_SERVICE:::service
     RECORD_SERVICE:::service
     SYNC_SERVICE:::service
     ORM:::service
     DATA_CONTROL:::service
     TENANT_A:::tenantA
     TENANT_B:::tenantB
     SECURE_STORAGE:::security
     LOCAL_CACHE:::data
     AUTO_SYNC:::security
     ISOLATION:::rule
     DENIED:::warning
    classDef tenantA fill:#EEF2FF,stroke:#4F46E5,stroke-width:2px,color:#111827
    classDef tenantB fill:#FFF7ED,stroke:#EA580C,stroke-width:2px,color:#111827
    classDef client fill:#F8FAFC,stroke:#64748B,stroke-width:1.5px,color:#111827
    classDef security fill:#EFF6FF,stroke:#2563EB,stroke-width:2px,color:#111827
    classDef claim fill:#F5F3FF,stroke:#7C3AED,stroke-width:1.5px,color:#111827
    classDef service fill:#ECFEFF,stroke:#0891B2,stroke-width:1.5px,color:#111827
    classDef data fill:#F0FDF4,stroke:#16A34A,stroke-width:2px,color:#111827
    classDef warning fill:#FEF2F2,stroke:#DC2626,stroke-width:2px,color:#111827
    classDef rule fill:#FFFBEB,stroke:#D97706,stroke-width:2px,color:#111827