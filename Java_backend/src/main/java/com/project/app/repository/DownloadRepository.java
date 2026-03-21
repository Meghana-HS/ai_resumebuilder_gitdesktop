package com.project.app.repository;

import com.project.app.entity.Download;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    List<Download> findByUserIdOrderByDownloadDateDesc(Long userId);

    Page<Download> findByUserIdOrderByDownloadDateDesc(Long userId, Pageable pageable);

    List<Download> findByUserIdAndActionOrderByDownloadDateDesc(Long userId, Download.Action action);

    List<Download> findByUserIdAndTypeOrderByDownloadDateDesc(Long userId, Download.DocumentType type);

    @Query("SELECT d FROM Download d WHERE d.user.id = :userId ORDER BY d.downloadDate DESC")
    List<Download> findRecentActivity(@Param("userId") Long userId);

    @Query("SELECT COUNT(d) FROM Download d WHERE d.user.id = :userId AND d.action = :action")
    Long countByUserIdAndAction(@Param("userId") Long userId, @Param("action") Download.Action action);

    @Query("SELECT COUNT(d) FROM Download d WHERE d.user.id = :userId AND d.type = :type AND d.action = :action")
    Long countByUserIdAndTypeAndAction(@Param("userId") Long userId, @Param("type") Download.DocumentType type, @Param("action") Download.Action action);

    void deleteByUserId(Long userId);
}
