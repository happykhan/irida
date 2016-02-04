package ca.corefacility.bioinformatics.irida.ria.integration.projects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectSamplesPage;

import com.github.springtestdbunit.annotation.DatabaseSetup;

/**
 * <p>
 * Integration test to ensure that the Project Details Page.
 * </p>
 *
 */
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/projects/ProjectSamplesView.xml")
public class ProjectSamplesPageIT extends AbstractIridaUIITChromeDriver {
	private static final Logger logger = LoggerFactory.getLogger(ProjectSamplesPageIT.class);

	@Before
	public void init() {
		LoginPage.loginAsManager(driver());
	}

	@Test(expected = AssertionError.class)
	public void testGoingToInvalidPage() {
		logger.debug("Testing going to an invalid sample id");
		ProjectSamplesPage.gotToPage(driver(), 100);
	}

	@Test
	public void testPageSetUp() {
		logger.info("Testing page set up for: Project Samples");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		assertTrue("Should have the project name as the page main header.", page.getTitle().equals("project"));
		assertEquals("Should display 10 projects initially.", 10, page.getNumberProjectsDisplayed());

		// Test the status of all buttons.
		assertFalse("Merge Button should be disabled", page.isMergeBtnEnabled());
		assertFalse("Copy Button should be disabled", page.isCopyBtnEnabled());
		assertFalse("Move Button should be disabled", page.isMoveBtnEnabled());
		assertFalse("Remove Button should be disabled", page.isRemoveBtnEnabled());

		// Test toolbar when changes selected sample count to 1.
		page.selectSample(0);
		assertFalse("Merge Button should be disabled", page.isMergeBtnEnabled());
		assertTrue("Copy Button should be enabled", page.isCopyBtnEnabled());
		assertTrue("Move Button should be enabled", page.isMoveBtnEnabled());
		assertTrue("Remove Button should be enabled", page.isRemoveBtnEnabled());

		// Test toolbar when changes selected sample count to 2.
		page.selectSample(1);
		assertTrue("Merge Button should be enabled", page.isMergeBtnEnabled());
		assertTrue("Copy Button should be enabled", page.isCopyBtnEnabled());
		assertTrue("Move Button should be enabled", page.isMoveBtnEnabled());
		assertTrue("Remove Button should be enabled", page.isRemoveBtnEnabled());

		// Need to ensure they return to there default state when unchecked
		page.selectSample(1);
		assertFalse("Merge Button should be disabled", page.isMergeBtnEnabled());
		assertTrue("Copy Button should be enabled", page.isCopyBtnEnabled());
		assertTrue("Move Button should be enabled", page.isMoveBtnEnabled());
		assertTrue("Remove Button should be enabled", page.isRemoveBtnEnabled());
		page.selectSample(0);
		assertFalse("Merge Button should be disabled", page.isMergeBtnEnabled());
		assertFalse("Copy Button should be disabled", page.isCopyBtnEnabled());
		assertFalse("Move Button should be disabled", page.isMoveBtnEnabled());
		assertFalse("Remove Button should be disabled", page.isRemoveBtnEnabled());

		// Test select all/none
		page.selectAllOrNone();
		assertTrue("Merge Button should be enabled", page.isMergeBtnEnabled());
		assertTrue("Copy Button should be enabled", page.isCopyBtnEnabled());
		assertTrue("Move Button should be enabled", page.isMoveBtnEnabled());
		assertTrue("Remove Button should be enabled", page.isRemoveBtnEnabled());
		page.selectAllOrNone();
		assertFalse("Merge Button should be disabled", page.isMergeBtnEnabled());
		assertFalse("Copy Button should be disabled", page.isCopyBtnEnabled());
		assertFalse("Move Button should be disabled", page.isMoveBtnEnabled());
		assertFalse("Remove Button should be disabled", page.isRemoveBtnEnabled());

	}

	@Test
	public void testPaging() {
		logger.info("Testing paging for: Project Samples");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);

		assertFalse("'Previous' button should be disabled", page.isPreviousBtnEnabled());
		assertTrue("'Next' button should be enabled", page.isNextBtnEnabled());
		assertEquals("Should be 3 pages of samples", 3, page.getPaginationCount());
	}

	@Test
	public void testSampleSelection() {
		logger.info("Testing sample selection for: Project Samples");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		assertEquals("Should be 0 selected samples", "No samples selected", page.getSelectedInfoText());

		page.selectSample(0);
		page.selectSampleWithShift(4);
		assertEquals("Should be 5 selected samples", "5 Samples Selected", page.getSelectedInfoText());

		page.selectAllOrNone();
		// Again, this is only the count for the current page!
		assertEquals("Should be 21 selected samples", "21 Samples Selected", page.getSelectedInfoText());
		page.selectAllOrNone();
		assertEquals("Should be 0 selected samples", "No samples selected", page.getSelectedInfoText());

	}

	@Test
	public void testAddSamplesToCart() {
		logger.info("Testing adding samples to the global cart.");
		ProjectSamplesPage page = ProjectSamplesPage.gotToPage(driver(), 1);
		page.selectSample(0);
		page.selectSampleWithShift(4);
		assertEquals("Should be 5 selected samples", "5 Samples Selected", page.getSelectedInfoText());

		page.addSelectedSamplesToCart();
		assertEquals("Should be 5 samples in the cart", 5, page.getCartCount());

	}
	//	@Test
	//	public void testDefaultMerge() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//		assertEquals(0, page.getTotalSelectedSamplesCount());
	//
	//		page.selectSampleByRow(0);
	//		page.selectSampleByRow(1);
	//		assertEquals(2, page.getTotalSelectedSamplesCount());
	//		assertTrue(page.isBtnEnabled("samplesOptionsBtn"));
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("mergeBtn");
	//		assertTrue(page.isItemVisible("merge-samples-modal"));
	//		page.clickBtn("confirmMergeBtn");
	//		assertTrue(page.checkSuccessNotification());
	//		assertEquals(0, page.getTotalSelectedSamplesCount());
	//	}
	//
	//	@Test
	//	public void testRenameMerge() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//		assertEquals(0, page.getTotalSelectedSamplesCount());
	//
	//		page.selectSampleByRow(0);
	//		page.selectSampleByRow(1);
	//		assertEquals(2, page.getTotalSelectedSamplesCount());
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("mergeBtn");
	//		assertTrue(page.isItemVisible("merge-samples-modal"));
	//
	//		// Try entering a name that is too short
	//		assertTrue(page.isBtnEnabled("confirmMergeBtn"));
	//		page.enterNewMergeSampleName("HI");
	//		assertTrue(page.isItemVisible("merge-length-error"));
	//		assertFalse(page.isBtnEnabled("confirmMergeBtn"));
	//
	//		// Try entering a name with spaces
	//		page.enterNewMergeSampleName("HI BOB I AM WRONG");
	//		assertTrue(page.isItemVisible("merge-format-error"));
	//		assertFalse(page.isBtnEnabled("confirmMergeBtn"));
	//
	//		// Try to enter a proper name name
	//		String oriName = page.getSampleNameByRow(0);
	//		String newLongName = "LONGERNAME";
	//		page.enterNewMergeSampleName(newLongName);
	//		assertFalse(page.isItemVisible("merge-length-error"));
	//		assertFalse(page.isItemVisible("merge-format-error"));
	//		assertTrue(page.isBtnEnabled("confirmMergeBtn"));
	//		page.clickBtn("confirmMergeBtn");
	//		assertTrue(page.checkSuccessNotification());
	//		String updatedName = page.getSampleNameByRow(0);
	//		assertFalse(oriName.equals(updatedName));
	//		assertTrue(updatedName.equals(newLongName));
	//	}
	//
	//	@Test
	//	public void testProjectUserCannotCopyOrMoveFilesToAnotherProject() {
	//		LoginPage.loginAsUser(driver());
	//		page.goToPage();
	//		assertFalse(page.isElementOnScreen("copyBtn"));
	//		assertFalse(page.isElementOnScreen("moveBtn"));
	//	}
	//
	//	@Test
	//	public void testCopySamplesAsManagerToManagedProject() {
	//		LoginPage.login(driver(), "project1Manager", "Password1");
	//		// Make sure the project to copy to is empty to begin with
	//		page.goToPage("2");
	//		assertEquals(0, page.getNumberOfSamplesDisplayed());
	//
	//		page.goToPage();
	//		assertTrue(page.isElementOnScreen("copyBtn"));
	//		assertTrue(page.isElementOnScreen("moveBtn"));
	//
	//		// Should be able to copy files to a project that they are a manager of.
	//		selectFirstThreeSamples();
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("copyBtn");
	//		assertTrue(page.isItemVisible("copy-samples-modal"));
	//		page.selectProjectByName("2", "confirm-copy-samples");
	//		assertTrue(page.isBtnEnabled("confirm-copy-samples"));
	//		page.clickBtn("confirm-copy-samples");
	//		page.checkSuccessNotification();
	//
	//		// Check to make sure the samples where copied there
	//		page.goToPage("2");
	//		assertEquals(3, page.getNumberOfSamplesDisplayed());
	//	}
	//
	//	@Test
	//	public void testMoveSamplesAsManagerToManagedProject() {
	//		LoginPage.login(driver(), "project1Manager", "Password1");
	//		// Make sure the project to copy to is empty to begin with
	//		page.goToPage("2");
	//		assertEquals(0, page.getNumberOfSamplesDisplayed());
	//		page.goToPage();
	//
	//		// Should be able to copy files to a project that they are a manager of.
	//		selectFirstThreeSamples();
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("moveBtn");
	//		assertTrue(page.isItemVisible("move-samples-modal"));
	//		page.selectProjectByName("2", "confirm-move-samples");
	//		assertTrue(page.isBtnEnabled("confirm-move-samples"));
	//		page.clickBtn("confirm-move-samples");
	//		page.checkSuccessNotification();
	//
	//		assertEquals("no samples shold be selected after move", 0, page.getTotalNumberOfSamplesSelected());
	//
	//		// Check to make sure the samples where copied there
	//		page.goToPage("2");
	//		assertEquals(3, page.getNumberOfSamplesDisplayed());
	//	}
	//
	//	@Test
	//	public void testCopySamplesAsManagerToUnmanagedProject() {
	//		LoginPage.login(driver(), "project1Manager", "Password1");
	//		page.goToPage();
	//
	//		// Should be able to copy files to a project that they are a manager of.
	//		selectFirstThreeSamples();
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("copyBtn");
	//		assertTrue(page.isItemVisible("copy-samples-modal"));
	//		page.selectProjectByName("3", "confirm-copy-samples");
	//		assertFalse("Since the project does not exist in the list, they cannot copy files to it.",
	//				page.isBtnEnabled("confirm-copy-samples"));
	//	}
	//
	//	@Test
	//	public void testRemoveSamples() {
	//		LoginPage.login(driver(), "project1Manager", "Password1");
	//		page.goToPage();
	//
	//		int totalSampleCount = page.getTotalSampleCount();
	//
	//		selectFirstThreeSamples();
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("removeBtn");
	//		assertTrue(page.isItemVisible("remove-samples-modal"));
	//		page.clickBtn("confirmRemoveBtn");
	//		assertTrue(page.checkSuccessNotification());
	//
	//		page.goToPage();
	//		int newSampleCount = page.getTotalSampleCount();
	//
	//		assertEquals("should be 3 less samples that we started with", totalSampleCount - 3, newSampleCount);
	//	}
	//
	//	@Test
	//	public void testAdminCopyFromAnyProjectToAnyProject() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		selectFirstThreeSamples();
	//		// Admin is not on project5
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("copyBtn");
	//		assertTrue(page.isItemVisible("copy-samples-modal"));
	//		page.selectProjectByName("5", "confirm-copy-samples");
	//		assertTrue(page.isBtnEnabled("confirm-copy-samples"));
	//		page.clickBtn("confirm-copy-samples");
	//		assertTrue(page.checkSuccessNotification());
	//
	//		// Check to make sure the samples where copied there
	//		page.goToPage("5");
	//		assertEquals(3, page.getNumberOfSamplesDisplayed());
	//	}
	//
	//	@Test
	//	public void testMoveSampleToProjectConflict() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		// try to move to existing project
	//		page.selectSampleByRow(0);
	//		page.clickBtn("samplesOptionsBtn");
	//		page.clickBtn("moveBtn");
	//		assertTrue(page.isItemVisible("move-samples-modal"));
	//		page.selectProjectByName("3", "confirm-move-samples");
	//		assertTrue(page.isBtnEnabled("confirm-move-samples"));
	//		page.clickBtn("confirm-move-samples");
	//		assertTrue(page.checkWarningNotification());
	//
	//		assertEquals(1, page.getTotalNumberOfSamplesSelected());
	//	}

	//	@Test
	//	public void testExportLinker() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		assertFalse(page.isBtnEnabled("exportOptionsBtn"));
	//		page.selectSampleByRow(0);
	//		assertTrue(page.isBtnEnabled("exportOptionsBtn"));
	//		page.clickBtn("exportOptionsBtn");
	//		page.clickBtn("exportLinkerBtn");
	//
	//		assertTrue(page.isItemVisible("linker-modal"));
	//		assertEquals(1, getSampleFlagCount(page.getLinkerScriptText()));
	//		page.clickBtn("linkerCloseBtn");
	//
	//		// Select all samples
	//		page.clickBtn("selectBtn");
	//		page.clickBtn("selectAllBtn");
	//		page.clickBtn("exportOptionsBtn");
	//		page.clickBtn("exportLinkerBtn");
	//		assertEquals(0, getSampleFlagCount(page.getLinkerScriptText()));
	//		page.clickBtn("linkerCloseBtn");
	//
	//		page.selectSampleByRow(0);
	//		int selectedCount = page.getTotalSelectedSamplesCount();
	//		page.clickBtn("exportOptionsBtn");
	//		page.clickBtn("exportLinkerBtn");
	//		String command = page.getLinkerScriptText();
	//		assertEquals(selectedCount, getSampleFlagCount(command));
	//	}

	//	@Test
	//	public void testSampleFilter() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		// Filter by name
	//		page.filterByName("ple1");
	//		assertEquals(1, page.getFilteredSampleCount());
	//		page.filterByName("5");
	//		assertEquals(17, page.getFilteredSampleCount());
	//		page.clearFilterByName();
	//
	//		// Filter by organism
	//		page.filterByOrganism("coli");
	//		assertEquals(3, page.getFilteredSampleCount());
	//		page.filterByOrganism("Listeria");
	//		assertEquals(2, page.getFilteredSampleCount());
	//
	//		// Test filter by file
	//		page.filterByFile();
	//		assertEquals(2, page.getFilteredSampleCount());
	//	}
	//
	//	@Test
	//	public void testChangingTableSize() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		assertEquals(10, page.getNumberOfSamplesDisplayed());
	//		page.selectPageSize("25");
	//		assertEquals(21, page.getNumberOfSamplesDisplayed());
	//		page.selectPageSize("10");
	//		assertEquals(10, page.getNumberOfSamplesDisplayed());
	//	}
	//

	//	@Test
	//	public void testClearCart() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		selectFirstThreeSamples();
	//		page.addSamplesToGlobalCart();
	//		page.showCart();
	//		assertEquals("cart should have 3 samples", 3, page.getCartCount());
	//		assertEquals("cart should have 1 project", 1, page.getCartProjectCount());
	//
	//		page.clearCart();
	//
	//		assertFalse(page.isCartCountVisible());
	//		assertEquals("cart should have been emptied", 0, page.getCartProjectCount());
	//	}
	//
	//	@Test
	//	public void testDeleteProjectFromCart() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		assertFalse(page.isCartVisible());
	//		assertFalse(page.isCartCountVisible());
	//
	//		selectFirstThreeSamples();
	//		page.addSamplesToGlobalCart();
	//		page.showCart();
	//		assertEquals("cart should have 3 samples", 3, page.getCartCount());
	//		assertEquals("cart should have 1 project", 1, page.getCartProjectCount());
	//
	//		page.removeProjectFromCart(1L);
	//
	//		assertFalse(page.isCartCountVisible());
	//		assertEquals("cart should have been emptied", 0, page.getCartProjectCount());
	//	}
	//
	//	@Test
	//	public void testDeleteSampleFromCart() {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage();
	//
	//		selectFirstThreeSamples();
	//		page.addSamplesToGlobalCart();
	//		page.showCart();
	//		assertEquals("cart should have 3 samples", 3, page.getCartCount());
	//		assertEquals("cart should have 1 project", 1, page.getCartProjectCount());
	//
	//		page.removeFirstSampleFromProjectInCart(1L);
	//
	//		assertEquals("cart should have 2 samples", 2, page.getCartCount());
	//		assertEquals("cart should have 2 samples", 1, page.getCartProjectCount());
	//	}
	//
	//	@Test
	//	public void testShowAssociatedSamples() throws InterruptedException {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage("6");
	//		int initialNumber = page.getNumberOfSamplesDisplayed();
	//
	//		page.enableAssociatedProjects();
	//
	//		int laterNumber = page.getNumberOfSamplesDisplayed();
	//
	//		assertNotEquals("page should have associated samples displayed", initialNumber, laterNumber);
	//	}
	//
	//	@Test
	//	public void testAddAssociatedToCart() throws InterruptedException {
	//		LoginPage.loginAsManager(driver());
	//		page.goToPage("6");
	//		page.enableAssociatedProjects();
	//
	//		page.selectSampleByClass("associated-sample");
	//		page.addSamplesToGlobalCart();
	//		int cartCount = page.getCartCount();
	//
	//		assertEquals(1, cartCount);
	//	}
	//
	//	@Test
	//	public void testShowRemoteSamples() throws InterruptedException {
	//		LoginPage.loginAsAdmin(driver());
	//		// add the api
	//		RemoteApiUtilities.addRemoteApi(driver());
	//
	//		// associate a project from that api
	//		AssociatedProjectEditPage apEditPage = new AssociatedProjectEditPage(driver());
	//		apEditPage.goTo(2L);
	//		apEditPage.viewRemoteTab();
	//		apEditPage.clickAssociatedButton(6L);
	//		apEditPage.checkNotyStatus("success");
	//
	//		// go to project
	//		page.goToPage("2");
	//
	//		assertEquals("no remote samples should be displayed", 0, page.getNumberOfRemoteSamplesDisplayed());
	//
	//		page.enableRemoteProjects();
	//
	//		assertEquals("1 remote sample should be displayed", 1, page.getNumberOfRemoteSamplesDisplayed());
	//
	//		page.selectSampleByClass("remote-sample");
	//		page.addSamplesToGlobalCart();
	//		assertEquals(1, page.getCartCount());
	//	}
}
