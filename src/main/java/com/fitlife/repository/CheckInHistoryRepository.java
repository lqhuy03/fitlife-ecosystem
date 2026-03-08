package com.fitlife.repository;

import com.fitlife.entity.CheckInHistory;
import com.fitlife.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CheckInHistoryRepository extends JpaRepository<CheckInHistory, Long> {

    /**
     * Truy vấn kiểm tra xem hội viên ĐÃ VÀO CỬA THÀNH CÔNG trong ngày hôm nay chưa
     * Để tránh việc khách quẹt thẻ liên tục làm spam database
     */
    @Query("SELECT c FROM CheckInHistory c WHERE c.member = :member " +
            "AND c.checkInTime >= :startOfDay AND c.checkInTime <= :endOfDay " +
            "AND c.status = 'ACCESS_GRANTED'")
    Optional<CheckInHistory> findSuccessfulCheckInToday(
            @Param("member") Member member,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}