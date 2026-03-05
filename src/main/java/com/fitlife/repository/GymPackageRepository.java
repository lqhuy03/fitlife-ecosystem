package com.fitlife.repository;

import com.fitlife.entity.GymPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymPackageRepository extends JpaRepository<GymPackage, Long> {

    // Kiểm tra xem tên gói tập đã tồn tại chưa (tránh tạo 2 gói trùng tên)
    boolean existsByName(String name);
}