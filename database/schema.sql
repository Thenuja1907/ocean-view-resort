-- ============================================================
--  Ocean View Resort - Room Reservation System
--  Database Schema (MySQL 8.x)
--  CIS6003 Advanced Programming Assessment
-- ============================================================

CREATE DATABASE IF NOT EXISTS ocean_view_resort
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ocean_view_resort;

-- ──────────────────────────────────────
-- TABLE: users  (Staff / Admin Authentication)
-- ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id       INT          AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    role          ENUM('ADMIN','STAFF','RECEPTIONIST') NOT NULL DEFAULT 'STAFF',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login    DATETIME     NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email    (email)
) ENGINE=InnoDB;

-- ──────────────────────────────────────
-- TABLE: rooms
-- ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS rooms (
    room_id       INT            AUTO_INCREMENT PRIMARY KEY,
    room_number   VARCHAR(10)    NOT NULL UNIQUE,
    room_type     ENUM('STANDARD','DELUXE','SUITE','OCEAN_VIEW','PENTHOUSE') NOT NULL,
    floor_number  INT            NOT NULL DEFAULT 1,
    capacity      INT            NOT NULL DEFAULT 2,
    rate_per_night DECIMAL(10,2) NOT NULL,
    description   TEXT           NULL,
    amenities     TEXT           NULL,
    is_available  BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rooms_type      (room_type),
    INDEX idx_rooms_available (is_available)
) ENGINE=InnoDB;

-- ──────────────────────────────────────
-- TABLE: guests
-- ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS guests (
    guest_id       INT          AUTO_INCREMENT PRIMARY KEY,
    first_name     VARCHAR(75)  NOT NULL,
    last_name      VARCHAR(75)  NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NULL,   -- Added for Guest Portal
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE, -- Added for Guest Portal
    contact_number VARCHAR(20)  NOT NULL,
    address        TEXT         NOT NULL,
    id_type        ENUM('NIC','PASSPORT','DRIVING_LICENSE') NOT NULL DEFAULT 'NIC',
    id_number      VARCHAR(50)  NOT NULL,
    nationality    VARCHAR(100) NOT NULL DEFAULT 'Sri Lankan',
    last_login     DATETIME     NULL,   -- Added for Guest Portal
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_guests_email   (email),
    INDEX idx_guests_contact (contact_number)
) ENGINE=InnoDB;

-- ──────────────────────────────────────
-- TABLE: reservations
-- ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id     INT          AUTO_INCREMENT PRIMARY KEY,
    reservation_number VARCHAR(20)  NOT NULL UNIQUE,
    guest_id           INT          NOT NULL,
    room_id            INT          NOT NULL,
    check_in_date      DATE         NOT NULL,
    check_out_date     DATE         NOT NULL,
    num_guests         INT          NOT NULL DEFAULT 1,
    special_requests   TEXT         NULL,
    status             ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL DEFAULT 'PENDING',
    booked_by          INT          NOT NULL,   -- FK to users
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_guest   FOREIGN KEY (guest_id)  REFERENCES guests(guest_id) ON DELETE RESTRICT,
    CONSTRAINT fk_res_room    FOREIGN KEY (room_id)   REFERENCES rooms(room_id)   ON DELETE RESTRICT,
    CONSTRAINT fk_res_booker  FOREIGN KEY (booked_by) REFERENCES users(user_id)   ON DELETE RESTRICT,
    CONSTRAINT chk_dates      CHECK (check_out_date > check_in_date),
    INDEX idx_res_number    (reservation_number),
    INDEX idx_res_status    (status),
    INDEX idx_res_checkin   (check_in_date),
    INDEX idx_res_checkout  (check_out_date),
    INDEX idx_res_guest     (guest_id),
    INDEX idx_res_room      (room_id)
) ENGINE=InnoDB;

-- ──────────────────────────────────────
-- TABLE: bills
-- ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
    bill_id           INT            AUTO_INCREMENT PRIMARY KEY,
    bill_number       VARCHAR(20)    NOT NULL UNIQUE,
    reservation_id    INT            NOT NULL,
    num_nights        INT            NOT NULL,
    num_guests        INT            NOT NULL DEFAULT 1,
    room_rate         DECIMAL(10,2)  NOT NULL,
    room_charges      DECIMAL(10,2)  NOT NULL,
    tax_percentage    DECIMAL(5,2)   NOT NULL DEFAULT 10.00,
    tax_amount        DECIMAL(10,2)  NOT NULL,
    service_charge    DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    discount_amount   DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    total_amount      DECIMAL(10,2)  NOT NULL,
    payment_status    ENUM('PENDING','PARTIAL','PAID','REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_method    VARCHAR(50)    NULL,
    issued_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at           DATETIME       NULL,
    notes             TEXT           NULL,
    CONSTRAINT fk_bill_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT,
    INDEX idx_bills_number     (bill_number),
    INDEX idx_bills_res        (reservation_id),
    INDEX idx_bills_status     (payment_status)
) ENGINE=InnoDB;

-- ──────────────────────────────────────
-- TABLE: audit_log  (Observer pattern output)
-- ──────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_log (
    log_id      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   INT          NULL,
    old_value   TEXT         NULL,
    new_value   TEXT         NULL,
    ip_address  VARCHAR(45)  NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user   (user_id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_time   (created_at)
) ENGINE=InnoDB;

-- ============================================================
--  SEED DATA
-- ============================================================

-- Default admin user  (password = Admin@1234)
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
('admin',       '$2b$12$aiBaEvaegq2MU7heTLcrLO4BZ9cl4wDs4nvQcyldCbBBQKoRBUNGI.', 'System Administrator', 'admin@oceanviewresort.lk', 'ADMIN'),
('receptionist','$2b$12$aiBaEvaegq2MU7heTLcrLO4BZ9cl4wDs4nvQcyldCbBBQKoRBUNGI.', 'Front Desk Staff',      'desk@oceanviewresort.lk',  'RECEPTIONIST');

-- Room catalogue
INSERT INTO rooms (room_number, room_type, floor_number, capacity, rate_per_night, description, amenities) VALUES
('101', 'STANDARD',    1, 2,  8500.00, 'Cosy standard room with garden view',              'WiFi, AC, TV, Mini-bar'),
('102', 'STANDARD',    1, 2,  8500.00, 'Comfortable standard room',                        'WiFi, AC, TV, Mini-bar'),
('103', 'STANDARD',    1, 2,  8500.00, 'Standard room near the pool',                      'WiFi, AC, TV, Mini-bar'),
('201', 'DELUXE',      2, 2, 14000.00, 'Spacious deluxe room with partial ocean view',     'WiFi, AC, Smart TV, Mini-bar, Bathtub'),
('202', 'DELUXE',      2, 3, 14000.00, 'Deluxe room with extra bed option',               'WiFi, AC, Smart TV, Mini-bar, Bathtub'),
('203', 'DELUXE',      2, 2, 14000.00, 'Deluxe room with balcony',                        'WiFi, AC, Smart TV, Mini-bar, Bathtub, Balcony'),
('301', 'OCEAN_VIEW',  3, 2, 20000.00, 'Stunning panoramic ocean view room',              'WiFi, AC, Smart TV, Mini-bar, Jacuzzi, Balcony'),
('302', 'OCEAN_VIEW',  3, 3, 20000.00, 'Ocean view room with extra bed',                  'WiFi, AC, Smart TV, Mini-bar, Jacuzzi, Balcony'),
('401', 'SUITE',       4, 4, 35000.00, 'Luxurious suite with separate living area',       'WiFi, AC, Smart TV, Mini-bar, Jacuzzi, Kitchenette, Balcony'),
('402', 'SUITE',       4, 4, 35000.00, 'Presidential suite with butler service',          'WiFi, AC, Smart TV, Mini-bar, Jacuzzi, Full Kitchen, Balcony, Butler'),
('501', 'PENTHOUSE',   5, 6, 75000.00, 'Exclusive penthouse with private rooftop terrace','WiFi, AC, Smart TV, Full Bar, Private Pool, Butler, Private Chef');
