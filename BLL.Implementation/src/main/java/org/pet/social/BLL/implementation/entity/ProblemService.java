package org.pet.social.BLL.implementation.entity;

import org.pet.social.BLL.contracts.entity.ProblemServiceInterface;
import org.pet.social.BLL.implementation.PhotoService;
import org.pet.social.DAL.contracts.ProblemInterface;
import org.pet.social.common.entity.Problem;
import org.pet.social.common.entity.ProblemUserApprove;
import org.pet.social.common.entity.User;
import org.pet.social.common.enums.ProblemStatus;
import org.pet.social.common.enums.Resolvers;
import org.pet.social.common.exceptions.*;
import org.pet.social.common.utils.CategoryClassifier;
import org.pet.social.common.viewmodels.AddProblemViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class ProblemService implements ProblemServiceInterface {
    @Autowired
    private ProblemInterface problems;
    @Autowired
    private PhotoService photos;
    @Autowired
    private ProblemUserApproveService puas;

    private Problem problem;

    @Override
    public Page<Problem> getLimited(ProblemStatus notStatus, Pageable pageable) {
       return problems.findByStatusNot(notStatus, pageable);
    }

    @Override
    public Optional<Problem> get(Integer id) {
        return problems.findById(id);
    }


    @Override
    public boolean add(User user, AddProblemViewModel model) {
        Problem problem = new Problem();
        problem.setStatus(ProblemStatus.NOT_CONFIRMED);
        problem.setText(model.getText());
        problem.setTitle(model.getTitle());
        problem.setUserId(user.getId());
        problem.setGovernmentStructure(CategoryClassifier.classify(model.getText(), model.getTitle()));
        problem.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        problem.setLat(model.getLat());
        problem.setLon(model.getLon());

        this.problem = problem;
        return problems.save(problem) != null;
    }

    @Override
    public boolean resolve(Integer id, Integer userId) throws ProblemNotApprovedException, ObjectNotFoundException {
        Optional<Problem> problem = problems.findById(id);

        if(!problem.isPresent()) {
            throw new ObjectNotFoundException("Проблема не найдена");
        }

        Problem readyproblem = problem.get();

        if (readyproblem.getStatus() != ProblemStatus.CONFIRMED) {
            throw new ProblemNotApprovedException("Проблема не была подтверждена");
        }

        List<ProblemUserApprove> approves = puas.GetApprovesByIdAndStatus(id, ProblemStatus.RESOLVED);

        readyproblem.setApproveCount(approves.size() + 1);

        if(readyproblem.getApproveCount() < 5){
            if(puas.Resolve(id, userId)){
                problems.save(readyproblem);
            }
            return true;
        }

        readyproblem.setApproveCount(0);
        readyproblem.setStatus(ProblemStatus.RESOLVED);

        return problems.save(readyproblem) != null;
    }

    @Override
    public boolean approve(Integer id, Integer userId) throws ProblemShouldNotApprove, ObjectNotFoundException {
        Optional<Problem> problem = problems.findById(id);

        if(!problem.isPresent()) {
            throw new ObjectNotFoundException("Проблема не найдена");
        }

        Problem readyproblem = problem.get();

        if(readyproblem.getStatus() != ProblemStatus.NOT_CONFIRMED) {
            throw new ProblemShouldNotApprove("Проблему не надо подтверждать");
        }

        List<ProblemUserApprove> approves = puas.GetApprovesByIdAndStatus(id, ProblemStatus.CONFIRMED);

        readyproblem.setApproveCount(approves.size() + 1);

        if(readyproblem.getApproveCount() < 5){
            if(puas.Approve(id, userId)){
                problems.save(readyproblem);
            }
            return true;
        }

        readyproblem.setApproveCount(0);
        readyproblem.setStatus(ProblemStatus.CONFIRMED);

        return problems.save(readyproblem) != null;
    }

    /**
     * Для того, на, чтобы установить кто же зарезолвил сию проблему на
     * @param id
     * @param resolver
     * @throws ObjectNotFoundException
     */
    @Override
    public void setResolver(Integer id, Resolvers resolver) throws ObjectNotFoundException {
        Optional<Problem> problem = problems.findById(id);

        if(!problem.isPresent()) {
            throw new ObjectNotFoundException("Проблема не найдена");
        }

        Problem readyproblem = problem.get();

        readyproblem.setResolver(resolver);

        problems.save(readyproblem);
    }

    @Override
    public boolean moderate(Integer id, User moderator) throws NotModeratorException, ObjectNotFoundException, ShouldNotModerateException {
        if(!moderator.canModerate()) {
            throw new NotModeratorException("Пользователь не модератор");
        }

        Optional<Problem> targetProblem = problems.findById(id);

        if(!targetProblem.isPresent()) {
            throw new ObjectNotFoundException("Пробелма для модерации не обнаружена");
        }

        Problem problem = targetProblem.get();

        if(problem.getStatus() != ProblemStatus.MODERATION) { // FIXME: change to moderated - status
            throw new ShouldNotModerateException("Проблема не нуждается в модерации ");
        }

        problem.setStatus(ProblemStatus.NOT_CONFIRMED);


        return problems.save(problem) != null;
    }

    public Problem getProblem() {
        return  this.problem;
    }
}
