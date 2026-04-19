package com.fitlife.nutrition.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_plan_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private NutritionPlan nutritionPlan;

    @Column(name = "meal_name", nullable = false, length = 100)
    private String mealName; // VD: Sáng, Trưa, Tối, Bữa phụ

    @Column(name = "food_items", nullable = false, columnDefinition = "TEXT")
    private String foodItems; // Danh sách món ăn (có thể lưu chuỗi JSON hoặc text)

    private Double calories;

    @Column(name = "is_customized")
    @Builder.Default
    private Boolean isCustomized = false;
}