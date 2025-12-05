package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.support.AuthUserFixture;
import org.example.expert.support.TodoFixture;
import org.example.expert.support.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    @DisplayName("매니저 생성 실패 - 매니저를 생성할 일정이 없음")
    void saveManager_ShouldThrowException_WhenNoTodosIsNull() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 생성 실패 - 일정을 생성한 유저 없음")
    void saveManager_ShouldThrowException_WhenUserIsNull() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = TodoFixture.createTodo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정을 생성한 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 생성 실패 - 일정을 생성한 유저가 아닌 유저가 매니저를 지정하려 함")
    void saveManager_ShouldThrowException_WhenUserIsNotTodoOwner() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = TodoFixture.createTodo();
        ReflectionTestUtils.setField(todo.getUser(), "id", 2L);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 생성 실패 - 일정 생성 유저가 스스로를 매니저로 지정")
    void saveManager_ShouldThrowException_WhenUserAssignsSelf() {
        // given
        AuthUser authUser = AuthUserFixture.createAuthUser();
        long todoId = 1L;
        long managerUserId = 1L;

        Todo todo = TodoFixture.createTodo();
        User managerUser = UserFixture.createUser();
        ReflectionTestUtils.setField(managerUser, "id", 1L);
        ReflectionTestUtils.setField(todo.getUser(), "id", 1L);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerSaveRequest.getManagerUserId())).willReturn(Optional.of(managerUser));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("매니저 목록 조회 성공")
    void getManagers_ShouldReturnManagerList() {
        // given
        long todoId = 1L;
        Todo todo = TodoFixture.createTodo();
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
    @DisplayName("매니저 등록 성공")
    void saveManagers_ShouldSaveManager() {
        // given
        AuthUser authUser = AuthUserFixture.createAuthUser();

        long todoId = 1L;
        Todo todo = TodoFixture.createTodo();

        long managerUserId = 2L;
        User managerUser = UserFixture.createUser();  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);
        ReflectionTestUtils.setField(authUser, "id", 1L);
        ReflectionTestUtils.setField(todo.getUser(), "id", 1L);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }
}
