package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.pdf.Pdf;

/**
 * Deletes a pdf identified using it's displayed index from the pdf book.
 * By default it performs a 'soft' delete where the file is merely deleted from PDF++
 * but not the file system. Option for 'hard' delete is also possible.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    /**
     * Enum that represents the different types of delete operation performed.
     * Soft: File is deleted from PDF++.
     * Hard: File is deleted from PDF++ and from local file system.
     */
    public enum DeleteType {
        Hard, Soft
    }

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the PDF identified by the index number used in the displayed PDF list.\n"
            + "Soft delete selected by default, hard delete can be selected by additional keyword 'hard'.\n"
            + "Parameters(Soft Delete): INDEX (must be a positive integer)\n"
            + "Parameters(Hard Delete): INDEX (must be a positive integer) hard\n"
            + "Example: " + COMMAND_WORD + " 1 OR " + COMMAND_WORD + " 1 hard";

    public static final String MESSAGE_DELETE_PDF_SUCCESS = "Deleted PDF: %1$s";
    public static final String MESSAGE_DELETE_HARD_FAIL = "PDF unable to be deleted at filesystem.";

    private final Index targetIndex;
    private final DeleteType deleteType;

    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
        this.deleteType = DeleteType.Soft;
    }

    public DeleteCommand(Index targetIndex, DeleteType type) {
        this.targetIndex = targetIndex;
        this.deleteType = type;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);
        List<Pdf> lastShownList = getFliteredPdfList(model, targetIndex);

        Pdf pdfToDelete = lastShownList.get(targetIndex.getZeroBased());
        model.deletePdf(pdfToDelete);

        if (deleteType == DeleteType.Hard) {
            File dFile = Paths.get(pdfToDelete.getDirectory().getDirectory(),
                    pdfToDelete.getName().getFullName()).toFile();
            if (!dFile.delete()) {
                throw new CommandException(MESSAGE_DELETE_HARD_FAIL);
            }
        }

        model.commitPdfBook();
        return new CommandResult(String.format(MESSAGE_DELETE_PDF_SUCCESS, pdfToDelete));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeleteCommand // instanceof handles nulls
                && targetIndex.equals(((DeleteCommand) other).targetIndex)
                && deleteType.equals((((DeleteCommand) other).deleteType))); // state check
    }
}
