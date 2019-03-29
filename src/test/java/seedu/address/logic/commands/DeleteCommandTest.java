package seedu.address.logic.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPdfAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PDF;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PDF;
import static seedu.address.testutil.TypicalPdfs.getTypicalPdfBook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.pdf.Pdf;
import seedu.address.model.pdf.exceptions.PdfNotFoundException;

/**
 * Contains integration tests (interaction with the Model, UndoCommand and RedoCommand) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalPdfBook(), new UserPrefs());
    private CommandHistory commandHistory = new CommandHistory();

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Pdf pdfToDelete = model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PDF);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PDF_SUCCESS, pdfToDelete);

        ModelManager expectedModel = new ModelManager(model.getPdfBook(), new UserPrefs());
        expectedModel.deletePdf(pdfToDelete);
        expectedModel.commitPdfBook();

        assertCommandSuccess(deleteCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexUnfilteredListHard_success() {
        Pdf pdfToDelete = model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased());

        saveBackup(pdfToDelete);
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PDF, DeleteCommand.DeleteType.Hard);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PDF_SUCCESS, pdfToDelete);

        ModelManager expectedModel = new ModelManager(model.getPdfBook(), new UserPrefs());
        expectedModel.deletePdf(pdfToDelete);
        expectedModel.commitPdfBook();

        assertCommandSuccess(deleteCommand, model, commandHistory, expectedMessage, expectedModel);
        revertBackup(pdfToDelete);
        assertTrue(Paths.get(pdfToDelete.getDirectory().getDirectory(), pdfToDelete.getName().getFullName())
                .toFile().exists());

    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPdfList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);
        assertCommandFailure(deleteCommand, model, commandHistory, Messages.MESSAGE_INVALID_PDF_DISPLAYED_INDEX);

        DeleteCommand deleteHardCommand = new DeleteCommand(outOfBoundIndex, DeleteCommand.DeleteType.Hard);
        assertCommandFailure(deleteHardCommand, model, commandHistory, Messages.MESSAGE_INVALID_PDF_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPdfAtIndex(model, INDEX_FIRST_PDF);

        Pdf pdfToDelete = model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PDF);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PDF_SUCCESS, pdfToDelete);

        Model expectedModel = new ModelManager(model.getPdfBook(), new UserPrefs());
        expectedModel.deletePdf(pdfToDelete);
        expectedModel.commitPdfBook();
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredListHard_success() {
        showPdfAtIndex(model, INDEX_FIRST_PDF);

        Pdf pdfToDelete = model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased());
        saveBackup(pdfToDelete);
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PDF, DeleteCommand.DeleteType.Hard);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PDF_SUCCESS, pdfToDelete);

        Model expectedModel = new ModelManager(model.getPdfBook(), new UserPrefs());
        expectedModel.deletePdf(pdfToDelete);
        expectedModel.commitPdfBook();
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, commandHistory, expectedMessage, expectedModel);
        revertBackup(pdfToDelete);
        assertTrue(Paths.get(pdfToDelete.getDirectory().getDirectory(),
                pdfToDelete.getName().getFullName()).toFile().exists());
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPdfAtIndex(model, INDEX_FIRST_PDF);

        Index outOfBoundIndex = INDEX_SECOND_PDF;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getPdfBook().getPdfList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, commandHistory, Messages.MESSAGE_INVALID_PDF_DISPLAYED_INDEX);

        DeleteCommand deleteHardCommand = new DeleteCommand(outOfBoundIndex, DeleteCommand.DeleteType.Hard);

        assertCommandFailure(deleteHardCommand, model, commandHistory, Messages.MESSAGE_INVALID_PDF_DISPLAYED_INDEX);
    }

    @Test
    public void executeUndoRedo_validIndexUnfilteredList_success() throws Exception {
        Pdf pdfToDelete = model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PDF);
        Model expectedModel = new ModelManager(model.getPdfBook(), new UserPrefs());
        expectedModel.deletePdf(pdfToDelete);
        expectedModel.commitPdfBook();

        // delete -> first pdf deleted
        deleteCommand.execute(model, commandHistory);

        // undo -> reverts addressbook back to previous state and filtered pdf list to show all persons
        expectedModel.undoPdfBook();
        assertCommandSuccess(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        // redo -> same first pdf deleted again
        expectedModel.redoPdfBook();
        assertCommandSuccess(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void executeUndoRedo_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPdfList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        // execution failed -> address book state not added into model
        assertCommandFailure(deleteCommand, model, commandHistory, Messages.MESSAGE_INVALID_PDF_DISPLAYED_INDEX);

        // single address book state in model -> undoCommand and redoCommand fail
        assertCommandFailure(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_FAILURE);
        assertCommandFailure(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_FAILURE);
    }

    /**
     * 1. Deletes a {@code Pdf} from a filtered list.
     * 2. Undo the deletion.
     * 3. The unfiltered list should be shown now. Verify that the index of the previously deleted pdf in the
     * unfiltered list is different from the index at the filtered list.
     * 4. Redo the deletion. This ensures {@code RedoCommand} deletes the pdf object regardless of indexing.
     */
    @Test
    public void executeUndoRedo_validIndexFilteredList_samePersonDeleted() throws Exception {
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PDF);
        Model expectedModel = new ModelManager(model.getPdfBook(), new UserPrefs());

        showPdfAtIndex(model, INDEX_SECOND_PDF);
        Pdf pdfToDelete = model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased());
        expectedModel.deletePdf(pdfToDelete);
        expectedModel.commitPdfBook();

        // delete -> deletes second pdf in unfiltered pdf list / first pdf in filtered pdf list
        deleteCommand.execute(model, commandHistory);

        // undo -> reverts pdfbook back to previous state and filtered pdf list to show all persons
        expectedModel.undoPdfBook();
        assertCommandSuccess(new UndoCommand(), model, commandHistory, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        assertNotEquals(pdfToDelete, model.getFilteredPdfList().get(INDEX_FIRST_PDF.getZeroBased()));
        // redo -> deletes same second pdf in unfiltered pdf list
        expectedModel.redoPdfBook();
        assertCommandSuccess(new RedoCommand(), model, commandHistory, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PDF);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PDF);
        DeleteCommand deleteHardFirstCommand = new DeleteCommand(INDEX_FIRST_PDF, DeleteCommand.DeleteType.Hard);
        DeleteCommand deleteHardSecondCommand = new DeleteCommand(INDEX_SECOND_PDF, DeleteCommand.DeleteType.Hard);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PDF);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));
        assertFalse(deleteFirstCommand.equals(deleteHardFirstCommand));
        assertFalse(deleteHardSecondCommand.equals(deleteSecondCommand));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different pdf -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    /**
     * Saves {@code pdfToDelete} as a backup
     */
    private void saveBackup(Pdf pdfToDelete) {
        try {

            File testdir = Paths.get(pdfToDelete.getDirectory().getDirectory(), "Backup").toFile();
            if (!testdir.exists()) {
                if (testdir.mkdir()) {
                    throw new IOException();
                }
            }

            Files.copy(Paths.get(pdfToDelete.getDirectory().getDirectory(), pdfToDelete.getName().getFullName()),
                    Paths.get(pdfToDelete.getDirectory().getDirectory(), "Backup",
                            pdfToDelete.getName().getFullName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            throw new PdfNotFoundException();
        }
    }

    /**
     * Moves {@code pdfToRevert} back to its original location
     */
    private void revertBackup(Pdf pdfToRevert) {
        File fileToRevert = Paths.get(pdfToRevert.getDirectory().getDirectory(), "Backup",
                pdfToRevert.getName().getFullName()).toFile();
        File revertedFile = Paths.get(pdfToRevert.getDirectory().getDirectory(),
                pdfToRevert.getName().getFullName()).toFile();
        fileToRevert.renameTo(revertedFile);
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPdfList(p -> false);

        assertTrue(model.getFilteredPdfList().isEmpty());
    }
}
