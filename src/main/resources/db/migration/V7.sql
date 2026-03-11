-- Thêm cột chiều cao và cân nặng vào bảng members
ALTER TABLE members ADD COLUMN height DOUBLE DEFAULT NULL;
ALTER TABLE members ADD COLUMN weight DOUBLE DEFAULT NULL;