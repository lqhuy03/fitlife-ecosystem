package com.fitlife.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkoutResponse {

    private String planName;
    private String goal;
    private int estimatedDurationWeeks;
    private String fitnessLevel;

    // Danh sách các buổi tập trong tuần (VD: Push Day, Pull Day)
    private List<Session> sessions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Session {
        private String dayName; // VD: "Day 1: Push", "Monday"
        private String focusArea; // VD: "Chest, Shoulders, Triceps"

        private List<Exercise> exercises;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercise {
        private String exerciseName;
        private int sets;
        private String reps;
        private int restSeconds;
        private String notes;
    }
}
