```mermaid

flowchart TB
    ADMIN["Tenant Administrator"] -- Login credentials --> P1("1.0 Authenticate User")
    WEBUSER["Web Application User"] -- Login credentials --> P1
    MOBILEUSER["Mobile Field User"] -- Login credentials --> P1
    P1 -- Authentication request --> D1[(" Identity and Access Store")]
    D1 -- User identity, role and tenant details --> P1
    P1 -- Authenticated session --> ADMIN & WEBUSER
    P1 -- Access and refresh tokens --> MOBILEUSER
    ADMIN -- User details and access permissions --> P2("2.0 Manage Tenant Users")
    P2 -- Create or update identity --> D1
    P2 -- Create or update tenant user profile --> D2[(" Tenant and User Store")]
    P2 -- Invitation and password setup request --> EMAIL["Brevo Email Service"]
    EMAIL -- Password setup and application access email --> NEWUSER["Newly Registered User"]
    ADMIN -- Customer, location and category details --> P3("3.0 Manage Master Data")
    WEBUSER -- Authorized master data details --> P3
    P3 -- Create, update or retrieve master data --> D3[("              Master Data Store")]
    D3 -- "Tenant-specific master data" --> P3
    P3 -- Master data confirmation --> ADMIN
    P3 -- Master data response --> WEBUSER
    MOBILEUSER -- Text, image, GPS and selected master data --> P4("4.0 Capture Field Record")
    P4 -- Pending field record --> D6[(" Mobile Local Data Store")]
    D6 -- Saved local record --> P4
    P4 -- Capture confirmation --> MOBILEUSER
    MOBILEUSER -- Start synchronization --> P5("5.0 Synchronize Mobile Data")
    D6 -- Pending records and local references --> P5
    D3 -- "Tenant-specific customers, locations and categories" --> P5
    P5 -- Captured record data --> D4[(" Captured Record Store")]
    P5 -- Captured image --> D5[(" Captured Image Store")]
    D4 -- Record creation result --> P5
    D5 -- Stored image reference --> P5
    P5 -- Updated master data and synchronization status --> D6
    P5 -- Synchronization result --> MOBILEUSER
    ADMIN -- Record query or management request --> P6("6.0 View and Manage Captured Records")
    WEBUSER -- Authorized record query --> P6
    P6 -- "Tenant-filtered record query" --> D4
    D4 -- Captured record details --> P6
    P6 -- Image request --> D5
    D5 -- Captured image or image reference --> P6
    P6 -- Dashboard and record details --> ADMIN
    P6 -- Authorized record details --> WEBUSER

     ADMIN:::entity
     WEBUSER:::entity
     MOBILEUSER:::entity
     NEWUSER:::entity
     P1:::process
     P2:::process
     P3:::process
     P4:::process
     P5:::process
     P6:::process
     D1:::store
     D2:::store
     D3:::store
     D4:::store
     D5:::store
     D6:::store
     EMAIL:::external
    classDef entity fill:#FFF4F6,stroke:#EB5979,stroke-width:2px,color:#111827
    classDef process fill:#EFF6FF,stroke:#2563EB,stroke-width:2px,color:#111827
    classDef store fill:#F0FDF4,stroke:#16A34A,stroke-width:2px,color:#111827
    classDef external fill:#FFF7ED,stroke:#EA580C,stroke-width:2px,color:#111827