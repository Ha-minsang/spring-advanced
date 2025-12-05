package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.support.AuthUserFixture;
import org.example.expert.support.TodoFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("할 일 등록 성공")
    void saveTodo_ShouldSaveTodo() {
        // given
        AuthUser authUser = AuthUserFixture.createAuthUser();
        TodoSaveRequest request = new TodoSaveRequest("testTitle", "testContents");
        String weather = "testWeather";

        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo(
                request.getTitle(),
                request.getContents(),
                weather,
                user
        );
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        // then
        assertNotNull(response);
        assertEquals(todo.getId(), response.getId());
        assertEquals(todo.getTitle(), response.getTitle());
        assertEquals(todo.getContents(), response.getContents());
        assertEquals(weather, response.getWeather());
        assertEquals(user.getId(), response.getUser().getId());
        assertEquals(user.getEmail(), response.getUser().getEmail());
    }

    @Test
    @DisplayName("할 일 목록 조회 성공")
    void getTodos_ShouldReturnTodoPage() {
        // given
        int page = 1;
        int size = 10;

        Todo todo = TodoFixture.createTodo();
        ReflectionTestUtils.setField(todo, "id", 1L);
        User user = todo.getUser();
        ReflectionTestUtils.setField(user, "id", 1L);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Todo> todoPage = new PageImpl<>(List.of(todo), pageable, 1);

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todoPage);

        // when
        Page<TodoResponse> responses = todoService.getTodos(page, size);

        // then
        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        TodoResponse response = responses.getContent().get(0);
        assertEquals(todo.getId(), response.getId());
        assertEquals(todo.getTitle(), response.getTitle());
        assertEquals(todo.getContents(), response.getContents());
        assertEquals(todo.getWeather(), response.getWeather());
    }

    @Test
    @DisplayName("할 일 단건 조회 실패 - Todo를 찾을 수 없음")
    void getTodo_ShouldThrowException_WhenTodoNotFound() {
        // given
        long todoId = 1L;

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            todoService.getTodo(todoId);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("할 일 단건 조회 성공")
    void getTodo_ShouldReturnTodo() {
        // given
        long todoId = 1L;

        Todo todo = TodoFixture.createTodo();
        ReflectionTestUtils.setField(todo, "id", todoId);
        ReflectionTestUtils.setField(todo.getUser(), "id", 1L);

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoService.getTodo(todoId);

        // then
        assertNotNull(response);
        assertEquals(todo.getId(), response.getId());
        assertEquals(todo.getTitle(), response.getTitle());
        assertEquals(todo.getContents(), response.getContents());
        assertEquals(todo.getWeather(), response.getWeather());
        assertEquals(todo.getUser().getId(), response.getUser().getId());
        assertEquals(todo.getUser().getEmail(), response.getUser().getEmail());
    }
}
