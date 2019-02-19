package ca.uvic.concurrency.gmmurguia.project.distributedsliq.sync;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessor;
import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Holds the attribute in a file.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AttributeFileProcessorHdd implements AttributeFileProcessor {

    @NonNull
    private String rawFile;

    @NonNull
    private EntropyProcessor entropyProcessor;

    private BufferedWriter bw;

    private boolean classAttribute;

    /**
     * Creates an attribute processor for the given raw file.
     *
     * @param rawFile the raw file for this attribute.
     * @param classAttribute specifies if this is a class attribute.
     */
    public AttributeFileProcessorHdd(String rawFile, boolean classAttribute) {
        this.rawFile = rawFile;
        this.classAttribute = classAttribute;
    }

    /**
     * Opens the underlying file.
     */
    @Override
    public void init() {
        try {
            bw = new BufferedWriter(new FileWriter(rawFile));
        } catch (IOException e) {
            // Not interested in handling, if there is an error just propagate all the way to know the cause.
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a row to the file.
     *
     * @param index the row index.
     * @param value the value associated.
     * @throws IOException if there is a problem accessing the file.
     */
    @Override
    public void addRow(Integer index, String value) throws IOException {
        addRow(index.toString(), value, bw);
    }

    private void addRow(String index, String value, BufferedWriter bw) throws IOException {
        bw.write(index);
        bw.write(",");
        bw.write(value);
        bw.newLine();
    }

    /**
     * Obtains an iterator for the file.
     *
     * @return an iterator for the file.
     * @throws FileNotFoundException if the attribute file no longer exists.
     */
    @Override
    public Iterator<String[]> getIterator() throws FileNotFoundException {
        return new Iterator<String[]>() {
            String[] line;

            CSVReader csvReader = new CSVReader(
                    new BufferedReader(
                            new FileReader(classAttribute ? rawFile : getSortedFileName())
                    )
            );

            @Override
            public boolean hasNext() {
                try {
                    boolean hasIt = ((line = csvReader.readNext()) != null);
                    if (!hasIt) {
                        csvReader.close();
                    }
                    return hasIt;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String[] next() {
                String[] next = line;
                line = null;
                return next;
            }
        };
    }

    private String getSortedFileName() {
        return rawFile + "_sorted";
    }

    /**
     * Sorts the attribute rows in ascending order, considering the attribute value. It will create a new file with
     * the sorted attribute list.
     */
    @Override
    public void sortAttribute() {
        CSVReader csvReader;
        try {
            csvReader = new CSVReader(new FileReader(rawFile));
            List<String[]> rows;
            rows = csvReader.readAll();
            csvReader.close();

            rows.sort(Comparator.comparing(o -> o[1]));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(getSortedFileName()))) {
                for (String[] row : rows) {
                    addRow(row[0], row[1], bw);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the underlying file.
     */
    @Override
    public void close() {
        try {
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the attribute's name.
     *
     * @return the attribute's name.
     */
    @Override
    public String getAttributeName() {
        return rawFile;
    }

}
