package com.example.cmm.controller;

import com.example.cmm.entity.Attendance;
import com.example.cmm.entity.Member;
import com.example.cmm.repository.AttendanceRepository;
import com.example.cmm.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AttendanceController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // Root redirect to attendance
    @GetMapping("/")
    public String index() {
        return "redirect:/attendance";
    }

    // View: Attendance Page
    @GetMapping("/attendance")
    public String attendancePage() {
        return "attendance";
    }

    // View: Registration Page
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // View: Stats & Calendar Page
    @GetMapping("/stats")
    public String statsPage() {
        return "stats";
    }

    // View: Member Management Page
    @GetMapping("/members")
    public String membersPage() {
        return "members";
    }

    // API: Register new member face (직분, 부서, 연락처 및 중복 검사 포함)
    @PostMapping("/api/member/register")
    @ResponseBody
    public ResponseEntity<?> registerMember(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String faceDescriptor = (String) payload.get("descriptor");
        String position = (String) payload.get("position");
        String department = (String) payload.get("department");
        String phone = (String) payload.get("phone");
        String profileImage = (String) payload.get("profileImage");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이름을 입력해 주세요."));
        }

        String trimmedName = name.trim();

        // 1. 동일 인물 얼굴 중복 등록 검사 (벡터거리 < 0.42 - 골든 밸런스 기준)
        if (faceDescriptor != null) {
            List<Member> allMembers = memberRepository.findAll();
            for (Member existing : allMembers) {
                double dist = calculateEuclideanDistance(faceDescriptor, existing.getFaceDescriptor());
                if (dist < 0.42) { // 동일인 얼굴 중복 등록 방지 (0.42 최적 기준)
                    String existingPos = existing.getPosition() != null ? existing.getPosition() : "교우";
                    return ResponseEntity.ok(Map.of(
                        "success", false,
                        "alreadyRegistered", true,
                        "message", "촬영된 얼굴은 이미 '" + existing.getName() + " (" + existingPos + ")' 님으로 등록되어 있습니다."
                    ));
                }
            }
        }

        Member member = new Member(trimmedName, faceDescriptor);
        if (position != null && !position.trim().isEmpty()) member.setPosition(position.trim());
        if (department != null && !department.trim().isEmpty()) member.setDepartment(department.trim());
        if (phone != null && !phone.trim().isEmpty()) member.setPhone(phone.trim());
        if (profileImage != null && !profileImage.trim().isEmpty()) member.setProfileImage(profileImage);

        memberRepository.save(member);
        return ResponseEntity.ok(Map.of("success", true, "message", "'" + trimmedName + "' 교우님 등록이 완료되었습니다."));
    }

    // API: Update member details (교인 정보 및 프로필 사진 수정)
    @PutMapping("/api/member/{id}")
    @ResponseBody
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "해당 교인 정보를 찾을 수 없습니다."));
        }

        String name = (String) payload.get("name");
        String position = (String) payload.get("position");
        String department = (String) payload.get("department");
        String phone = (String) payload.get("phone");
        String profileImage = (String) payload.get("profileImage");
        String faceDescriptor = (String) payload.get("descriptor");

        if (name != null && !name.trim().isEmpty()) member.setName(name.trim());
        if (position != null) member.setPosition(position.trim());
        if (department != null) member.setDepartment(department.trim());
        if (phone != null) member.setPhone(phone.trim());
        if (profileImage != null) member.setProfileImage(profileImage);
        if (faceDescriptor != null && !faceDescriptor.trim().isEmpty()) member.setFaceDescriptor(faceDescriptor);

        memberRepository.save(member);
        return ResponseEntity.ok(Map.of("success", true, "message", "'" + member.getName() + "' 교우님 정보가 수정되었습니다."));
    }

    // API: Delete member (교인 정보 삭제 및 연관 출석 이력 정리)
    @DeleteMapping("/api/member/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "해당 교인 정보를 찾을 수 없습니다."));
        }

        attendanceRepository.deleteByMemberId(id);
        memberRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("success", true, "message", "'" + member.getName() + "' 교우님 정보가 삭제되었습니다."));
    }

    private double calculateEuclideanDistance(String desc1, String desc2) {
        if (desc1 == null || desc2 == null) return Double.MAX_VALUE;
        try {
            List<double[]> list1 = parseDescriptorJson(desc1);
            List<double[]> list2 = parseDescriptorJson(desc2);
            if (list1.isEmpty() || list2.isEmpty()) return Double.MAX_VALUE;

            double minDistance = Double.MAX_VALUE;
            for (double[] v1 : list1) {
                for (double[] v2 : list2) {
                    if (v1.length == v2.length && v1.length > 0) {
                        double sum = 0.0;
                        for (int i = 0; i < v1.length; i++) {
                            double diff = v1[i] - v2[i];
                            sum += diff * diff;
                        }
                        double dist = Math.sqrt(sum);
                        if (dist < minDistance) {
                            minDistance = dist;
                        }
                    }
                }
            }
            return minDistance;
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    private List<double[]> parseDescriptorJson(String jsonStr) {
        List<double[]> result = new ArrayList<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) return result;
        String trimmed = jsonStr.trim();
        try {
            if (trimmed.startsWith("[[") || trimmed.startsWith("[ [")) {
                // Multi-pose descriptors: [[...], [...]]
                String inner = trimmed.substring(1, trimmed.length() - 1);
                String[] items = inner.split("\\]\\s*,\\s*\\[");
                for (String item : items) {
                    String clean = item.replace("[", "").replace("]", "").trim();
                    if (!clean.isEmpty()) {
                        String[] parts = clean.split(",");
                        double[] arr = new double[parts.length];
                        for (int i = 0; i < parts.length; i++) {
                            arr[i] = Double.parseDouble(parts[i].trim());
                        }
                        result.add(arr);
                    }
                }
            } else {
                // Single-pose descriptor: [0.1, 0.2, ...]
                String clean = trimmed.replace("[", "").replace("]", "").trim();
                if (!clean.isEmpty()) {
                    String[] parts = clean.split(",");
                    double[] arr = new double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        arr[i] = Double.parseDouble(parts[i].trim());
                    }
                    result.add(arr);
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return result;
    }

    // API: Get all members for client-side face matching
    @GetMapping("/api/members")
    @ResponseBody
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberRepository.findAll());
    }

    // API: Get today's attendance count
    @GetMapping("/api/attendance/today-count")
    @ResponseBody
    public ResponseEntity<?> getTodayAttendanceCount() {
        long count = attendanceRepository.countByAttendanceDate(LocalDate.now());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // API: Record attendance (1인 1회 제한 및 시간 정보 반환)
    @PostMapping("/api/attendance/check")
    @ResponseBody
    public ResponseEntity<?> checkAttendance(@RequestBody Map<String, Long> payload) {
        Long memberId = payload.get("memberId");
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Member not found"));
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // 1인 1회 중복 출석 검사
        if (attendanceRepository.existsByMemberIdAndAttendanceDate(memberId, today)) {
            Optional<Attendance> existOpt = attendanceRepository.findTopByMemberIdAndAttendanceDate(memberId, today);
            String timeStr = existOpt.map(a -> a.getCheckInTime().format(timeFormatter)).orElse("");
            long todayCount = attendanceRepository.countByAttendanceDate(today);

            return ResponseEntity.ok(Map.of(
                "success", false,
                "alreadyChecked", true,
                "message", member.getName() + " 교우님은 이미 오늘 출석이 확인되셨습니다. (출석시간: " + timeStr + ")",
                "todayCount", todayCount,
                "checkInTime", timeStr
            ));
        }

        LocalDateTime now = LocalDateTime.now();
        Attendance attendance = new Attendance(member, now);
        attendanceRepository.save(attendance);

        long todayCount = attendanceRepository.countByAttendanceDate(today);
        String timeStr = now.format(timeFormatter);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "alreadyChecked", false,
            "message", member.getName() + " 교우님, 출석 완료! (출석시간: " + timeStr + ")",
            "todayCount", todayCount,
            "checkInTime", timeStr
        ));
    }

    // API: Record attendance for unregistered visitor (미등록 교인 / 새가족 / 방문자 출석 기록)
    @PostMapping("/api/attendance/check-unregistered")
    @ResponseBody
    public ResponseEntity<?> checkUnregisteredAttendance() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        Attendance attendance = new Attendance(now, "VISITOR");
        attendanceRepository.save(attendance);

        long todayCount = attendanceRepository.countByAttendanceDate(today);
        String timeStr = now.format(timeFormatter);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "새가족/방문자 출석 완료! (시간: " + timeStr + ")",
            "todayCount", todayCount,
            "checkInTime", timeStr
        ));
    }

    // API: Calendar summary events
    @GetMapping("/api/attendance/calendar-events")
    @ResponseBody
    public ResponseEntity<?> getCalendarEvents(@RequestParam(required = false) String start,
                                                @RequestParam(required = false) String end) {
        LocalDate startDate = (start != null && !start.isEmpty())
            ? LocalDate.parse(start.substring(0, 10))
            : LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = (end != null && !end.isEmpty())
            ? LocalDate.parse(end.substring(0, 10))
            : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Attendance> list = attendanceRepository.findByAttendanceDateBetween(startDate, endDate);

        Map<LocalDate, Long> countMap = list.stream()
            .collect(Collectors.groupingBy(Attendance::getAttendanceDate, Collectors.counting()));

        List<Map<String, Object>> events = new ArrayList<>();
        countMap.forEach((date, count) -> {
            events.add(Map.of(
                "title", "출석 " + count + "명",
                "start", date.toString(),
                "count", count,
                "allDay", true
            ));
        });

        return ResponseEntity.ok(events);
    }

    // API: Daily attendance statistics and hourly distribution
    @GetMapping("/api/attendance/daily-stats")
    @ResponseBody
    public ResponseEntity<?> getDailyStats(@RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null && !date.isEmpty())
            ? LocalDate.parse(date)
            : LocalDate.now();

        List<Attendance> list = attendanceRepository.findByAttendanceDate(targetDate);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        Map<String, Integer> hourlyMap = new LinkedHashMap<>();
        for (int h = 6; h <= 22; h++) {
            String hourLabel = String.format("%02d:00", h);
            hourlyMap.put(hourLabel, 0);
        }

        List<Map<String, Object>> memberDetails = new ArrayList<>();

        for (Attendance a : list) {
            int hour = a.getCheckInTime().getHour();
            if (hour >= 6 && hour <= 22) {
                String hourLabel = String.format("%02d:00", hour);
                hourlyMap.put(hourLabel, hourlyMap.get(hourLabel) + 1);
            }

            Member m = a.getMember();
            if (m != null) {
                memberDetails.add(Map.of(
                    "id", m.getId(),
                    "name", m.getName(),
                    "department", m.getDepartment() != null ? m.getDepartment() : "미지정",
                    "position", m.getPosition() != null ? m.getPosition() : "교우",
                    "checkInTime", a.getCheckInTime().format(timeFormatter)
                ));
            } else {
                memberDetails.add(Map.of(
                    "id", 0L,
                    "name", "새가족/방문자 (미등록)",
                    "department", "방문",
                    "position", "방문자",
                    "checkInTime", a.getCheckInTime().format(timeFormatter)
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
            "date", targetDate.toString(),
            "totalCount", list.size(),
            "hourlyLabels", new ArrayList<>(hourlyMap.keySet()),
            "hourlyData", new ArrayList<>(hourlyMap.values()),
            "members", memberDetails
        ));
    }
}
