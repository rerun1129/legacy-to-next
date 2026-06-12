package com.freightos.fms.adapter.out.persistence.attachment;

import com.freightos.fms.adapter.out.persistence.attachment.entity.BlAttachmentJpaEntity;
import com.freightos.fms.application.attachment.port.out.BlAttachmentPort;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BlAttachmentPersistenceAdapter implements BlAttachmentPort {

    private final BlAttachmentJpaRepository blAttachmentJpaRepository;
    private final BlAttachmentDomainToJpaMapper domainToJpaMapper;
    private final BlAttachmentJpaToDomainMapper jpaToDomainMapper;

    @Override
    @Transactional
    public Long saveAttachment(BlAttachment attachment) {
        BlAttachmentJpaEntity saved = blAttachmentJpaRepository.save(domainToJpaMapper.toJpa(attachment));
        return saved.getBlAttachmentId();
    }

    @Override
    public Optional<BlAttachment> findAttachmentById(Long id) {
        return blAttachmentJpaRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public List<BlAttachment> findAttachmentsByBl(AttachmentBlKind blKind, Long blId) {
        return blAttachmentJpaRepository
                .findByBlKindAndBlIdOrderByBlAttachmentIdDesc(blKind, blId)
                .stream()
                .map(jpaToDomainMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAttachmentById(Long id) {
        blAttachmentJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<String> deleteAttachmentsByBlAndReturnKeys(AttachmentBlKind blKind, Long blId) {
        List<String> keys = blAttachmentJpaRepository.findStorageKeysByBlKindAndBlId(blKind, blId);
        blAttachmentJpaRepository.deleteByBlKindAndBlId(blKind, blId);
        return keys;
    }

    @Override
    public List<String> findAllStorageKeys() {
        return blAttachmentJpaRepository.findAllStorageKeys();
    }
}
