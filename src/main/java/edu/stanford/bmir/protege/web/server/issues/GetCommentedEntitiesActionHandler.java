package edu.stanford.bmir.protege.web.server.issues;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractHasProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.pagination.Pager;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.issues.GetCommentedEntitiesAction;
import edu.stanford.bmir.protege.web.shared.issues.GetCommentedEntitiesResult;
import edu.stanford.bmir.protege.web.shared.pagination.Page;
import edu.stanford.bmir.protege.web.shared.pagination.PageRequest;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Mar 2017
 */
public class GetCommentedEntitiesActionHandler extends AbstractHasProjectActionHandler<GetCommentedEntitiesAction, GetCommentedEntitiesResult> {

    @Nonnull
    private final EntityDiscussionThreadRepository repository;

    @Inject
    public GetCommentedEntitiesActionHandler(@Nonnull ProjectManager projectManager,
                                             AccessManager accessManager,
                                             @Nonnull EntityDiscussionThreadRepository repository) {
        super(projectManager, accessManager);
        this.repository = repository;
    }

    @Override
    public Class<GetCommentedEntitiesAction> getActionClass() {
        return GetCommentedEntitiesAction.class;
    }

    @Override
    protected GetCommentedEntitiesResult execute(GetCommentedEntitiesAction action,
                                                 Project project,
                                                 ExecutionContext executionContext) {
        PageRequest request = action.getPageRequest();
        List<OWLEntity> commentedEntities = repository.getCommentedEntities(action.getProjectId());
        List<OWLEntityData> entityData = commentedEntities.stream()
                         .map(entity -> project.getRenderingManager().getRendering(entity))
                         .sorted()
//                         .skip((request.getPageNumber() - 1) * request.getPageSize())
//                         .limit(request.getPageSize())
                         .collect(toList());
        Pager<OWLEntityData> pager = Pager.getPagerForPageSize(entityData, request.getPageSize());
        return new GetCommentedEntitiesResult(action.getProjectId(), pager.getPage(request.getPageNumber()));
    }
}
