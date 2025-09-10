package com.bobeat.backend.global.response;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * 커서 기반 페이지네이션 응답을 위한 범용 클래스입니다.
 * 이 클래스는 단일 ID 기반의 커서를 사용하여 다음 페이지를 조회할 때 사용됩니다.
 */
@Getter
@RequiredArgsConstructor
public class CursorPageResponse<T> {

    private final List<T> data;
    private final Long nextCursor;
    private final Boolean hasNext;

    /**
     * Repository에서 pageSize + 1만큼 조회한 데이터 리스트를 받아
     * 다음 페이지 유무(hasNext)와 다음 커서(nextCursor)를 계산하여 응답 객체를 생성합니다.
     *
     * @param data        Repository에서 조회된 데이터 리스트 (pageSize + 1 크기)
     * @param pageSize    클라이언트에게 반환할 실제 페이지 크기
     * @param idExtractor 데이터 요소에서 Long 타입의 ID(커서)를 추출하는 함수
     * @param <T>         데이터 요소의 타입
     * @return 계산된 페이지네이션 정보가 포함된 응답 객체
     */
    public static <T> CursorPageResponse<T> of(List<T> data, int pageSize, ToLongFunction<T> idExtractor) {
        boolean hasNext = data.size() > pageSize;
        long nextCursor = -1L;

        if (hasNext) {
            T lastReponse = data.get(pageSize - 1);
            nextCursor = idExtractor.applyAsLong(lastReponse);

            data = data.subList(0, pageSize);
        }

        return new CursorPageResponse<>(data, nextCursor, hasNext);
    }

    public static <T> CursorPageResponse<T> empty() {
        return new CursorPageResponse<>(Collections.emptyList(), -1L, false);
    }
}
