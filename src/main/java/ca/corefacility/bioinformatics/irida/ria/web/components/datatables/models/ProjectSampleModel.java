package ca.corefacility.bioinformatics.irida.ria.web.components.datatables.models;

import java.util.List;

import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;

import com.google.common.collect.ImmutableList;

/**
 * Used for exporting the project samples table.  Prevents undefined from displaying in the table,
 * and adds the project name.
 */
public class ProjectSampleModel extends AbstractExportModel {
	/**
	 * Attributes on the {@link Sample}
	 */
	public static List<String> attributes = ImmutableList.of(
			"id",
			"sampleName",
			"projectName",
			"createdDate",
			"modifiedDate",
			"description",
			"organism",
			"isolate",
			"strain",
			"collectedBy",
			"collectionDate",
			"geographicLocationName",
			"isolationSource",
			"latitude",
			"longitude"
	);
	private Project project;
	private Sample sample;

	public ProjectSampleModel(ProjectSampleJoin psj) {
		this.project = psj.getSubject();
		this.sample = psj.getObject();
	}

	public static String generateSortName(String name) {
		if (name.equals("projectName")) {
			return "project.label";
		} else {
			return "sample." + name;
		}
	}

	public String getId() {
		return checkNullId(sample.getId());
	}

	public String getSampleName() {
		return checkNullStrings(sample.getSampleName());
	}

	public Long getProjectId() {
		return project.getId();
	}

	public String getProjectName() {
		return checkNullStrings(project.getName());
	}

	public String getCreatedDate() {
		return checkNullDate(sample.getCreatedDate());
	}

	public String getModifiedDate() {
		return checkNullDate(sample.getModifiedDate());
	}

	public String getDescription() {
		return checkNullStrings(sample.getDescription());
	}

	public String getOrganism() {
		return checkNullStrings(sample.getOrganism());
	}

	public String getIsolate() {
		return checkNullStrings(sample.getIsolate());
	}

	public String getCollectedBy() {
		return checkNullStrings(sample.getCollectedBy());
	}

	public String getCollectionDate() {
		return checkNullDate(sample.getCollectionDate());
	}

	public String getGeographicLocationName() {
		return checkNullStrings(sample.getGeographicLocationName());
	}

	public String getIsolationSource() {
		return checkNullStrings(sample.getIsolationSource());
	}

	public String getLatitude() {
		return checkNullStrings(sample.getLatitude());
	}

	public String getLongitude() {
		return checkNullStrings(sample.getLongitude());
	}
}
