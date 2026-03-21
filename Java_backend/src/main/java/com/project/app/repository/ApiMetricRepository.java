package com.project.app.repository;

import com.project.app.entity.ApiMetric;
import com.project.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiMetricRepository extends JpaRepository<ApiMetric, Long> {

    List<ApiMetric> findByUserIdOrderByTimestampDesc(Long userId);

    List<ApiMetric> findTop10ByOrderByTimestampDesc();

    List<ApiMetric> findByEndpointOrderByTimestampDesc(String endpoint);

    @Query("SELECT COUNT(a) FROM ApiMetric a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM ApiMetric a WHERE a.timestamp >= :since")
    long countSince(@Param("since") java.time.LocalDateTime since);

    List<ApiMetric> findByTimestampBetweenOrderByTimestampDesc(
        java.time.LocalDateTime start,
        java.time.LocalDateTime end
    );

    void deleteByUserId(Long userId);
}
