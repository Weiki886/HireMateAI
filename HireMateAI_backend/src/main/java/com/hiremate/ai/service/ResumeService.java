package com.hiremate.ai.service;

import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.request.ResumeRequest;
import com.hiremate.ai.dto.response.ResumeResponse;
import com.hiremate.ai.entity.Resume;
import com.hiremate.ai.mapper.ResumeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeMapper resumeMapper;

    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long userId, Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "简历不存在");
        }
        return toResponse(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> listResumes(Long userId) {
        LambdaQueryWrapper<Resume> qw = new LambdaQueryWrapper<>();
        qw.eq(Resume::getUserId, userId).orderByDesc(Resume::getUpdatedAt);
        return resumeMapper.selectList(qw).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResumeResponse createResume(Long userId, ResumeRequest.Create request) {
        Resume resume = Resume.builder()
                .userId(userId)
                .title(request.getTitle())
                .targetPosition(request.getTargetPosition())
                .targetIndustry(request.getTargetIndustry())
                .contentJson(request.getContentJson())
                .version(1)
                .status("COMPLETED")
                .build();
        resumeMapper.insert(resume);
        return toResponse(resume);
    }

    @Transactional
    public ResumeResponse updateResume(Long userId, Long resumeId, ResumeRequest.Update request) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "简历不存在");
        }

        LambdaUpdateWrapper<Resume> uw = new LambdaUpdateWrapper<>();
        uw.eq(Resume::getId, resumeId);

        if (request.getTitle() != null) {
            uw.set(Resume::getTitle, request.getTitle());
        }
        if (request.getTargetPosition() != null) {
            uw.set(Resume::getTargetPosition, request.getTargetPosition());
        }
        if (request.getTargetIndustry() != null) {
            uw.set(Resume::getTargetIndustry, request.getTargetIndustry());
        }
        if (request.getContentJson() != null) {
            uw.set(Resume::getContentJson, request.getContentJson());
            uw.set(Resume::getStatus, "COMPLETED");
        }

        resumeMapper.update(null, uw);
        resume = resumeMapper.selectById(resumeId);
        return toResponse(resume);
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "简历不存在");
        }
        resumeMapper.deleteById(resumeId);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getVersions(Long userId, Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "简历不存在");
        }

        LambdaQueryWrapper<Resume> qw = new LambdaQueryWrapper<>();
        qw.eq(Resume::getUserId, userId)
                .eq(resume.getTitle() != null, Resume::getTitle, resume.getTitle())
                .orderByDesc(Resume::getVersion);

        return resumeMapper.selectList(qw).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .userId(resume.getUserId())
                .title(resume.getTitle())
                .version(resume.getVersion())
                .targetPosition(resume.getTargetPosition())
                .targetIndustry(resume.getTargetIndustry())
                .status(resume.getStatus())
                .contentJson(resume.getContentJson())
                .contentPdfUrl(resume.getContentPdfUrl())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
