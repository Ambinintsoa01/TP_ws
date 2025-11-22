package com.apiEtudiant.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apiEtudiant.dto.ApiResponse;
import com.apiEtudiant.repository.GradeRepository;
import com.apiEtudiant.repository.CreditRepository;
import com.apiEtudiant.repository.AcademicYearSemesterRepository;
import com.apiEtudiant.entity.AcademicYearSemester;
import com.apiEtudiant.entity.Credit;
import com.apiEtudiant.entity.Grade;
import com.apiEtudiant.entity.Subject;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
public class GradesController {

    private final GradeRepository gradeRepository;
    private final CreditRepository creditRepository;
    private final AcademicYearSemesterRepository aysRepository;
    private final com.apiEtudiant.repository.OptionalGroupSubjectRepository optionalGroupSubjectRepository;

    public GradesController(GradeRepository gradeRepository, CreditRepository creditRepository, AcademicYearSemesterRepository aysRepository, com.apiEtudiant.repository.OptionalGroupSubjectRepository optionalGroupSubjectRepository) {
        this.gradeRepository = gradeRepository;
        this.creditRepository = creditRepository;
        this.aysRepository = aysRepository;
        this.optionalGroupSubjectRepository = optionalGroupSubjectRepository;
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGradesBySemester(@PathVariable("semesterId") Long semesterId) {
        List<Object[]> rows = gradeRepository.findGradesBySemesterId(semesterId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            // projection: s.id, s.lastName, s.firstName, p.label, g.id, subj.id, subj.code, subj.title, g.grade, ays.semester.id, ays.option.label
            m.put("studentId", r[0]);
            m.put("lastName", r[1]);
            m.put("firstName", r[2]);
            m.put("program", r[3]);
            m.put("gradeId", r[4]);
            m.put("subjectId", r[5]);
            m.put("subjectCode", r[6]);
            m.put("subjectTitle", r[7]);
            m.put("grade", r[8]);
            m.put("semesterId", r[9]);
            m.put("optionLabel", r[10]);
            list.add(m);
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/semester/{semesterId}/option/{optionId}/subjects")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubjectsAndGradesByOption(
            @PathVariable("semesterId") Long semesterId,
            @PathVariable("optionId") Long optionId) {

        // 1) load credits (subjects) for the option
        List<Credit> credits = creditRepository.findByOptionId(optionId);
        List<Subject> subjects = credits.stream().map(Credit::getSubject).collect(Collectors.toList());

        List<com.apiEtudiant.entity.OptionalGroupSubject> optSubjects = optionalGroupSubjectRepository.findByOptionalGroup_Semester_Id(semesterId);
        Map<Long, Long> subjectOptionalGroupIds = new java.util.HashMap<>();
        for (com.apiEtudiant.entity.OptionalGroupSubject ogs : optSubjects) {
            if (ogs.getOptionalGroup() == null || ogs.getOptionalGroup().getOption() == null) continue;
            if (!ogs.getOptionalGroup().getOption().getId().equals(optionId)) continue;
            if (ogs.getSubject() != null && ogs.getSubject().getId() != null) {
                subjectOptionalGroupIds.put(ogs.getSubject().getId(), ogs.getOptionalGroup().getId());
            }
        }

        List<Map<String, Object>> subjectList = subjects.stream()
            .map(sub -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("subjectId", sub.getId());
                m.put("subjectCode", sub.getCode());
                m.put("subjectTitle", sub.getTitle());
                Long optGroupId = subjectOptionalGroupIds.get(sub.getId());
                m.put("optionalGroupId", optGroupId);
                m.put("optionalInSemester", optGroupId != null);
                return m;
            })
            .collect(Collectors.toList());

        if (subjects.isEmpty()) {
            Map<String, Object> resp = Map.of("subjects", subjectList, "students", List.of(), "subjectAverages", List.of(), "optionAverage", null);
            return ResponseEntity.ok(ApiResponse.success(resp));
        }

        List<Long> subjectIds = subjects.stream().map(Subject::getId).collect(Collectors.toList());

        // 2) load grades in that semester for these subjects
        List<Grade> grades = gradeRepository.findBySemesterIdAndSubjectIds(semesterId, subjectIds);

        // 3) group by student and subject, pick best grade per student/subject
        Map<Long, Map<Long, Float>> best = new java.util.HashMap<>(); // studentId -> (subjectId -> bestGrade)
        Map<Long, Map<Long, Float>> optionalBest = new java.util.HashMap<>(); // studentId -> (optionalGroupId -> bestGrade)
        Map<Long, Map<String, Object>> studentInfo = new java.util.HashMap<>();

        for (Grade g : grades) {
            Long sid = g.getStudent() != null ? g.getStudent().getId() : null;
            if (sid == null) continue;
            Long subjId = g.getSubject() != null ? g.getSubject().getId() : null;
            Float val = g.getGrade();
            best.putIfAbsent(sid, new java.util.HashMap<>());
            Map<Long, Float> subjMap = best.get(sid);
            if (val != null) {
                Float cur = subjMap.get(subjId);
                if (cur == null || val > cur) subjMap.put(subjId, val);
                Long optGroupId = subjId != null ? subjectOptionalGroupIds.get(subjId) : null;
                if (optGroupId != null) {
                    optionalBest.putIfAbsent(sid, new java.util.HashMap<>());
                    Map<Long, Float> optMap = optionalBest.get(sid);
                    Float curOpt = optMap.get(optGroupId);
                    if (curOpt == null || val > curOpt) optMap.put(optGroupId, val);
                }
            }
            if (!studentInfo.containsKey(sid)) {
                Map<String, Object> info = new java.util.LinkedHashMap<>();
                info.put("studentId", sid);
                info.put("lastName", g.getStudent() != null ? g.getStudent().getLastName() : null);
                info.put("firstName", g.getStudent() != null ? g.getStudent().getFirstName() : null);
                studentInfo.put(sid, info);
            }
        }

        // 4) build students array with all subjects (null if no grade) and compute subject averages
        List<Map<String, Object>> students = new java.util.ArrayList<>();
        Map<Long, java.util.List<Float>> subjGrades = new java.util.HashMap<>(); // subjId -> list of grades

        for (Map.Entry<Long, Map<Long, Float>> e : best.entrySet()) {
            Long sid = e.getKey();
            Map<Long, Float> subjMap = e.getValue();
            Map<String, Object> sinfo = new java.util.LinkedHashMap<>();
            Map<String, Object> info = (Map<String, Object>) studentInfo.get(sid);
            sinfo.put("studentId", info.get("studentId"));
            sinfo.put("lastName", info.get("lastName"));
            sinfo.put("firstName", info.get("firstName"));
            List<Map<String, Object>> gradeList = new java.util.ArrayList<>();
            for (Subject sub : subjects) {
                Long sidSub = sub.getId();
                Float gval = subjMap.get(sidSub);
                Long optGroupId = subjectOptionalGroupIds.get(sidSub);
                Float bestGroupGrade = null;
                if (optGroupId != null && optionalBest.containsKey(sid)) {
                    bestGroupGrade = optionalBest.get(sid).get(optGroupId);
                }
                Map<String, Object> gradeRow = new java.util.LinkedHashMap<>();
                gradeRow.put("subjectId", sidSub);
                gradeRow.put("subjectCode", sub.getCode());
                gradeRow.put("subjectTitle", sub.getTitle());
                gradeRow.put("bestGrade", gval); // best for the subject itself
                gradeRow.put("optionalGroupId", optGroupId);
                gradeRow.put("bestOptionalGrade", bestGroupGrade);
                gradeRow.put("rawGrade", gval);
                gradeList.add(gradeRow);
                if (gval != null) {
                    subjGrades.putIfAbsent(sidSub, new java.util.ArrayList<>());
                    subjGrades.get(sidSub).add(gval);
                }
            }
            sinfo.put("grades", gradeList);
            students.add(sinfo);
        }

        // For students that have no grades at all we may still want to include them? For now we only include students who have at least one grade.

        // 5) compute subject averages and overall option average
        List<Map<String, Object>> subjectAverages = new java.util.ArrayList<>();
        double optionSum = 0.0; long optionCount = 0;
        for (Subject sub : subjects) {
            List<Float> gvals = subjGrades.getOrDefault(sub.getId(), java.util.List.of());
            Double avg = null;
            if (!gvals.isEmpty()) {
                double s = 0.0; for (Float f : gvals) s += f;
                avg = Math.round((s / gvals.size()) * 100.0) / 100.0;
                optionSum += s; optionCount += gvals.size();
            }
            Map<String, Object> avgRow = new java.util.LinkedHashMap<>();
            avgRow.put("subjectId", sub.getId());
            avgRow.put("subjectCode", sub.getCode());
            avgRow.put("subjectTitle", sub.getTitle());
            avgRow.put("average", avg); // avg can stay null
            avgRow.put("count", gvals.size());
            subjectAverages.add(avgRow);
        }

        Double optionAverage = null;
        if (optionCount > 0) optionAverage = Math.round((optionSum / optionCount) * 100.0) / 100.0;

        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("subjects", subjectList);
        resp.put("students", students);
        resp.put("subjectAverages", subjectAverages);
        resp.put("optionAverage", optionAverage);

        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/subjects/credits/semester/{semesterId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSubjectsCreditsBySemester(@PathVariable("semesterId") Long semesterId) {
        // find option ids used in this semester (from academic_year_semesters)
        List<AcademicYearSemester> ays = aysRepository.findAll();
        java.util.Set<Long> optionIds = new java.util.HashSet<>();
        for (AcademicYearSemester a : ays) {
            if (a.getSemester() != null && a.getSemester().getId() != null && a.getSemester().getId().equals(semesterId)) {
                if (a.getOption() != null && a.getOption().getId() != null) optionIds.add(a.getOption().getId());
            }
        }

        // find subject ids that are optional in this semester via optional groups
        List<com.apiEtudiant.entity.OptionalGroupSubject> optSubjects = optionalGroupSubjectRepository.findByOptionalGroup_Semester_Id(semesterId);
        java.util.Set<Long> optionalSubjectIds = new java.util.HashSet<>();
        java.util.Map<Long, Long> subjToOptionalGroup = new java.util.HashMap<>(); // subjectId -> optionalGroupId
        for (com.apiEtudiant.entity.OptionalGroupSubject ogs : optSubjects) {
            if (ogs.getSubject() != null && ogs.getSubject().getId() != null) {
                optionalSubjectIds.add(ogs.getSubject().getId());
                if (ogs.getOptionalGroup() != null && ogs.getOptionalGroup().getId() != null) subjToOptionalGroup.put(ogs.getSubject().getId(), ogs.getOptionalGroup().getId());
            }
        }

        List<Credit> credits = creditRepository.findAll();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Credit c : credits) {
            if (c.getSubject() == null) continue;
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            Long subjId = c.getSubject().getId();
            m.put("subjectId", subjId);
            m.put("subjectCode", c.getSubject().getCode());
            m.put("subjectTitle", c.getSubject().getTitle());
            m.put("credits", c.getCredits());
            if (c.getOption() != null) {
                m.put("optionId", c.getOption().getId());
                m.put("optionLabel", c.getOption().getLabel());
            } else {
                m.put("optionId", null);
                m.put("optionLabel", null);
            }
            m.put("optionalInSemester", optionalSubjectIds.contains(subjId));
            if (subjToOptionalGroup.containsKey(subjId)) m.put("optionalGroupId", subjToOptionalGroup.get(subjId));
            result.add(m);
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableOptions() {
        // collect distinct options from credits
        List<com.apiEtudiant.entity.Credit> credits = creditRepository.findAll();
        Map<Long, String> opts = new java.util.LinkedHashMap<>();
        for (com.apiEtudiant.entity.Credit c : credits) {
            if (c.getOption() != null) opts.put(c.getOption().getId(), c.getOption().getLabel());
        }
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (Map.Entry<Long, String> e : opts.entrySet()) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", e.getKey());
            m.put("label", e.getValue());
            list.add(m);
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/semester/{semesterId}/subjects/grades")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGradesBySemesterSubjects(@PathVariable("semesterId") Long semesterId) {
        // 1) determine option ids present in this semester
        List<AcademicYearSemester> ays = aysRepository.findAll();
        java.util.Set<Long> optionIds = new java.util.HashSet<>();
        for (AcademicYearSemester a : ays) {
            if (a.getSemester() != null && a.getSemester().getId() != null && a.getSemester().getId().equals(semesterId)) {
                if (a.getOption() != null && a.getOption().getId() != null) optionIds.add(a.getOption().getId());
            }
        }

        // 2) collect credits that belong to these options -> subjects for the semester
        List<Credit> allCredits = creditRepository.findAll();
        List<Subject> subjects = new java.util.ArrayList<>();
        for (Credit c : allCredits) {
            if (c.getSubject() == null || c.getOption() == null) continue;
            if (optionIds.contains(c.getOption().getId())) {
                subjects.add(c.getSubject());
            }
        }

        if (subjects.isEmpty()) {
            Map<String, Object> resp = Map.of("subjects", List.of(), "students", List.of(), "subjectAverages", List.of(), "optionAverages", List.of());
            return ResponseEntity.ok(ApiResponse.success(resp));
        }

        List<Long> subjectIds = subjects.stream().map(Subject::getId).collect(Collectors.toList());

        // 3) optional groups mapping for this semester
        List<com.apiEtudiant.entity.OptionalGroupSubject> optSubjects = optionalGroupSubjectRepository.findByOptionalGroup_Semester_Id(semesterId);
        java.util.Map<Long, java.util.List<Long>> optionalGroupMembers = new java.util.HashMap<>(); // optionalGroupId -> list of subjectIds
        java.util.Map<Long, Long> subjToGroup = new java.util.HashMap<>();
        for (com.apiEtudiant.entity.OptionalGroupSubject ogs : optSubjects) {
            if (ogs.getOptionalGroup() == null || ogs.getOptionalGroup().getId() == null) continue;
            Long gid = ogs.getOptionalGroup().getId();
            Long sid = ogs.getSubject() != null ? ogs.getSubject().getId() : null;
            if (sid == null) continue;
            optionalGroupMembers.putIfAbsent(gid, new java.util.ArrayList<>());
            optionalGroupMembers.get(gid).add(sid);
            subjToGroup.put(sid, gid);
        }

        // 4) load grades for this semester and these subjects
        List<Grade> grades = gradeRepository.findBySemesterIdAndSubjectIds(semesterId, subjectIds);

        // 5) build best per student per subject and best per student per optional group
        java.util.Map<Long, java.util.Map<Long, Float>> bestPerStudentSubject = new java.util.HashMap<>();
        java.util.Map<Long, java.util.Map<Long, Float>> bestPerStudentOptGroup = new java.util.HashMap<>();
        java.util.Map<Long, Map<String, Object>> studentInfo = new java.util.HashMap<>();

        for (Grade g : grades) {
            Long sid = g.getStudent() != null ? g.getStudent().getId() : null;
            if (sid == null) continue;
            Long subId = g.getSubject() != null ? g.getSubject().getId() : null;
            if (subId == null) continue;
            Float val = g.getGrade();

            // per-subject best
            bestPerStudentSubject.putIfAbsent(sid, new java.util.HashMap<>());
            Map<Long, Float> subjMap = bestPerStudentSubject.get(sid);
            if (val != null) {
                Float cur = subjMap.get(subId);
                if (cur == null || val > cur) subjMap.put(subId, val);
            }

            // optional group best
            Long gid = subjToGroup.get(subId);
            if (gid != null) {
                bestPerStudentOptGroup.putIfAbsent(sid, new java.util.HashMap<>());
                Map<Long, Float> gmap = bestPerStudentOptGroup.get(sid);
                Float cur = gmap.get(gid);
                if (val != null) {
                    if (cur == null || val > cur) gmap.put(gid, val);
                }
            }

            if (!studentInfo.containsKey(sid)) {
                studentInfo.put(sid, Map.of("studentId", sid, "lastName", g.getStudent().getLastName(), "firstName", g.getStudent().getFirstName()));
            }
        }

        // 6) build students array: for optional group members we only display the best on the first member of the group
        List<Map<String, Object>> students = new java.util.ArrayList<>();
        Map<Long, java.util.List<Float>> subjGrades = new java.util.HashMap<>();

        // compute representative first member for each optional group
        java.util.Map<Long, Long> optGroupRepresentative = new java.util.HashMap<>();
        for (Map.Entry<Long, java.util.List<Long>> e : optionalGroupMembers.entrySet()) {
            java.util.List<Long> members = e.getValue();
            members.sort(Long::compareTo);
            if (!members.isEmpty()) optGroupRepresentative.put(e.getKey(), members.get(0));
        }

        for (Map.Entry<Long, Map<Long, Float>> e : bestPerStudentSubject.entrySet()) {
            Long sid = e.getKey();
            Map<Long, Float> subjMap = e.getValue();
            Map<String, Object> sinfo = new java.util.LinkedHashMap<>();
            Map<String, Object> info = studentInfo.get(sid);
            sinfo.put("studentId", info.get("studentId"));
            sinfo.put("lastName", info.get("lastName"));
            sinfo.put("firstName", info.get("firstName"));
            List<Map<String, Object>> gradeList = new java.util.ArrayList<>();

            for (Subject sub : subjects) {
                Long sidSub = sub.getId();
                Float recordedGrade = subjMap.get(sidSub); // best grade recorded for this subject
                Float displayVal = recordedGrade;
                Float bestOptionalGrade = null;
                Long gid = subjToGroup.get(sidSub);
                if (gid != null) {
                    Map<Long, Float> gmap = bestPerStudentOptGroup.getOrDefault(sid, java.util.Map.of());
                    bestOptionalGrade = gmap.get(gid);
                    if (bestOptionalGrade != null) displayVal = bestOptionalGrade;
                }

                Map<String, Object> gradeEntry = new java.util.LinkedHashMap<>();
                gradeEntry.put("subjectId", sidSub);
                gradeEntry.put("subjectCode", sub.getCode());
                gradeEntry.put("subjectTitle", sub.getTitle());
                gradeEntry.put("grade", displayVal);
                if (bestOptionalGrade != null) gradeEntry.put("bestOptionalGrade", bestOptionalGrade);
                if (gid != null) {
                    gradeEntry.put("optionalGroupId", gid);
                    Long rep = optGroupRepresentative.get(gid);
                    gradeEntry.put("optionalGroupRepresentative", rep != null && rep.equals(sidSub));
                }
                if (recordedGrade != null) gradeEntry.put("rawGrade", recordedGrade);

                gradeList.add(gradeEntry);
                if (recordedGrade != null) {
                    subjGrades.putIfAbsent(sidSub, new java.util.ArrayList<>());
                    subjGrades.get(sidSub).add(recordedGrade);
                }
            }

            sinfo.put("grades", gradeList);
            students.add(sinfo);
        }

        // 7) compute subject averages
        List<Map<String, Object>> subjectAverages = new java.util.ArrayList<>();
        for (Subject sub : subjects) {
            List<Float> gvals = subjGrades.getOrDefault(sub.getId(), java.util.List.of());
            Double avg = null;
            if (!gvals.isEmpty()) {
                double s = 0.0; for (Float f : gvals) s += f;
                avg = Math.round((s / gvals.size()) * 100.0) / 100.0;
            }
            subjectAverages.add(Map.of("subjectId", sub.getId(), "subjectCode", sub.getCode(), "subjectTitle", sub.getTitle(), "average", avg, "count", gvals.size()));
        }

        // 8) compute option averages by using credits to map subject -> option
        Map<Long, double[]> optionAgg = new java.util.HashMap<>(); // optionId -> [sum,count]
        Map<Long, String> optionLabels = new java.util.HashMap<>();
        for (Credit c : allCredits) {
            if (c.getSubject() == null || c.getOption() == null) continue;
            Long sidSub = c.getSubject().getId();
            if (!subjectIds.contains(sidSub)) continue;
            Long optId = c.getOption().getId();
            optionLabels.put(optId, c.getOption().getLabel());
        }

        for (Map.Entry<Long, java.util.List<Float>> e : subjGrades.entrySet()) {
            Long subjId = e.getKey();
            java.util.List<Float> vals = e.getValue();
            // find option for this subject from credits
            Long optId = null;
            for (Credit c : allCredits) {
                if (c.getSubject() != null && c.getSubject().getId().equals(subjId) && c.getOption() != null) {
                    optId = c.getOption().getId();
                    break;
                }
            }
            if (optId == null) continue;
            optionAgg.putIfAbsent(optId, new double[]{0.0, 0.0});
            double[] arr = optionAgg.get(optId);
            for (Float v : vals) { arr[0] += v; arr[1] += 1.0; }
        }

        List<Map<String, Object>> optionAverages = new java.util.ArrayList<>();
        for (Map.Entry<Long, double[]> e : optionAgg.entrySet()) {
            Long optId = e.getKey();
            double[] arr = e.getValue();
            Double avg = null; if (arr[1] > 0) avg = Math.round((arr[0] / arr[1]) * 100.0) / 100.0;
            optionAverages.add(Map.of("optionId", optId, "optionLabel", optionLabels.get(optId), "average", avg, "count", (long) arr[1]));
        }

        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        // subjects as maps
        List<Map<String,Object>> subjectList = subjects.stream().map(sub -> {
            Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("subjectId", sub.getId());
            m.put("subjectCode", sub.getCode());
            m.put("subjectTitle", sub.getTitle());
            // find credit entry for this subject to expose credits and option info
            Credit credit = allCredits.stream().filter(c -> c.getSubject() != null && c.getSubject().getId() != null && c.getSubject().getId().equals(sub.getId())).findFirst().orElse(null);
            if (credit != null) {
                m.put("credits", credit.getCredits());
                if (credit.getOption() != null) {
                    m.put("optionId", credit.getOption().getId());
                    m.put("optionLabel", credit.getOption().getLabel());
                } else {
                    m.put("optionId", null);
                    m.put("optionLabel", null);
                }
            } else {
                m.put("credits", null);
                m.put("optionId", null);
                m.put("optionLabel", null);
            }
            m.put("optionalInSemester", subjToGroup.containsKey(sub.getId()));
            if (subjToGroup.containsKey(sub.getId())) m.put("optionalGroupId", subjToGroup.get(sub.getId()));
            return m;
        }).collect(Collectors.toList());

        resp.put("subjects", subjectList);
        resp.put("students", students);
        resp.put("subjectAverages", subjectAverages);
        resp.put("optionAverages", optionAverages);

        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}
