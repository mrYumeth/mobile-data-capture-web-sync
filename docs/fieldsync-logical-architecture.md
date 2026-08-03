```mermaid

flowchart TB
 subgraph USERS["1. User Layer"]
    direction TB
        MU["Mobile Field User"]
        WU["Web Application User"]
        AD["Tenant Administrator"]
  end
 subgraph MOBILE["Flutter Mobile Application"]
    direction TB
        MFA["Mobile User Interface"]
        CAP["Data Capture Module"]
        GPS["GPS Location Service"]
        CAM["Camera and Image Capture"]
        OFFLINE["Offline Record Manager"]
        MASTER_CACHE["Master Data Cache"]
        SYNC_CLIENT["Mobile Synchronization Client"]
        SECURE["Secure Token Storage"]
  end
 subgraph WEB["React Web Application"]
    direction TB
        WUI["Web User Interface"]
        DASH["Dashboard and Record Management"]
        MASTER_UI["Master Data Management"]
        USER_UI["User Administration"]
  end
 subgraph CLIENTS["2. Client Application Layer"]
    direction TB
        MOBILE
        WEB
  end
 subgraph IAM["3. Identity and Access Management"]
    direction TB
        OIDC["OpenID Connect Authentication"]
        KC["Keycloak Identity Provider"]
        JWT["JWT Access Token"]
        ROLE["Roles and Tenant Identity"]
  end
 subgraph SERVICES["Business Services"]
    direction LR
        MASTER_SVC["Master Data Service"]
        RECORD_SVC["Captured Record Service"]
        SYNC_SVC["Synchronization Service"]
        USER_SVC["User Management Service"]
  end
 subgraph API["4. Backend Application Layer"]
    direction TB
        EXPRESS["Node.js and Express REST API"]
        AUTH["Authentication Middleware"]
        TENANT["Tenant Authorization and Isolation"]
        SERVICES
        IMAGE_SVC["Image Upload Service"]
        EMAIL_SVC["Invitation Email Service"]
  end
 subgraph DATA_ACCESS["5. Data Access and Security Layer"]
    direction TB
        PRISMA["Prisma ORM"]
        RLS["Tenant Filtering and Row Level Security"]
  end
 subgraph DATA["6. Data Storage Layer"]
    direction TB
        SQLITE[("SQLite Local Database")]
        POSTGRES[("PostgreSQL Database - Supabase")]
        STORAGE[("Supabase Storage")]
  end
 subgraph EXTERNAL["7. External Services"]
    direction TB
        BREVO["Brevo SMTP Service"]
  end
    MFA --> CAP
    CAP --> GPS & CAM & OFFLINE
    OFFLINE --> SYNC_CLIENT
    MASTER_CACHE --> SYNC_CLIENT
    SECURE --> SYNC_CLIENT
    WUI --> DASH & MASTER_UI & USER_UI
    OIDC --> KC
    KC --> JWT
    JWT --> ROLE
    EXPRESS --> AUTH
    AUTH --> TENANT
    TENANT --> MASTER_SVC
    RECORD_SVC --> IMAGE_SVC & PRISMA
    USER_SVC --> EMAIL_SVC
    PRISMA --> RLS
    MU --> MFA
    MFA -- Login --> OIDC
    JWT -- Stored securely --> SECURE
    JWT -- Bearer token --> AUTH
    MASTER_UI -- HTTPS REST API --> EXPRESS
    OFFLINE -- Save pending records --> SQLITE
    MASTER_CACHE -- Cache master data --> SQLITE
    SQLITE -- Read pending and cached data --> SYNC_CLIENT
    RLS --> POSTGRES
    IMAGE_SVC --> STORAGE
    EMAIL_SVC --> BREVO
    MASTER_SVC -- Customers, Locations and Categories --> SYNC_CLIENT
    SYNC_CLIENT -- Pending Records and Images --> SYNC_SVC

     MU:::user
     WU:::user
     AD:::user
     MFA:::client
     CAP:::client
     GPS:::client
     CAM:::client
     OFFLINE:::client
     MASTER_CACHE:::client
     SYNC_CLIENT:::client
     SECURE:::client
     WUI:::client
     DASH:::client
     MASTER_UI:::client
     USER_UI:::client
     OIDC:::auth
     KC:::auth
     JWT:::auth
     ROLE:::auth
     MASTER_SVC:::service
     RECORD_SVC:::service
     SYNC_SVC:::service
     USER_SVC:::service
     EXPRESS:::service
     AUTH:::auth
     TENANT:::auth
     IMAGE_SVC:::service
     EMAIL_SVC:::service
     PRISMA:::service
     RLS:::service
     SQLITE:::data
     POSTGRES:::data
     STORAGE:::data
     BREVO:::external
    classDef user fill:#FFF4F6,stroke:#EB5979,stroke-width:2px,color:#111827
    classDef client fill:#F8FAFC,stroke:#64748B,stroke-width:1.5px,color:#111827
    classDef auth fill:#EEF2FF,stroke:#6366F1,stroke-width:1.5px,color:#111827
    classDef service fill:#EFF6FF,stroke:#2563EB,stroke-width:1.5px,color:#111827
    classDef data fill:#F0FDF4,stroke:#16A34A,stroke-width:1.5px,color:#111827
    classDef external fill:#FFF7ED,stroke:#EA580C,stroke-width:1.5px,color:#111827