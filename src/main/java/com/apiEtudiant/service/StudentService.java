package com.apiEtudiant.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apiEtudiant.entity.Student;
import com.apiEtudiant.exception.ApiException;
import com.apiEtudiant.exception.ErrorCode;
import com.apiEtudiant.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public Student requireStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.STUDENT_NOT_FOUND,
                        "Aucun étudiant trouvé avec l'identifiant " + studentId + ".",
                        HttpStatus.NOT_FOUND));
    }
}
