package ca.corefacility.bioinformatics.irida.repositories.remote.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import ca.corefacility.bioinformatics.irida.model.remote.resource.ListResourceWrapper;
import ca.corefacility.bioinformatics.irida.model.remote.resource.ResourceWrapper;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.repositories.remote.SingleEndSequenceFileRemoteRepository;
import ca.corefacility.bioinformatics.irida.service.RemoteAPITokenService;

/**
 * Implementation of a {@link SingleEndSequenceFileRemoteRepository} extending
 * {@link RemoteRepositoryImpl}
 */
@Repository
public class SingleEndSequenceFileRemoteRepositoryImpl extends RemoteRepositoryImpl<SingleEndSequenceFile> implements
		SingleEndSequenceFileRemoteRepository {

	private static final ParameterizedTypeReference<ListResourceWrapper<SingleEndSequenceFile>> listTypeReference = new ParameterizedTypeReference<ListResourceWrapper<SingleEndSequenceFile>>() {
	};
	private static final ParameterizedTypeReference<ResourceWrapper<SingleEndSequenceFile>> objectTypeReference = new ParameterizedTypeReference<ResourceWrapper<SingleEndSequenceFile>>() {
	};

	@Autowired
	public SingleEndSequenceFileRemoteRepositoryImpl(RemoteAPITokenService tokenService) {
		super(tokenService, listTypeReference, objectTypeReference);
	}

}