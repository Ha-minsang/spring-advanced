package org.example.expert.support;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;

public class CommentFixture {
    private static final String DEFAULT_CONTENTS = "testContents";

    public static Comment createComment() {
        User user = UserFixture.createUser();
        Todo todo = TodoFixture.createTodo(user);

        return new Comment(DEFAULT_CONTENTS, user, todo);
    }
}