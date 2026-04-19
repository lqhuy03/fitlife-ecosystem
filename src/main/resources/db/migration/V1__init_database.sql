-- =========================================================================
-- PROJECT FITLIFE - MASTER DATABASE SCHEMA
-- Version: V1 (21 Core Tables - Synchronized with ERD)
-- =========================================================================

-- 1. TABLE USERS
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       fit_coin INT DEFAULT 0,
                       reset_token VARCHAR(255),
                       reset_token_expiry DATETIME,
                       is_deleted BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. TABLE MEMBERS
CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT UNIQUE,
                         full_name VARCHAR(100) NOT NULL,
                         phone VARCHAR(20) UNIQUE NOT NULL,
                         email VARCHAR(100) UNIQUE,
                         avatar_url VARCHAR(255),
                         height DOUBLE,
                         weight DOUBLE,
                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                         is_deleted BOOLEAN DEFAULT FALSE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. TABLE GYM BRANCHES
CREATE TABLE gym_branches (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              name VARCHAR(100) NOT NULL,
                              address VARCHAR(255) NOT NULL,
                              max_capacity INT NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                              is_deleted BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. TABLE LOCKERS
CREATE TABLE lockers (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         gym_branch_id BIGINT NOT NULL,
                         locker_number VARCHAR(50) NOT NULL UNIQUE,
                         status VARCHAR(50) DEFAULT 'AVAILABLE',
                         CONSTRAINT fk_locker_branch FOREIGN KEY (gym_branch_id) REFERENCES gym_branches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. TABLE PACKAGES
CREATE TABLE packages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          duration_months INT NOT NULL,
                          price DECIMAL(12,2) NOT NULL,
                          description TEXT,
                          thumbnail_url VARCHAR(255),
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          is_deleted BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. TABLE SUBSCRIPTIONS
CREATE TABLE subscriptions (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               member_id BIGINT NOT NULL,
                               package_id BIGINT NOT NULL,
                               start_date DATE NULL,
                               end_date DATE NULL,
                               status VARCHAR(20) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_sub_member FOREIGN KEY (member_id) REFERENCES members(id),
                               CONSTRAINT fk_sub_package FOREIGN KEY (package_id) REFERENCES packages(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. TABLE PAYMENTS
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          subscription_id BIGINT NOT NULL,
                          amount DECIMAL(12,2) NOT NULL,
                          payment_date DATETIME NOT NULL,
                          payment_method VARCHAR(50),
                          vnp_transaction_no VARCHAR(255),
                          vnp_response_code VARCHAR(50),
                          vnp_order_info TEXT,
                          vnp_bank_code VARCHAR(50),
                          vnp_pay_date VARCHAR(50),
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payment_sub FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. TABLE HEALTH METRICS
CREATE TABLE health_metrics (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_id BIGINT NOT NULL,
                                weight DOUBLE NOT NULL,
                                height DOUBLE NOT NULL,
                                bmi DOUBLE,
                                recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_health_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. TABLE CHECK IN HISTORY
CREATE TABLE check_in_history (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  gym_branch_id BIGINT NOT NULL,
                                  locker_id BIGINT,
                                  check_in_time DATETIME NOT NULL,
                                  check_out_time DATETIME,
                                  status VARCHAR(30) NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_checkin_member FOREIGN KEY (member_id) REFERENCES members(id),
                                  CONSTRAINT fk_checkin_branch FOREIGN KEY (gym_branch_id) REFERENCES gym_branches(id),
                                  CONSTRAINT fk_checkin_locker FOREIGN KEY (locker_id) REFERENCES lockers(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. TABLE AI WORKOUT PLANS
CREATE TABLE ai_workout_plans (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  goal VARCHAR(255),
                                  plan_data JSON,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_ai_plan_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. TABLE WORKOUT PLANS
CREATE TABLE workout_plans (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               name VARCHAR(255) NOT NULL,
                               description TEXT,
                               member_id BIGINT NOT NULL,
                               start_date DATETIME,
                               end_date DATETIME,
                               status VARCHAR(50) DEFAULT 'ACTIVE',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_plan_member_exec FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. TABLE WORKOUT SESSIONS
CREATE TABLE workout_sessions (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  plan_id BIGINT NOT NULL,
                                  day_of_week VARCHAR(50),
                                  focus_area VARCHAR(255),
                                  CONSTRAINT fk_session_plan FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. TABLE WORKOUT DETAILS
CREATE TABLE workout_details (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 session_id BIGINT NOT NULL,
                                 exercise_name VARCHAR(255) NOT NULL,
                                 sets INT,
                                 reps VARCHAR(50),
                                 notes TEXT,
                                 is_completed BOOLEAN DEFAULT FALSE,
                                 is_customized BOOLEAN DEFAULT FALSE,
                                 CONSTRAINT fk_detail_session FOREIGN KEY (session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. TABLE WORKOUT LOGS
CREATE TABLE workout_logs (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NOT NULL,
                              exercise_name VARCHAR(100) NOT NULL,
                              sets INT NOT NULL,
                              reps INT NOT NULL,
                              calories_burned DOUBLE DEFAULT 0,
                              duration_minutes INT, -- Bổ sung từ ERD
                              workout_date DATE NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_workout_log_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- 15. E-COMMERCE & POS DOMAIN
-- =========================================================================
CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          category VARCHAR(100),
                          price DECIMAL(12,2) NOT NULL,
                          stock_quantity INT DEFAULT 0,
                          image_url VARCHAR(255),
                          description TEXT,
                          status VARCHAR(50) DEFAULT 'ACTIVE',
                          is_deleted BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        total_amount DECIMAL(12,2) NOT NULL,
                        payment_method VARCHAR(50),
                        status VARCHAR(50) DEFAULT 'COMPLETED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_order_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

-- =========================================================================
-- 16. NUTRITION & CHAT DOMAIN
-- =========================================================================
CREATE TABLE nutrition_plans (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 member_id BIGINT NOT NULL,
                                 ai_plan_id BIGINT,
                                 target_calories DOUBLE NOT NULL,
                                 protein_grams DOUBLE,
                                 carbs_grams DOUBLE,
                                 fat_grams DOUBLE,
                                 status VARCHAR(50) DEFAULT 'ACTIVE',
                                 start_date DATETIME,
                                 end_date DATETIME,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_nutrition_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_nutrition_ai_plan FOREIGN KEY (ai_plan_id) REFERENCES ai_workout_plans(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE meal_details (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              nutrition_plan_id BIGINT NOT NULL,
                              meal_name VARCHAR(100) NOT NULL,
                              food_items TEXT NOT NULL,
                              calories DOUBLE,
                              is_customized BOOLEAN DEFAULT FALSE,
                              CONSTRAINT fk_meal_nutrition FOREIGN KEY (nutrition_plan_id) REFERENCES nutrition_plans(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ai_chat_histories (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   member_id BIGINT NOT NULL,
                                   sender_role VARCHAR(20) NOT NULL, -- USER, AI
                                   message_content TEXT NOT NULL,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_chat_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================================
-- OPTIMIZE PERFORMANCE (INDEXING)
-- =========================================================================
CREATE INDEX idx_member_phone ON members(phone);
CREATE INDEX idx_member_email ON members(email);
CREATE INDEX idx_subscription_status ON subscriptions(status);
CREATE INDEX idx_check_in_time ON check_in_history(check_in_time);
CREATE INDEX idx_check_in_status ON check_in_history(status);
CREATE INDEX idx_workout_log_date ON workout_logs(workout_date);
CREATE INDEX idx_payments_vnp_txn ON payments(vnp_transaction_no);