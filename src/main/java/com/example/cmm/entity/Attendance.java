package com.example.cmm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id")
    private ServiceType serviceType;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    @Column(length = 20)
    private String status;

    public Attendance() {}

    public Attendance(Member member, LocalDateTime checkInTime) {
        this.member = member;
        this.checkInTime = checkInTime;
        this.attendanceDate = checkInTime.toLocalDate();
        this.status = "PRESENT";
    }

    public Attendance(LocalDateTime checkInTime, String status) {
        this.member = null;
        this.checkInTime = checkInTime;
        this.attendanceDate = checkInTime.toLocalDate();
        this.status = status != null ? status : "VISITOR";
    }

    public Attendance(Member member, ServiceType serviceType, LocalDateTime checkInTime, String status) {
        this.member = member;
        this.serviceType = serviceType;
        this.checkInTime = checkInTime;
        this.attendanceDate = checkInTime.toLocalDate();
        this.status = status != null ? status : "PRESENT";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
        if (checkInTime != null) {
            this.attendanceDate = checkInTime.toLocalDate();
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
