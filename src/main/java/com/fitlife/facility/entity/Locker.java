package com.fitlife.facility.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lockers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Locker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "locker_number", nullable = false, unique = true)
    private String lockerNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LockerStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    // FIX: Sửa thành gym_branch_id cho chuẩn Convention
    @JoinColumn(name = "gym_branch_id", nullable = false)
    private GymBranch branch;

    // Đưa Enum vào trong Class thành public (hoặc tách file riêng)
    public enum LockerStatus {
        AVAILABLE, OCCUPIED, MAINTENANCE
    }
}