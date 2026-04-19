package com.brandon.finance.tag.service;

import com.brandon.finance.tag.dto.TagDTO;
import com.brandon.finance.tag.entity.Tag;
import com.brandon.finance.tag.mapper.TagMapper;
import com.brandon.finance.tag.repository.TagRepository;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, UserRepository userRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional
    public TagDTO create(TagDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Tag tag = tagMapper.toEntity(dto, user);
        Tag saved = tagRepository.save(tag);

        return tagMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public TagDTO getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + id));
        return tagMapper.toDTO(tag);
    }

    @Transactional(readOnly = true)
    public Page<TagDTO> getByUserId(Long userId, Pageable pageable) {
        Page<Tag> tags = tagRepository.findByUserId(userId, pageable);
        return new PageImpl<>(
                tags.getContent().stream().map(tagMapper::toDTO).collect(Collectors.toList()),
                pageable,
                tags.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getByUserIdList(Long userId) {
        List<Tag> tags = tagRepository.findByUserId(userId);
        return tags.stream().map(tagMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TagDTO getByName(Long userId, String name) {
        Tag tag = tagRepository.findByUserIdAndName(userId, name)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + name));
        return tagMapper.toDTO(tag);
    }

    @Transactional(readOnly = true)
    public List<TagDTO> searchByName(Long userId, String nameContains) {
        List<Tag> tags = tagRepository.findByUserIdAndNameContainingIgnoreCase(userId, nameContains);
        return tags.stream().map(tagMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public TagDTO update(Long id, TagDTO dto, Long userId) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + id));

        if (!tag.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Tag does not belong to user " + userId);
        }

        tag.setName(dto.getName());
        Tag updated = tagRepository.save(tag);

        return tagMapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + id));

        if (!tag.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Tag does not belong to user " + userId);
        }

        tagRepository.deleteById(id);
    }
}
