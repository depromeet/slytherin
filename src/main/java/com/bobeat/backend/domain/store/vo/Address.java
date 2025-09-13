package com.bobeat.backend.domain.store.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Address {

    private String address;
    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    public void setLocation(Point point) {
        this.location = point;
        if (point != null) {
            this.latitude = point.getY();
            this.longitude = point.getX();
        }
    }
}
