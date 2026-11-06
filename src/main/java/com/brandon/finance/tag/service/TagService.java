package com.brandon.finance.tag.service;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.tag.dto.TagDTO;
import com.brandon.finance.tag.entity.Tag;
import com.brandon.finance.tag.mapper.TagMapper;
import com.brandon.finance.tag.repository.TagRepository;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
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
    private final TagMapper tagMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public TagService(TagRepository tagRepository, TagMapper tagMapper, AuthenticatedUserService authenticatedUserService) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public TagDTO create(TagDTO dto) {
        User user = authenticatedUserService.getUser();

        Tag tag = tagMapper.toEntity(dto, user);
        Tag saved = tagRepository.save(tag);

        return tagMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public TagDTO getById(Long id) {
        return tagMapper.toDTO(getOwnedTag(id));
    }

    @Transactional(readOnly = true)
    public Page<TagDTO> getByUserId(Pageable pageable) {
        Page<Tag> tags = tagRepository.findByUserId(authenticatedUserService.getUserId(), pageable);
        return new PageImpl<>(
                tags.getContent().stream().map(tagMapper::toDTO).collect(Collectors.toList()),
                pageable,
                tags.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getAll() {
        List<Tag> tags = tagRepository.findByUserId(authenticatedUserService.getUserId());
        return tags.stream().map(tagMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TagDTO getByName(String name) {
        Tag tag = tagRepository.findByUserIdAndName(authenticatedUserService.getUserId(), name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag não encontrada"));
        return tagMapper.toDTO(tag);
    }

    @Transactional(readOnly = true)
    public List<TagDTO> searchByName(String nameContains) {
        List<Tag> tags = tagRepository.findByUserIdAndNameContainingIgnoreCase(authenticatedUserService.getUserId(), nameContains);
        return tags.stream().map(tagMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public TagDTO update(Long id, TagDTO dto) {
        Tag tag = getOwnedTag(id);

        tag.setName(dto.getName());
        Tag updated = tagRepository.save(tag);

        return tagMapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        tagRepository.delete(getOwnedTag(id));
    }

    private Tag getOwnedTag(Long id) {
        return tagRepository.findByIdAndUserId(id, authenticatedUserService.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Tag não encontrada"));
    }
}
