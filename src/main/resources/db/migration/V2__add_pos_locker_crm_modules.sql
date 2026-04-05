-- =========================================================================
-- FITLIFE DB MIGRATION V2 - ADD POS, SMART LOCKER AND CRM
-- =========================================================================

-- 1. TABLE PRODUCTS (POS & Inventory)
CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          category VARCHAR(100),
                          price DECIMAL(12,2) NOT NULL,
                          stock_quantity INT NOT NULL DEFAULT 0,
                          image_url VARCHAR(255),
                          status VARCHAR(50) DEFAULT 'ACTIVE',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. TABLE ORDERS (POS)
CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        total_amount DECIMAL(12,2) NOT NULL,
                        payment_method VARCHAR(50), -- EX: 'VNPAY', 'CASH'
                        status VARCHAR(50) DEFAULT 'COMPLETED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_order_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. TABLE ORDER DETAILS (POS)
CREATE TABLE order_details (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               order_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               quantity INT NOT NULL,
                               price_at_time DECIMAL(12,2) NOT NULL,
                               subtotal DECIMAL(12,2) NOT NULL,
                               CONSTRAINT fk_detail_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                               CONSTRAINT fk_detail_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. TABLE LOCKERS (Smart Locker)
CREATE TABLE lockers (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         gym_branch_id BIGINT NOT NULL,
                         locker_number VARCHAR(20) NOT NULL,
                         status VARCHAR(50) DEFAULT 'AVAILABLE', -- Status: AVAILABLE, IN_USE, MAINTENANCE
                         CONSTRAINT fk_locker_branch FOREIGN KEY (gym_branch_id) REFERENCES gym_branches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE check_in_history
    ADD COLUMN locker_id BIGINT NULL,
ADD CONSTRAINT fk_checkin_locker FOREIGN KEY (locker_id) REFERENCES lockers(id) ON DELETE SET NULL;

-- 5. TABLE NOTIFICATIONS (CRM / Auto-Notification)
CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               type VARCHAR(50), -- Types: REMINDER, PROMOTION, SYSTEM
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_notification_user_read ON notifications(user_id, is_read);