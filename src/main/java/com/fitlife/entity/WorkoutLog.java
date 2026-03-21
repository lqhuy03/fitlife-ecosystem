package com.fitlife.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workout_logs")
public class WorkoutLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "exercise_name")
    private String exerciseName;

    private int sets;
    private int reps;

    @Column(name = "calories_burned")
    private double caloriesBurned;

    @Column(name = "workout_date")
    private LocalDate workoutDate;
}