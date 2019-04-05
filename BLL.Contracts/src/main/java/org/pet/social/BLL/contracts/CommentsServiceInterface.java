package org.pet.social.BLL.contracts;

import org.pet.social.common.entity.Comment;

public interface CommentsServiceInterface {
    boolean Add(String text, Integer problemId, Integer userId);
    boolean Like(Integer commentId, Integer userId);
    boolean Dislike(Integer commentId, Integer userId);
    Comment GetComment(Integer commentId);
}
