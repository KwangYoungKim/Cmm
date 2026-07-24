package com.example.cmm.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String faceDescriptor;

    @Column(length = 50)
    private String department; // 소속 부서 (예: 청년부, 1교구)

    @Column(length = 30)
    private String position; // 직분 (예: 집사, 권사, 장로, 교우)

    @Column(length = 20)
    private String phone; // 연락처

    @Column(length = 10)
    private String gender; // 성별 (MALE, FEMALE)

    @Column(length = 10)
    private String birthDate; // 생년월일 (YYYY-MM-DD)

    @Column(columnDefinition = "TEXT")
    private String profileImage; // Base64 섬네일 이미지 데이터

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Member() {}

    public Member(String name, String faceDescriptor) {
        this.name = name;
        this.faceDescriptor = faceDescriptor;
    }

    public Member(String name, String faceDescriptor, String department, String position, String phone, String gender, String birthDate) {
        this.name = name;
        this.faceDescriptor = faceDescriptor;
        this.department = department;
        this.position = position;
        this.phone = phone;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        if (phone != null) {
            this.phone = phone.replaceAll("[^0-9]", "");
        } else {
            this.phone = null;
        }
    }

    @Transient
    public String getFormattedPhone() {
        if (phone == null || phone.isEmpty()) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11) {
            return digits.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (digits.length() == 10) {
            if (digits.startsWith("02")) {
                return digits.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "$1-$2-$3");
            } else {
                return digits.replaceAll("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
            }
        } else if (digits.length() == 9 && digits.startsWith("02")) {
            return digits.replaceAll("(\\d{2})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return digits;
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
