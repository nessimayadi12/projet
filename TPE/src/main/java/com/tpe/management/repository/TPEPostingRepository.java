package com.tpe.management.repository;

import com.tpe.management.entity.TPEPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TPEPostingRepository extends JpaRepository<TPEPosting, Long> {
}
