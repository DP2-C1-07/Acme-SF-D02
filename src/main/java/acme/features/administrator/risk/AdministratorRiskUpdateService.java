
package acme.features.administrator.risk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.accounts.Administrator;
import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;
import acme.entities.risk.Risk;

@Service
public class AdministratorRiskUpdateService extends AbstractService<Administrator, Risk> {

	// Internal state ---------------------------------------------------------
	@Autowired
	private AdministratorRiskRepository repository;


	// AbstractService interface ----------------------------------------------
	@Override
	public void authorise() {
		boolean status = super.getRequest().getPrincipal().hasRole(Administrator.class);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void bind(final Risk object) {
		assert object != null;

		super.bind(object, "reference", "identificationDate", "impact", "probability", "description", "link");
	}

	@Override
	public void load() {
		Risk object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.repository.findOneById(id);

		super.getBuffer().addData(object);
	}

	@Override
	public void validate(final Risk object) {
		assert object != null;

		if (!super.getBuffer().getErrors().hasErrors("reference")) {
			Risk existing;

			existing = this.repository.findOneRiskByReference(object.getReference());
			object.getReference();

			if (existing != null)
				super.state(existing.getId() == object.getId(), "reference", "administrator.risk.create.error.duplicated");
		}
	}

	@Override
	public void perform(final Risk object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Risk object) {
		assert object != null;

		Dataset dataset;
		dataset = super.unbind(object, "reference", "identificationDate", "impact", "probability", "description", "link");

		super.getResponse().addData(dataset);
	}
}
