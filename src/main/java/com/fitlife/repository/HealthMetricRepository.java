package com.fitlife.repository;

import com.fitlife.entity.HealthMetric;
import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    // Truy xuất lịch sử sức khỏe của hội viên, sắp xếp ngày mới nhất lên đầu
    List<HealthMetric> findByMemberIdOrderByRecordedDateDesc(Long memberId);

    Optional<HealthMetric> findFirstByMemberOrderByRecordedDateDesc(Member member);

}
