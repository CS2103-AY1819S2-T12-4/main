package systemtests;

import static org.junit.Assert.assertTrue;
import static seedu.pdf.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.pdf.commons.core.Messages.MESSAGE_INVALID_PDF_DISPLAYED_INDEX;
import static seedu.pdf.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.pdf.logic.commands.SelectCommand.MESSAGE_SELECT_PDF_SUCCESS;
import static seedu.pdf.testutil.TestUtil.getLastIndex;
import static seedu.pdf.testutil.TestUtil.getMidIndex;
import static seedu.pdf.testutil.TypicalIndexes.INDEX_FIRST_PDF;
import static seedu.pdf.testutil.TypicalPdfs.KEYWORD_MATCHING_MEIER;

import org.junit.Test;

import seedu.pdf.commons.core.index.Index;
import seedu.pdf.logic.commands.RedoCommand;
import seedu.pdf.logic.commands.SelectCommand;
import seedu.pdf.logic.commands.UndoCommand;
import seedu.pdf.model.Model;

public class SelectCommandSystemTest extends PdfBookSystemTest {
    @Test
    public void select() {
        /* ------------------------ Perform select operations on the shown unfiltered list -------------------------- */

        /* Case: select the first card in the pdf list, command with leading spaces and trailing spaces
         * -> selected
         */
        String command = "   " + SelectCommand.COMMAND_WORD + " " + INDEX_FIRST_PDF.getOneBased() + "   ";
        assertCommandSuccess(command, INDEX_FIRST_PDF);

        /* Case: select the last card in the pdf list -> selected */
        Index pdfCount = getLastIndex(getModel());
        command = SelectCommand.COMMAND_WORD + " " + pdfCount.getOneBased();
        assertCommandSuccess(command, pdfCount);

        /* Case: undo previous selection -> rejected */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_FAILURE;
        assertCommandFailure(command, expectedResultMessage);

        /* Case: redo selecting last card in the list -> rejected */
        command = RedoCommand.COMMAND_WORD;
        expectedResultMessage = RedoCommand.MESSAGE_FAILURE;
        assertCommandFailure(command, expectedResultMessage);

        /* Case: select the middle card in the pdf list -> selected */
        Index middleIndex = getMidIndex(getModel());
        command = SelectCommand.COMMAND_WORD + " " + middleIndex.getOneBased();
        assertCommandSuccess(command, middleIndex);

        /* Case: select the current selected card -> selected */
        assertCommandSuccess(command, middleIndex);

        /* ------------------------ Perform select operations on the shown filtered list ---------------------------- */

        /* Case: filtered pdf list, select index within bounds of pdf book but out of bounds of pdf list
         * -> rejected
         */
        showPdfWithName(KEYWORD_MATCHING_MEIER);
        int invalidIndex = getModel().getPdfBook().getPdfList().size();
        assertCommandFailure(SelectCommand.COMMAND_WORD + " " + invalidIndex, MESSAGE_INVALID_PDF_DISPLAYED_INDEX);

        /* Case: filtered pdf list, select index within bounds of pdf book and pdf list -> selected */
        Index validIndex = Index.fromOneBased(1);
        assertTrue(validIndex.getZeroBased() < getModel().getFilteredPdfList().size());
        command = SelectCommand.COMMAND_WORD + " " + validIndex.getOneBased();
        assertCommandSuccess(command, validIndex);

        /* ----------------------------------- Perform invalid select operations ------------------------------------ */

        /* Case: invalid index (0) -> rejected */
        assertCommandFailure(SelectCommand.COMMAND_WORD + " " + 0,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));

        /* Case: invalid index (-1) -> rejected */
        assertCommandFailure(SelectCommand.COMMAND_WORD + " " + -1,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));

        /* Case: invalid index (size + 1) -> rejected */
        invalidIndex = getModel().getFilteredPdfList().size() + 1;
        assertCommandFailure(SelectCommand.COMMAND_WORD + " " + invalidIndex, MESSAGE_INVALID_PDF_DISPLAYED_INDEX);

        /* Case: invalid arguments (alphabets) -> rejected */
        assertCommandFailure(SelectCommand.COMMAND_WORD + " abc",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));

        /* Case: invalid arguments (extra argument) -> rejected */
        assertCommandFailure(SelectCommand.COMMAND_WORD + " 1 abc",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SelectCommand.MESSAGE_USAGE));

        /* Case: mixed case command word -> rejected */
        assertCommandFailure("SeLeCt 1", MESSAGE_UNKNOWN_COMMAND);

        /* Case: select from empty pdf book -> rejected */
        deleteAllPdf();
        assertCommandFailure(SelectCommand.COMMAND_WORD + " " + INDEX_FIRST_PDF.getOneBased(),
                MESSAGE_INVALID_PDF_DISPLAYED_INDEX);
    }

    /**
     * Executes {@code command} and asserts that the,<br>
     * 1. Command box displays an empty string.<br>
     * 2. Command box has the default style class.<br>
     * 3. Result display box displays the success message of executing select command with the
     * {@code expectedSelectedCardIndex} of the selected pdf.<br>
     * 4. {@code Storage} and {@code PdfListPanel} remain unchanged.<br>
     * 5. Selected card is at {@code expectedSelectedCardIndex} and the browser url is updated accordingly.<br>
     * 6. Status bar remains unchanged.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code PdfBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see PdfBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     * @see PdfBookSystemTest#assertSelectedCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Index expectedSelectedCardIndex) {
        Model expectedModel = getModel();
        String expectedResultMessage = String.format(
                MESSAGE_SELECT_PDF_SUCCESS, expectedSelectedCardIndex.getOneBased());
        int preExecutionSelectedCardIndex = getPdfListPanel().getSelectedCardIndex();

        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);

        if (preExecutionSelectedCardIndex == expectedSelectedCardIndex.getZeroBased()) {
            assertSelectedCardUnchanged();
        } else {
            assertSelectedCardChanged(expectedSelectedCardIndex);
        }

        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchanged();
    }

    /**
     * Executes {@code command} and asserts that the,<br>
     * 1. Command box displays {@code command}.<br>
     * 2. Command box has the error style class.<br>
     * 3. Result display box displays {@code expectedResultMessage}.<br>
     * 4. {@code Storage} and {@code PdfListPanel} remain unchanged.<br>
     * 5. Browser url, selected card and status bar remain unchanged.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code PdfBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see PdfBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
