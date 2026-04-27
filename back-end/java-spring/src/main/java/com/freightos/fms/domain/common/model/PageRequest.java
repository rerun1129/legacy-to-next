package com.freightos.fms.domain.common.model;

/** 도메인 계층용 페이징 요청 모델. Spring Pageable에 의존하지 않는다. */
public class PageRequest {

    private final int page;
    private final int size;

    private PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
