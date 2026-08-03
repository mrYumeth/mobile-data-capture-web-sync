```mermaid

flowchart LR

    %% =====================================================
    %% USER DEVICES
    %% =====================================================
    subgraph DEVICES["User Devices"]
        direction TB

        subgraph MOBILE_DEVICE["Android Mobile Device"]
            FLUTTER["FieldSync Flutter App"]
            SQLITE[("SQLite Local Database")]
            CAMERA["Device Camera"]
            GPS["GPS Service"]
            SECURE["Secure Token Storage"]

            FLUTTER --> SQLITE
            FLUTTER --> CAMERA
            FLUTTER --> GPS
            FLUTTER --> SECURE
        end

        BROWSER["Web Browser"]
    end

    %% =====================================================
    %% RENDER CLOUD
    %% =====================================================
    subgraph RENDER["Render Cloud Platform"]
        direction TB

        WEB["FieldSync React Web App<br/>Static Site"]

        API["FieldSync Backend API<br/>Node.js and Express"]

        KEYCLOAK["Keycloak IAM Service<br/>Docker Web Service"]

        KCDB[("Keycloak PostgreSQL Database")]
    end

    %% =====================================================
    %% SUPABASE
    %% =====================================================
    subgraph SUPABASE["Supabase Platform"]
        direction TB

        APPDB[("FieldSync PostgreSQL Database")]
        STORAGE[("Captured Image Storage")]
    end

    %% =====================================================
    %% EMAIL SERVICE
    %% =====================================================
    subgraph BREVO_CLOUD["Brevo Cloud"]
        SMTP["Transactional Email Service<br/>SMTP Port 2525"]
    end

    %% =====================================================
    %% DEPLOYMENT CONNECTIONS
    %% =====================================================

    BROWSER -->|"HTTPS"| WEB
    WEB -->|"HTTPS REST API"| API
    WEB -->|"OpenID Connect Login"| KEYCLOAK

    FLUTTER -->|"HTTPS REST API and Sync"| API
    FLUTTER -->|"OpenID Connect Login"| KEYCLOAK

    API -->|"SQL over TLS"| APPDB
    API -->|"Image Upload and Retrieval"| STORAGE
    API -->|"User Provisioning"| KEYCLOAK
    API -->|"Application Access Email"| SMTP

    KEYCLOAK -->|"Identity Data"| KCDB
    KEYCLOAK -->|"Password Setup Email"| SMTP

    %% =====================================================
    %% STYLING
    %% =====================================================
    classDef device fill:#F8FAFC,stroke:#64748B,stroke-width:1.5px,color:#111827;
    classDef render fill:#EFF6FF,stroke:#2563EB,stroke-width:1.5px,color:#111827;
    classDef database fill:#F0FDF4,stroke:#16A34A,stroke-width:1.5px,color:#111827;
    classDef external fill:#FFF7ED,stroke:#EA580C,stroke-width:1.5px,color:#111827;
    classDef security fill:#EEF2FF,stroke:#6366F1,stroke-width:1.5px,color:#111827;

    class FLUTTER,BROWSER,CAMERA,GPS,SECURE device;
    class WEB,API render;
    class KEYCLOAK security;
    class SQLITE,KCDB,APPDB,STORAGE database;
    class SMTP external;

    
