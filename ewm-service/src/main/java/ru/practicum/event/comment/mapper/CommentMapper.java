package ru.practicum.event.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.event.comment.model.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class, EventMapper.class})
public interface CommentMapper {

    Comment toComment(NewCommentRequest newCommentRequest);

    @Mapping(source = "author.name", target = "authorName")
    CommentDto toDto(Comment comment);

    @Mapping(target = "text", source = "text")
    Comment update(UpdateCommentRequest updateCommentRequest, @MappingTarget Comment comment);

    CommentFullDto toFullDto(Comment comment);
}