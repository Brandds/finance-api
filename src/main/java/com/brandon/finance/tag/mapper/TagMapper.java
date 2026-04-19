package com.brandon.finance.tag.mapper;

import com.brandon.finance.tag.dto.TagDTO;
import com.brandon.finance.tag.entity.Tag;
import com.brandon.finance.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagDTO toDTO(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setUserId(tag.getUser() != null ? tag.getUser().getId() : null);

        return dto;
    }

    public Tag toEntity(TagDTO dto, User user) {
        if (dto == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setUser(user);

        return tag;
    }
}
