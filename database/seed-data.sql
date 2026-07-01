INSERT INTO users (username, password_hash, full_name, role, is_active)
VALUES
(
    'admin',
    'fieldsync-admin-salt:e848f884017adf21d798f2068333dc40ea45cf67f91b1e3588fe700a8456c9c0',
    'FieldSync Admin',
    'admin',
    TRUE
),
(
    'mobile',
    'fieldsync-mobile-salt:0c1adebcdc540b42d63d4a50716ef8d385f9b04c9860e14c6930bca52b1210ba',
    'Mobile Field User',
    'mobile_user',
    TRUE
);

INSERT INTO customers (name, phone, email, address)
VALUES
('ABC Traders', '0771234567', 'abc@example.com', 'Colombo'),
('Green Field Supplies', '0719876543', 'greenfield@example.com', 'Kandy'),
('Metro Retailers', '0765554444', 'metro@example.com', 'Galle');

INSERT INTO locations (name, address)
VALUES
('Colombo Warehouse', 'No. 10, Main Street, Colombo'),
('Kandy Branch', 'No. 25, Hill Road, Kandy'),
('Galle Distribution Center', 'No. 45, Fort Road, Galle');

INSERT INTO categories (name, description)
VALUES
('Inspection', 'General field inspection record'),
('Delivery Check', 'Delivery verification and condition check'),
('Maintenance', 'Maintenance or repair related record');