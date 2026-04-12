package com.brandon.finance.expense_tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brandon.finance.tag.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
    
}
