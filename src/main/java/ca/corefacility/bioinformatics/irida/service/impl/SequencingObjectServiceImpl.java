package ca.corefacility.bioinformatics.irida.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.corefacility.bioinformatics.irida.events.annotations.LaunchesProjectEvent;
import ca.corefacility.bioinformatics.irida.exceptions.DuplicateSampleException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.event.DataAddedToSampleProjectEvent;
import ca.corefacility.bioinformatics.irida.model.run.SequencingRun;
import ca.corefacility.bioinformatics.irida.model.run.SequencingRun.LayoutType;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequencingObjectJoin;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequencingObject;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.processing.FileProcessingChain;
import ca.corefacility.bioinformatics.irida.repositories.joins.sample.SampleSequencingObjectJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.sequencefile.SequenceFileRepository;
import ca.corefacility.bioinformatics.irida.repositories.sequencefile.SequencingObjectRepository;
import ca.corefacility.bioinformatics.irida.repositories.specification.SampleSequencingObjectSpecification;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.impl.processor.SequenceFileProcessorLauncher;

/**
 * Implementation of {@link SequencingObjectService} using a
 * {@link SequencingObjectRepository} and
 * {@link SampleSequencingObjectJoinRepository} to persist and load objects.
 */
@Service
public class SequencingObjectServiceImpl extends CRUDServiceImpl<Long, SequencingObject> implements
		SequencingObjectService {

	private final SampleSequencingObjectJoinRepository ssoRepository;
	private final SequenceFileRepository sequenceFileRepository;
	private TaskExecutor fileProcessingChainExecutor;
	private FileProcessingChain fileProcessingChain;
	private final SequencingObjectRepository repository;

	@Autowired
	public SequencingObjectServiceImpl(SequencingObjectRepository repository,
			SequenceFileRepository sequenceFileRepository, SampleSequencingObjectJoinRepository ssoRepository,
			@Qualifier("fileProcessingChainExecutor") TaskExecutor executor, FileProcessingChain fileProcessingChain,
			Validator validator) {
		super(repository, validator, SequencingObject.class);
		this.repository = repository;
		this.ssoRepository = ssoRepository;
		this.fileProcessingChainExecutor = executor;
		this.fileProcessingChain = fileProcessingChain;
		this.sequenceFileRepository = sequenceFileRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SEQUENCER') or hasPermission(#id, 'canReadSequencingObject')")
	@Override
	public SequencingObject read(Long id) throws EntityNotFoundException {
		return super.read(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_SEQUENCER', 'ROLE_USER')")
	public SequencingObject create(SequencingObject object) throws ConstraintViolationException, EntityExistsException {

		SequencingRun sequencingRun = object.getSequencingRun();
		if (sequencingRun != null) {
			if (object instanceof SingleEndSequenceFile && sequencingRun.getLayoutType() != LayoutType.SINGLE_END) {
				throw new IllegalArgumentException("Attempting to add a single end file to a non single end run");
			} else if (object instanceof SequenceFilePair && sequencingRun.getLayoutType() != LayoutType.PAIRED_END) {
				throw new IllegalArgumentException("Attempting to add a paired end file to a non paired end run");
			}
		}

		for (SequenceFile file : object.getFiles()) {
			file = sequenceFileRepository.save(file);
		}

		SequencingObject so = super.create(object);
		fileProcessingChainExecutor.execute(new SequenceFileProcessorLauncher(fileProcessingChain, so.getId(),
				SecurityContextHolder.getContext()));
		return so;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SEQUENCER') or hasPermission(#sample, 'canReadSample')")
	@LaunchesProjectEvent(DataAddedToSampleProjectEvent.class)
	public SampleSequencingObjectJoin createSequencingObjectInSample(SequencingObject seqObject, Sample sample) {
		// create the sequencing object
		seqObject = create(seqObject);

		// save the new join
		SampleSequencingObjectJoin sampleSequencingObjectJoin = new SampleSequencingObjectJoin(sample, seqObject);
		return ssoRepository.save(sampleSequencingObjectJoin);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SEQUENCER') or hasPermission(#sample, 'canReadSample')")
	public Collection<SampleSequencingObjectJoin> getSequencingObjectsForSample(Sample sample) {
		return ssoRepository.getSequencesForSample(sample);
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SEQUENCER') or hasPermission(#sample, 'canReadSample')")
	@Override
	public Collection<SampleSequencingObjectJoin> getSequencesForSampleOfType(Sample sample,
			Class<? extends SequencingObject> type) {

		return ssoRepository.findAll(SampleSequencingObjectSpecification.getSequenceOfTypeForSample(sample, type));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SEQUENCER') or hasPermission(#sample, 'canReadSample')")
	public SequencingObject readSequencingObjectForSample(Sample sample, Long objectId) {
		SampleSequencingObjectJoin readObjectForSample = ssoRepository.readObjectForSample(sample, objectId);

		return readObjectForSample.getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN') or hasPermission(#sequenceFiles, 'canReadSequencingObject')")
	@Override
	public <T extends SequencingObject> Map<Sample, T> getUniqueSamplesForSequencingObjects(Set<T> sequenceFiles)
			throws DuplicateSampleException {
		Map<Sample, T> sequenceFilePairsSampleMap = new HashMap<>();

		for (T filePair : sequenceFiles) {
			SequenceFile pair1 = filePair.getFiles().iterator().next();

			SampleSequencingObjectJoin join = ssoRepository.getSampleForSequencingObject(filePair);

			Sample sample = join.getSubject();
			if (sequenceFilePairsSampleMap.containsKey(sample)) {
				SequencingObject previousPair = sequenceFilePairsSampleMap.get(sample);
				throw new DuplicateSampleException("Sequence file pairs " + pair1 + ", " + previousPair
						+ " have the same sample " + sample);
			} else {
				sequenceFilePairsSampleMap.put(sample, filePair);
			}
		}

		return sequenceFilePairsSampleMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SEQUENCER')")
	public Set<SequencingObject> getSequencingObjectsForSequencingRun(SequencingRun sequencingRun) {
		return repository.findSequencingObjectsForSequencingRun(sequencingRun);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#idents, 'canReadSequencingObject')")
	public Iterable<SequencingObject> readMultiple(Iterable<Long> idents) {
		return super.readMultiple(idents);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasPermission(#id, 'canReadSequencingObject')")
	public Boolean exists(Long id) {
		return super.exists(id);
	}

}
