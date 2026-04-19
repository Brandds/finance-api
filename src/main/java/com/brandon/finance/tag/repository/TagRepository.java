package com.brandon.finance.tag.repository;

import com.brandon.finance.tag.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Page<Tag> findByUserId(Long userId, Pageable pageable);

    List<Tag> findByUserId(Long userId);

    Optional<Tag> findByUserIdAndName(Long userId, String name);

    List<Tag> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);
}
