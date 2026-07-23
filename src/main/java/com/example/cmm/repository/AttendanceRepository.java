package com.example.cmm.repository;

import com.example.cmm.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMemberIdAndCheckInTimeBetween(Long memberId, LocalDateTime start, LocalDateTime end);
    long countByAttendanceDate(LocalDate attendanceDate);
    boolean existsByMemberIdAndAttendanceDate(Long memberId, LocalDate attendanceDate);
    Optional<Attendance> findTopByMemberIdAndAttendanceDate(Long memberId, LocalDate attendanceDate);
    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);
    List<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
    void deleteByMemberId(Long memberId);
}
