package com.freightos.admin.common.response;

import java.util.List;
import java.util.function.Function;

/** 도메인 계층용 페이징 결과 모델. Spring Page에 의존하지 않는다. */
public class PagedResult<T> {

    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;

    private PagedResult(List<T> content, long totalElements, int totalPages, int page, int size) {
        this.content       = content;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.page          = page;
        this.size          = size;
    }

    public static <T> PagedResult<T> of(List<T> content, long totalElements, int totalPages, int page, int size) {
        return new PagedResult<>(content, totalElements, totalPages, page, size);
    }

    public List<T> getContent() { return content; }

    public long getTotalElements() { return totalElements; }

    public int getTotalPages() { return totalPages; }

    public int getPage() { return page; }

    public int getSize() { return size; }

    public <R> PagedResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PagedResult<>(content.stream().<R>map(mapper).toList(), totalElements, totalPages, page, size);
    }
}
