package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packages") // Tên class là GymPackage, nhưng DB vẫn là bảng packages
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name; // Ví dụ: "Gói Vàng 6 Tháng"

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths; // Số tháng của gói

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // "ACTIVE" hoặc "INACTIVE"
}