package com.fitlife.workout.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workout_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    private WorkoutSession session;

    @Column(name = "exercise_name", nullable = false, length = 255)
    private String exerciseName;

    private Integer sets;

    @Column(length = 50)
    private String reps;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "is_customized")
    @Builder.Default
    private Boolean isCustomized = false;
}