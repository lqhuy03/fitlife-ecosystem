package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Chi tiết từng bài tập trong một buổi tập
 */
@Entity
@Table(name = "workout_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String exerciseName;
    private Integer sets;
    private String reps;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @ToString.Exclude
    private WorkoutSession session;
}