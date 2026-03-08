package com.fitlife.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Đại diện cho một buổi tập trong ngày (ví dụ: Thứ 2 tập Ngực)
 */
@Entity
@Table(name = "workout_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayOfWeek; // Monday, Tuesday...
    private String focusArea; // Push, Triceps...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonBackReference
    @EqualsAndHashCode.Exclude // THÊM DÒNG NÀY
    @ToString.Exclude
    private WorkoutPlan workoutPlan;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    @EqualsAndHashCode.Exclude // THÊM DÒNG NÀY
    @ToString.Exclude
    private Set<WorkoutDetail> details = new LinkedHashSet<>();
}