package com.neusoft.supervision.feedback.repository;

public interface ProvincePollutantExceedanceProjection {
    String getProvinceName();

    String getProvinceShortName();

    Long getPm25ExceedCount();

    Long getSo2ExceedCount();

    Long getCoExceedCount();
}
