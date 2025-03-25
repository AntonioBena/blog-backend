package com.job.interview.blog.model.dto.response;

import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int pageIndex;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;

    @Override
    public String toString() {
        return "PageResponse{" +
                "content=" + content +
                ", pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", last=" + last +
                ", first=" + first +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PageResponse<?> that = (PageResponse<?>) o;
        return pageIndex == that.pageIndex && pageSize == that.pageSize && totalElements == that.totalElements && totalPages == that.totalPages && last == that.last && first == that.first && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, pageIndex, pageSize, totalElements, totalPages, last, first);
    }
}