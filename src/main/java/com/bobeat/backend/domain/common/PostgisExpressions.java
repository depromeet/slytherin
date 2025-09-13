package com.bobeat.backend.domain.common;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.NumberExpression;

public class PostgisExpressions {
    private PostgisExpressions() {}

    /** 반경 검색: location 이 center(lon,lat) 로부터 radius(m) 이내인지 */
    public static BooleanExpression stDWithin(Path<?> geographyPoint,
                                              double centerLat,
                                              double centerLon,
                                              double radiusMeters) {
        return Expressions.booleanTemplate(
                "function('ST_DWithin', {0}, " +
                        "  function('geography', function('ST_SetSRID', function('ST_MakePoint', {1}, {2}), 4326))," +
                        "  {3}" +
                        ") = true",
                geographyPoint, centerLon, centerLat, radiusMeters
        );
    }

    /** BBox 검색: (nwLat,nwLon)-(seLat,seLon) 범위 내인지 (빠른 bbox 교차 검사) */
    public static BooleanExpression intersectsEnvelope(Path<?> geographyPoint,
                                                       double nwLat, double nwLon,
                                                       double seLat, double seLon) {
        return Expressions.booleanTemplate(
                "function('ST_Intersects', " +
                        "  function('geometry', {0}), " +
                        "  function('ST_MakeEnvelope', {1}, {2}, {3}, {4}, 4326)" +
                        ") = true",
                geographyPoint, nwLon, seLat, seLon, nwLat
        );
    }

    /** 거리 계산(미터) */
    public static NumberExpression<Integer> distanceMeters(Path<?> geographyPoint,
                                                           double centerLat,
                                                           double centerLon) {
        return Expressions.numberTemplate(
                Integer.class,
                "CAST(" +
                        "  function('ST_Distance', {0}, " +
                        "    function('geography', function('ST_SetSRID', function('ST_MakePoint', {1}, {2}), 4326))" +
                        "  ) AS integer" +
                        ")",
                geographyPoint, centerLon, centerLat
        );
    }
}
