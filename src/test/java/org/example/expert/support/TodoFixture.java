package org.example.expert.support;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;

public class TodoFixture {
    private static final String DEFAULT_TITLE = "testTitle";
    private static final String DEFAULT_CONTENTS = "testContents";
    private static final String DEFAULT_WEATHER = "testWeather";

    public static Todo createTodo(User user) {
        return new Todo(DEFAULT_TITLE, DEFAULT_CONTENTS, DEFAULT_WEATHER, user);
    }

    public static Todo createTodo() {
        User user = UserFixture.createUser();
        return new Todo(DEFAULT_TITLE, DEFAULT_CONTENTS, DEFAULT_WEATHER, user);
    }
}
