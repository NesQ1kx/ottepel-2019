package org.pet.social.BLL.contracts.entity;

import org.pet.social.common.entity.Problem;
import org.pet.social.common.entity.User;
import org.pet.social.common.enums.ProblemStatus;
import org.pet.social.common.enums.Resolvers;
import org.pet.social.common.exceptions.*;
import org.pet.social.common.viewmodels.AddProblemViewModel;

import java.util.Optional;

public interface ProblemServiceInterface {
     Iterable<Problem> getLimited(ProblemStatus status, Integer limit, Integer offset);
     Optional<Problem> get(Integer id);

     boolean add(User user, AddProblemViewModel model);

     boolean resolve(Integer id, Integer userId) throws ProblemNotApprovedException, ObjectNotFoundException;
     boolean approve(Integer id, Integer userId) throws ProblemShouldNotApprove, ObjectNotFoundException;

     void setResolver(Integer id, Resolvers resolver) throws ObjectNotFoundException;

     boolean moderate(Integer id, User moderator) throws NotModeratorException, ObjectNotFoundException, ShouldNotModerateException;

     Problem getProblem();
}
