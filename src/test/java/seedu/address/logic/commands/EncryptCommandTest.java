package seedu.address.logic.commands;

//import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PDF;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.logic.CommandHistory;
import seedu.address.model.pdf.Pdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static seedu.address.testutil.TypicalPdfs.SAMPLE_PDF_3;

public class EncryptCommandTest {
    private static final CommandHistory EMPTY_COMMAND_HISTORY = new CommandHistory();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        initialiseTest(SAMPLE_PDF_3);
    }

    @Test
    public void constructor_nullPdf_throwsNullPointerException() {
        /*thrown.expect(NullPointerException.class);
        new AddCommand(null);*/
    }

    /**
     * Initialises the files for th test
     */
    private void initialiseTest(Pdf target) {
        if (Paths.get(target.getDirectory().getDirectory() + "\\" + target.getName()).toFile().exists()) {
            try {
                Files.delete(Paths.get(target.getDirectory().getDirectory() + "\\" + target.getName()));
            } catch (IOException ioe) {
                System.out.println("The test is already at initialised state");
            }
        }
    }
}
