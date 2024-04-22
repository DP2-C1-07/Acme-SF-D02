
package acme.features.manager.project;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.accounts.Principal;
import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;
import acme.entities.audit_records.AuditRecord;
import acme.entities.codeaudits.CodeAudit;
import acme.entities.contract.Contract;
import acme.entities.progressLog.ProgressLog;
import acme.entities.projects.Project;
import acme.entities.projects.ProjectUserStory;
import acme.entities.risk.Risk;
import acme.entities.sponsorships.Sponsorship;
import acme.entities.userstories.UserStory;
import acme.features.manager.userstory.ManagerUserStoryRepository;
import acme.roles.Manager;

@Service
public class ManagerProjectDeleteService extends AbstractService<Manager, Project> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private ManagerProjectRepository	managerProjectRepository;

	@Autowired
	private ManagerUserStoryRepository	managerUserStoryRepository;


	// AbstractService interface ----------------------------------------------
	@Override
	public void authorise() {
		boolean status;
		int projectId;
		Manager manager;
		Project project;

		projectId = super.getRequest().getData("id", int.class);
		project = this.managerProjectRepository.findOneProjectById(projectId);

		Principal principal = super.getRequest().getPrincipal();
		manager = this.managerProjectRepository.findManagerById(principal.getActiveRoleId());

		status = project != null && project.isDraftMode() && super.getRequest().getPrincipal().hasRole(manager) && project.getManager().equals(manager);

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void bind(final Project object) {
		assert object != null;

		super.bind(object, "code", "title", "projectAbstract", "indication", "cost", "link");
	}

	@Override
	public void load() {
		Project object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.managerProjectRepository.findOneProjectById(id);

		super.getBuffer().addData(object);
	}

	@Override
	public void validate(final Project object) {
		assert object != null;
	}

	@Override
	public void perform(final Project object) {
		assert object != null;
		// assert object.isDraftMode();
		int projectId;
		projectId = super.getRequest().getData("id", int.class);

		Collection<ProjectUserStory> projectUserStories;
		projectUserStories = this.managerProjectRepository.findAllProjectUserStoriesByProjectId(projectId);
		this.managerProjectRepository.deleteAll(projectUserStories);

		Collection<CodeAudit> codeAudits = this.managerProjectRepository.findAllCodeAuditsFromProjectId(projectId);
		for (CodeAudit codeAudit : codeAudits) {
			Collection<AuditRecord> auditRecords = this.managerProjectRepository.findAllAuditRecordsFromCodeAuditId(codeAudit.getId());
			this.managerProjectRepository.deleteAll(auditRecords);
		}

		this.managerProjectRepository.deleteAll(codeAudits);

		Collection<Contract> contracts = this.managerProjectRepository.findAllContractsByProjectId(projectId);
		for (Contract contract : contracts) {
			Collection<ProgressLog> progressLogs = this.managerProjectRepository.findAllProgressLogsByContractId(contract.getId());
			this.managerProjectRepository.deleteAll(progressLogs);
		}
		this.managerProjectRepository.deleteAll(contracts);

		Collection<Risk> risks = this.managerProjectRepository.findAllRisksByProjectId(projectId);
		this.managerProjectRepository.deleteAll(risks);

		Collection<Sponsorship> sponsorships = this.managerProjectRepository.findAllSponsorshipsByProjectId(projectId);
		this.managerProjectRepository.deleteAll(sponsorships);

		Collection<UserStory> userStories;
		userStories = this.managerUserStoryRepository.findAllUserStoriesByProjectId(projectId);
		this.managerUserStoryRepository.deleteAll(userStories);

		this.managerProjectRepository.delete(object);
	}

	@Override
	public void unbind(final Project object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbind(object, "code", "title", "projectAbstract", "indication", "cost", "link");
		super.getResponse().addData(dataset);
	}
}
