package com.fitlife.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraph(
        name = "WorkoutPlan.fullGraph",
        attributeNodes = {
                @NamedAttributeNode(value = "sessions", subgraph = "sessions-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "sessions-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("details")
                        }
                )
        }
)
@Entity
@Table(name = "workout_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @EqualsAndHashCode.Exclude // Thêm dòng này
    @ToString.Exclude
    private Member member;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private PlanStatus status; // ACTIVE, COMPLETED, CANCELLED

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    @EqualsAndHashCode.Exclude // Thêm dòng này
    @ToString.Exclude
    private Set<WorkoutSession> sessions = new LinkedHashSet<>();

    public enum PlanStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}