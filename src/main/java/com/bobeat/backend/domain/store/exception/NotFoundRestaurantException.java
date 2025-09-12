package com.bobeat.backend.domain.store.exception;

import static com.bobeat.backend.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;

import com.bobeat.backend.global.exception.CustomException;

public class NotFoundRestaurantException extends CustomException {
    public NotFoundRestaurantException() {
        super(NOT_FOUND_RESTAURANT);
    }
}