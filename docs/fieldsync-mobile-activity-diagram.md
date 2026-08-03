```mermaid

flowchart TB

    %% =====================================================
    %% START
    %% =====================================================
    START((Start))

    %% =====================================================
    %% AUTHENTICATION
    %% =====================================================
    subgraph AUTH["Authentication"]
        direction TB

        OPEN["Open FieldSync Mobile App"]
        LOGIN["Select Login"]
        KC["Authenticate through Keycloak"]
        AUTH_OK{"Authentication successful?"}
        LOGIN_ERROR["Display login error"]
        STORE["Store access and refresh tokens securely"]
    end

    %% =====================================================
    %% TENANT INITIALIZATION
    %% =====================================================
    subgraph INITIALIZE["Tenant Data Initialization"]
        direction TB

        AUTO_SYNC["Automatically request tenant master data"]
        ONLINE_LOGIN{"Network available?"}
        DOWNLOAD["Download tenant-specific customers,<br/>locations and categories"]
        REPLACE["Replace local master-data cache"]
        LOAD_FAIL["Display tenant-data loading error"]
        HOME["Open Mobile Home Screen"]
    end

    %% =====================================================
    %% FIELD DATA CAPTURE
    %% =====================================================
    subgraph CAPTURE["Field Record Capture"]
        direction TB

        NEW_RECORD["Select New Field Record"]
        LOAD_MASTER["Load customers, locations and categories"]
        ENTER["Enter record details"]
        SELECT["Select customer, location and category"]
        GPS["Capture GPS coordinates"]
        IMAGE_DECISION{"Capture an image?"}
        CAMERA["Open device camera"]
        SAVE_IMAGE["Save image path locally"]
        VALIDATE{"Required details valid?"}
        VALIDATION_ERROR["Display validation message"]
        SAVE_LOCAL["Save record to SQLite"]
        MARK_PENDING["Set synchronization status to Pending"]
        CAPTURED["Display record-saved confirmation"]
    end

    %% =====================================================
    %% SYNCHRONIZATION
    %% =====================================================
    subgraph SYNC["Mobile Synchronization"]
        direction TB

        SYNC_ACTION["Select Synchronize"]
        NETWORK{"Network available?"}
        OFFLINE["Keep records pending<br/>and display network message"]
        GET_PENDING["Retrieve pending local records"]
        HAS_PENDING{"Pending records available?"}
        REFRESH_ONLY["Refresh tenant master data"]
        NEXT["Select next pending record"]
        HAS_IMAGE{"Record contains an image?"}
        UPLOAD_IMAGE["Upload image to cloud storage"]
        IMAGE_OK{"Image upload successful?"}
        IMAGE_FAIL["Keep record pending<br/>and record failure"]
        UPLOAD_RECORD["Send record data and image reference<br/>to backend"]
        RECORD_OK{"Record accepted?"}
        MARK_SYNCED["Set local status to Synced"]
        RECORD_FAIL["Keep record pending<br/>and record failure"]
        MORE{"More pending records?"}
        REFRESH["Download latest tenant master data"]
        UPDATE_CACHE["Update local master-data cache"]
        SUMMARY["Display synchronization summary"]
    end

    %% =====================================================
    %% LOGOUT
    %% =====================================================
    subgraph SESSION["Session Completion"]
        direction TB

        CONTINUE{"Continue using the app?"}
        LOGOUT["Select Logout"]
        CLEAR["Clear authentication session"]
        END_NODE((End))
    end

    %% =====================================================
    %% MAIN FLOW
    %% =====================================================
    START --> OPEN
    OPEN --> LOGIN
    LOGIN --> KC
    KC --> AUTH_OK

    AUTH_OK -->|"No"| LOGIN_ERROR
    LOGIN_ERROR --> LOGIN

    AUTH_OK -->|"Yes"| STORE
    STORE --> AUTO_SYNC
    AUTO_SYNC --> ONLINE_LOGIN

    ONLINE_LOGIN -->|"No"| LOAD_FAIL
    LOAD_FAIL --> LOGIN

    ONLINE_LOGIN -->|"Yes"| DOWNLOAD
    DOWNLOAD --> REPLACE
    REPLACE --> HOME

    %% =====================================================
    %% CAPTURE FLOW
    %% =====================================================
    HOME --> NEW_RECORD
    NEW_RECORD --> LOAD_MASTER
    LOAD_MASTER --> ENTER
    ENTER --> SELECT
    SELECT --> GPS
    GPS --> IMAGE_DECISION

    IMAGE_DECISION -->|"Yes"| CAMERA
    CAMERA --> SAVE_IMAGE
    SAVE_IMAGE --> VALIDATE

    IMAGE_DECISION -->|"No"| VALIDATE

    VALIDATE -->|"No"| VALIDATION_ERROR
    VALIDATION_ERROR --> ENTER

    VALIDATE -->|"Yes"| SAVE_LOCAL
    SAVE_LOCAL --> MARK_PENDING
    MARK_PENDING --> CAPTURED
    CAPTURED --> HOME

    %% =====================================================
    %% SYNCHRONIZATION FLOW
    %% =====================================================
    HOME --> SYNC_ACTION
    SYNC_ACTION --> NETWORK

    NETWORK -->|"No"| OFFLINE
    OFFLINE --> HOME

    NETWORK -->|"Yes"| GET_PENDING
    GET_PENDING --> HAS_PENDING

    HAS_PENDING -->|"No"| REFRESH_ONLY
    REFRESH_ONLY --> UPDATE_CACHE
    UPDATE_CACHE --> SUMMARY

    HAS_PENDING -->|"Yes"| NEXT
    NEXT --> HAS_IMAGE

    HAS_IMAGE -->|"Yes"| UPLOAD_IMAGE
    UPLOAD_IMAGE --> IMAGE_OK

    IMAGE_OK -->|"No"| IMAGE_FAIL
    IMAGE_FAIL --> MORE

    IMAGE_OK -->|"Yes"| UPLOAD_RECORD

    HAS_IMAGE -->|"No"| UPLOAD_RECORD

    UPLOAD_RECORD --> RECORD_OK

    RECORD_OK -->|"Yes"| MARK_SYNCED
    MARK_SYNCED --> MORE

    RECORD_OK -->|"No"| RECORD_FAIL
    RECORD_FAIL --> MORE

    MORE -->|"Yes"| NEXT
    MORE -->|"No"| REFRESH

    REFRESH --> UPDATE_CACHE
    SUMMARY --> HOME

    %% =====================================================
    %% LOGOUT FLOW
    %% =====================================================
    HOME --> CONTINUE
    CONTINUE -->|"Yes"| HOME
    CONTINUE -->|"No"| LOGOUT
    LOGOUT --> CLEAR
    CLEAR --> END_NODE

    %% =====================================================
    %% STYLING
    %% =====================================================
    classDef action fill:#EFF6FF,stroke:#2563EB,stroke-width:1.5px,color:#111827;
    classDef decision fill:#FFFBEB,stroke:#D97706,stroke-width:2px,color:#111827;
    classDef error fill:#FEF2F2,stroke:#DC2626,stroke-width:1.5px,color:#111827;
    classDef success fill:#F0FDF4,stroke:#16A34A,stroke-width:1.5px,color:#111827;
    classDef terminal fill:#F5F3FF,stroke:#7C3AED,stroke-width:2px,color:#111827;

    class OPEN,LOGIN,KC,STORE,AUTO_SYNC,DOWNLOAD,REPLACE,HOME,NEW_RECORD,LOAD_MASTER,ENTER,SELECT,GPS,CAMERA,SAVE_IMAGE,SAVE_LOCAL,MARK_PENDING,SYNC_ACTION,GET_PENDING,REFRESH_ONLY,NEXT,UPLOAD_IMAGE,UPLOAD_RECORD,MARK_SYNCED,REFRESH,UPDATE_CACHE,LOGOUT,CLEAR action;

    class AUTH_OK,ONLINE_LOGIN,IMAGE_DECISION,VALIDATE,NETWORK,HAS_PENDING,HAS_IMAGE,IMAGE_OK,RECORD_OK,MORE,CONTINUE decision;

    class LOGIN_ERROR,LOAD_FAIL,VALIDATION_ERROR,OFFLINE,IMAGE_FAIL,RECORD_FAIL error;

    class CAPTURED,SUMMARY success;

    class START,END_NODE terminal;            