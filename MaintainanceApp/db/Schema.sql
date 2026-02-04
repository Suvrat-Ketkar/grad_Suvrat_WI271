DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    full_name TEXT NOT NULL,
	password_hash TEXT NOT NULL,
    role TEXT CHECK (role IN ('ADMIN', 'OWNER')) NOT NULL
);

DROP TABLE IF EXISTS site_types CASCADE;

CREATE TABLE site_types (
    site_type_id SERIAL PRIMARY KEY,
    type_name TEXT UNIQUE NOT NULL
);

INSERT INTO site_types (type_name) VALUES
('Villa'),
('Apartment'),
('Independent House'),
('Open Site');


DROP TABLE IF EXISTS site_sizes CASCADE;

CREATE TABLE site_sizes (
    size_id SERIAL PRIMARY KEY,
    length_ft INT NOT NULL,
    width_ft INT NOT NULL,
    UNIQUE (length_ft, width_ft)
);

INSERT INTO site_sizes (length_ft, width_ft) VALUES
(40, 60),
(30, 50),
(30, 40);

DROP TABLE IF EXISTS charge_rates CASCADE;

CREATE TABLE charge_rates (
    rate_id SERIAL PRIMARY KEY,
    site_status TEXT CHECK (site_status IN ('OPEN', 'OCCUPIED')) UNIQUE NOT NULL,
    rate_per_sqft NUMERIC(10,2) NOT NULL
);

INSERT INTO charge_rates (site_status, rate_per_sqft) VALUES
('OPEN', 6.00),
('OCCUPIED', 9.00);

DROP TABLE IF EXISTS sites CASCADE;

CREATE TABLE sites (
    site_id SERIAL PRIMARY KEY,
    site_number INT UNIQUE NOT NULL,
    site_type_id INT REFERENCES site_types(site_type_id),
    size_id INT REFERENCES site_sizes(size_id),
    site_status TEXT CHECK (site_status IN ('OPEN', 'OCCUPIED')) NOT NULL,
    owner_id INT REFERENCES users(user_id),
    is_active BOOLEAN DEFAULT TRUE
);

DROP TABLE IF EXISTS site_update_requests CASCADE;

CREATE TABLE site_update_requests (
    request_id SERIAL PRIMARY KEY,
    site_id INT REFERENCES sites(site_id),
    requested_by INT REFERENCES users(user_id),
    requested_site_type_id INT REFERENCES site_types(site_type_id),
    requested_size_id INT REFERENCES site_sizes(size_id),
    requested_status TEXT CHECK (requested_status IN ('OPEN', 'OCCUPIED')),
    status TEXT CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')) DEFAULT 'PENDING'
);

DROP TABLE IF EXISTS maintenance_dues CASCADE;

DROP TABLE IF EXISTS maintenance_dues CASCADE;

CREATE TABLE maintenance_dues (
    due_id SERIAL PRIMARY KEY,
    site_id INT NOT NULL REFERENCES sites(site_id),
    bill_year INT NOT NULL CHECK (bill_year >= 2000),
    total_amount NUMERIC(12,2) NOT NULL CHECK (total_amount > 0),
    status TEXT CHECK (status IN ('NOT_PAID', 'PARTIALLY_PAID', 'PAID')) DEFAULT 'NOT_PAID',
    UNIQUE (site_id, bill_year)
);


DROP TABLE IF EXISTS maintenance_payments CASCADE;

CREATE TABLE maintenance_payments (
    payment_id SERIAL PRIMARY KEY,
    due_id INT NOT NULL REFERENCES maintenance_dues(due_id),
    paid_amount NUMERIC(12,2) NOT NULL CHECK (paid_amount > 0),
    payment_date DATE DEFAULT CURRENT_DATE
);



INSERT INTO users (full_name, password_hash, role)
VALUES ('System Admin', 'admin123', 'ADMIN');


INSERT INTO users (full_name, password_hash, role) VALUES
('Owner 1', 'pass1', 'OWNER'),
('Owner 2', 'pass2', 'OWNER'),
('Owner 3', 'pass3', 'OWNER'),
('Owner 4', 'pass4', 'OWNER'),
('Owner 5', 'pass5', 'OWNER'),
('Owner 6', 'pass6', 'OWNER'),
('Owner 7', 'pass7', 'OWNER'),
('Owner 8', 'pass8', 'OWNER'),
('Owner 9', 'pass9', 'OWNER'),
('Owner 10', 'pass10', 'OWNER'),
('Owner 11', 'pass11', 'OWNER'),
('Owner 12', 'pass12', 'OWNER'),
('Owner 13', 'pass13', 'OWNER'),
('Owner 14', 'pass14', 'OWNER'),
('Owner 15', 'pass15', 'OWNER');