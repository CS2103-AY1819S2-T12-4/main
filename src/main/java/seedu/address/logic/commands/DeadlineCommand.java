package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.commands.EditCommand.MESSAGE_EDIT_PDF_SUCCESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE_DONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE_NEW;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE_REMOVE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PDFS;

import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.pdf.Deadline;
import seedu.address.model.pdf.DeadlineStatus;
import seedu.address.model.pdf.Pdf;

/**
 * Edits the deadline of an existing pdf in the address book.
 */
public class DeadlineCommand extends Command {

    public static final String COMMAND_WORD = "deadline";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sets a deadline to a selected pdf indicated "
            + "by the index number used in the displayed pdf list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_DEADLINE_NEW + "DEADLINE] (In dd-mm-yyyy format) [done] [remove]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_DEADLINE_NEW + "13-02-2020\n"
            + "Example: " + COMMAND_WORD + " 2 " + PREFIX_DEADLINE_DONE + "\n"
            + "Example: " + COMMAND_WORD + " 2 " + PREFIX_DEADLINE_REMOVE + "\n";

    private final Index index;
    private final Deadline deadline;
    private final DeadlineStatus status;

    public DeadlineCommand(Index index, Deadline deadline) {
        requireNonNull(index);
        requireNonNull(deadline);

        this.index = index;
        this.deadline = deadline;
        this.status = DeadlineStatus.READY;
    }

    public DeadlineCommand(Index index, Deadline deadline, DeadlineStatus status) {
        requireNonNull(index);
        requireNonNull(status);

        this.index = index;
        this.deadline = deadline;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);
        List<Pdf> lastShownList = getFliteredPdfList(model, index);

        Pdf oPdf = lastShownList.get(this.index.getZeroBased());
        Pdf nPdf;

        if (deadline == null) {
            nPdf = DeadlineCommand.getPdfWithNewDeadline(oPdf, new Deadline(oPdf.getDeadline(), this.status));
        } else {
            nPdf = DeadlineCommand.getPdfWithNewDeadline(oPdf, this.deadline);
        }

        model.setPdf(oPdf, nPdf);
        model.updateFilteredPdfList(PREDICATE_SHOW_ALL_PDFS);
        model.commitPdfBook();

        return new CommandResult(String.format(MESSAGE_EDIT_PDF_SUCCESS, nPdf));
    }

    public static Pdf getPdfWithNewDeadline(Pdf old, Deadline deadline) {
        return new Pdf(old.getName(), old.getDirectory(), old.getSize(), old.getTags(), deadline);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeadlineCommand)) {
            return false;
        }

        // state check
        DeadlineCommand e = (DeadlineCommand) other;
        return index.equals(e.index)
                && deadline.equals(e.deadline)
                && status.equals(e.status);
    }
}
