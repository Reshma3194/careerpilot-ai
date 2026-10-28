package com.careerpilot.service;

import com.careerpilot.entity.Resume;
import com.careerpilot.entity.User;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ResumeRepository;
import com.careerpilot.repository.UserRepository;
import com.careerpilot.util.ResumeTextExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeTextExtractor extractor;

    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository,
                         ResumeTextExtractor extractor) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.extractor = extractor;
    }

    @Transactional
    public Resume upload(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a resume file to upload");
        }
        String text = extractor.extract(file);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setFileName(file.getOriginalFilename());
        resume.setContentType(file.getContentType());
        resume.setExtractedText(text);
        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public List<Resume> list(String email) {
        return resumeRepository.findByUserEmailOrderByUploadedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Resume get(String email, Long id) {
        return resumeRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    @Transactional
    public void delete(String email, Long id) {
        resumeRepository.delete(get(email, id));
    }
}
