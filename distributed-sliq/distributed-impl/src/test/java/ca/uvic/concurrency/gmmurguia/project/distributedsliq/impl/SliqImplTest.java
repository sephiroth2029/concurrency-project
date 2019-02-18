package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessorLocal;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.SliqImpl;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * This is a test drive for the project jamboree.
 */
@RunWith(Parameterized.class)
public class SliqImplTest {

    @Parameterized.Parameters
    public static Collection<String> data2() {
        return Arrays.asList(
                "D:\\Development\\Projects\\MSc\\Concurrency\\project\\demo-sync\\ConfLongDemo_JSI_10.txt",
                "D:\\Development\\Projects\\MSc\\Concurrency\\project\\demo-sync\\ConfLongDemo_JSI_100.txt",
                "D:\\Development\\Projects\\MSc\\Concurrency\\project\\demo-sync\\ConfLongDemo_JSI_500.txt",
                "D:\\Development\\Projects\\MSc\\Concurrency\\project\\demo-sync\\ConfLongDemo_JSI_750.txt",
                "D:\\Development\\Projects\\MSc\\Concurrency\\project\\demo-sync\\ConfLongDemo_JSI_1000.txt"
        );
    }

    public Integer repetitions = 5;

    @Parameterized.Parameter
    public String file;

    private SliqImpl sliq;

    @Test
    public void testIt() throws IOException {
        String attribute = "Activity";
        AttributeFileProcessorAtomix classAttributeProcessor = new AttributeFileProcessorAtomix(attribute, true);
        ClassListDistributed classList = new ClassListDistributed(attribute);
        classList.init();
        MinEntropyHistoryDistributed minEntroyHistory = new MinEntropyHistoryDistributed();
        minEntroyHistory.init();

        EntropyProcessorLocal entropyProcessorLocal = new EntropyProcessorLocal(classList, minEntroyHistory);

        sliq = new SliqImpl();
        sliq.setClassAttributeProcessor(classAttributeProcessor);
        sliq.setClassList(classList);
        sliq.setMinEntropyHistory(minEntroyHistory);
        sliq.setSourcePath(file);
        AttributeFileProcessor[] attributeFileProcessors = {
                new AttributeFileProcessorAtomix("Person-ID", entropyProcessorLocal),
                new AttributeFileProcessorAtomix("Tag-ID", entropyProcessorLocal),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessorAtomix("X-Coord", entropyProcessorLocal),
                new AttributeFileProcessorAtomix("Y-Coord", entropyProcessorLocal),
                new AttributeFileProcessorAtomix("Z-Coord", entropyProcessorLocal),
                classAttributeProcessor,
        };
        sliq.setAttributeFileProcessors(attributeFileProcessors);
        classAttributeProcessor.setEntropyProcessor(entropyProcessorLocal);
        entropyProcessorLocal.setAttributeFileProcessors(attributeFileProcessors);
        CompleteProcessorAsync completeProcessorAsync = new CompleteProcessorAsync(attributeFileProcessors);
        sliq.setCompleteProcessor(completeProcessorAsync);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        sliq.start(repetitions);
        stopWatch.stop();

        try (BufferedWriter out = new BufferedWriter(new FileWriter("results_distributed.txt", true))) {
            out.write(String.format("%d,%d", repetitions, stopWatch.getNanoTime()));
            out.newLine();
        }
    }
}