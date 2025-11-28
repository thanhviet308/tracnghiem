package com.example.tracnghiem.service;

import com.example.tracnghiem.domain.question.Passage;
import com.example.tracnghiem.domain.subject.Chapter;
import com.example.tracnghiem.domain.subject.Subject;
import com.example.tracnghiem.dto.subject.*;
import com.example.tracnghiem.exception.ResourceNotFoundException;
import com.example.tracnghiem.repository.ChapterRepository;
import com.example.tracnghiem.repository.PassageRepository;
import com.example.tracnghiem.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final PassageRepository passageRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          ChapterRepository chapterRepository,
                          PassageRepository passageRepository) {
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
        this.passageRepository = passageRepository;
    }

    public List<SubjectResponse> getSubjects() {
        return subjectRepository.findAll().stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    public SubjectResponse createSubject(SubjectRequest request) {
        Subject subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .active(request.active())
                .build();
        return toSubjectResponse(subjectRepository.save(subject));
    }

    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = getSubjectEntity(id);
        subject.setName(request.name());
        subject.setDescription(request.description());
        subject.setActive(request.active());
        return toSubjectResponse(subjectRepository.save(subject));
    }

    public ChapterResponse createChapter(ChapterRequest request) {
        Subject subject = getSubjectEntity(request.subjectId());
        Chapter chapter = Chapter.builder()
                .subject(subject)
                .name(request.name())
                .description(request.description())
                .build();
        return toChapterResponse(chapterRepository.save(chapter));
    }

    public ChapterResponse updateChapter(Long chapterId, ChapterRequest request) {
        Chapter chapter = getChapterEntity(chapterId);
        Subject subject = getSubjectEntity(request.subjectId());
        chapter.setSubject(subject);
        chapter.setName(request.name());
        chapter.setDescription(request.description());
        return toChapterResponse(chapterRepository.save(chapter));
    }

    public List<ChapterResponse> getChapters(Long subjectId) {
        return chapterRepository.findBySubject_Id(subjectId).stream()
                .map(this::toChapterResponse)
                .toList();
    }

    public PassageResponse createPassage(PassageRequest request) {
        Chapter chapter = getChapterEntity(request.chapterId());
        Passage passage = Passage.builder()
                .chapter(chapter)
                .content(request.content())
                .build();
        return toPassageResponse(passageRepository.save(passage));
    }

    public PassageResponse updatePassage(Long passageId, PassageRequest request) {
        Passage passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new ResourceNotFoundException("Passage not found"));
        Chapter chapter = getChapterEntity(request.chapterId());
        passage.setChapter(chapter);
        passage.setContent(request.content());
        return toPassageResponse(passageRepository.save(passage));
    }

    public List<PassageResponse> getPassages(Long chapterId) {
        return passageRepository.findByChapter_Id(chapterId).stream()
                .map(this::toPassageResponse)
                .toList();
    }

    public void deleteSubject(Long id) {
        Subject subject = getSubjectEntity(id);
        subjectRepository.delete(subject);
    }

    private Subject getSubjectEntity(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }

    private Chapter getChapterEntity(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
    }

    private SubjectResponse toSubjectResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.isActive(),
                subject.getCreatedAt()
        );
    }

    private ChapterResponse toChapterResponse(Chapter chapter) {
        return new ChapterResponse(chapter.getId(), chapter.getSubject().getId(), chapter.getName(), chapter.getDescription());
    }

    private PassageResponse toPassageResponse(Passage passage) {
        return new PassageResponse(passage.getId(), passage.getChapter().getId(), passage.getContent());
    }
}

