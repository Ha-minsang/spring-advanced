package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.AuthUserFixture;
import org.example.expert.support.CommentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글 등록 실패 - 등록할 일정이 없음")
    void createComment_ShouldThrowException_WhenTodoNotFound() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("댓글 등록 성공")
    void saveComment_ShouldSaveComment() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("testContents");
        AuthUser authUser = AuthUserFixture.createAuthUser();
        Comment comment = CommentFixture.createComment();
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(comment.getTodo()));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse response = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(response);
        assertEquals(comment.getId(), response.getId());
        assertEquals(comment.getContents(), response.getContents());
    }

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_ShouldGetComment() {
        // given
        long todoId = 1L;
        Comment comment = CommentFixture.createComment();
        ReflectionTestUtils.setField(comment, "id", 1L);
        List<Comment> commentList = List.of(comment);

        given(commentRepository.findByTodoIdWithUser(anyLong())).willReturn(commentList);

        // when
        List<CommentResponse> responses = commentService.getComments(todoId);

        // then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(comment.getId(), responses.get(0).getId());
        assertEquals(comment.getContents(), responses.get(0).getContents());
    }
}
