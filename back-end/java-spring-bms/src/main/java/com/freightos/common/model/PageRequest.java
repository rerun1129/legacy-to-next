package com.freightos.common.model;

import com.freightos.bms.domain.common.enums.SortDirection;

import java.util.Objects;

/** 도메인 계층용 페이징 요청 모델. Spring Pageable에 의존하지 않는다. */
public class PageRequest {

    private final int page;
    private final int size;
    private final String sortBy;
    private final SortDirection sortDirection;

    private PageRequest(int page, int size, String sortBy, SortDirection sortDirection) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, null, null);
    }

    public static PageRequest of(int page, int size, String sortBy, SortDirection sortDirection) {
        return new PageRequest(page, size, sortBy, sortDirection);
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public String getSortBy() { return sortBy; }
    public SortDirection getSortDirection() { return sortDirection; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageRequest that)) return false;
        return page == that.page && size == that.size && Objects.equals(sortBy, that.sortBy) && sortDirection == that.sortDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, sortBy, sortDirection);
    }
}
