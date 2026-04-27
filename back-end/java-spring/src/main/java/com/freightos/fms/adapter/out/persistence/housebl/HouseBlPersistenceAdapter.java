package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HouseBlPersistenceAdapter implements HouseBlPort {

    private final HouseBlRepository houseBlRepository;
    private final HouseBlMapper houseBlMapper;

    @Override
    public Optional<HouseBl> findById(UUID id) {
        return houseBlRepository.findById(id)
                .map(houseBlMapper::toDomain);
    }

    @Override
    public PagedResult<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(
            JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPage =
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage(), pageRequest.getSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<HouseBlJpaEntity> page = houseBlRepository
                .findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound, springPage);
        return toPagedResult(page);
    }

    @Override
    public PagedResult<HouseBl> findBySchedule(JobDiv jobDiv, Bound bound, LocalDate from, LocalDate to,
                                                PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPage =
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage(), pageRequest.getSize(),
                        Sort.by(Sort.Direction.ASC, "etd"));
        Page<HouseBlJpaEntity> page = houseBlRepository.findBySchedule(jobDiv, bound, from, to, springPage);
        return toPagedResult(page);
    }

    @Override
    public long countByMasterBlId(UUID masterBlId) {
        return houseBlRepository.countByMasterBlId(masterBlId);
    }

    @Override
    public HouseBl save(HouseBl houseBl) {
        HouseBlJpaEntity jpa = houseBlMapper.toJpa(houseBl);
        HouseBlJpaEntity saved = houseBlRepository.save(jpa);
        return houseBlMapper.toDomain(saved);
    }

    @Override
    public void delete(HouseBl houseBl) {
        HouseBlJpaEntity jpa = houseBlMapper.toJpa(houseBl);
        houseBlRepository.delete(jpa);
    }

    private PagedResult<HouseBl> toPagedResult(Page<HouseBlJpaEntity> page) {
        List<HouseBl> content = page.getContent().stream()
                .map(houseBlMapper::toDomain)
                .toList();
        return PagedResult.of(content, page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }
}
