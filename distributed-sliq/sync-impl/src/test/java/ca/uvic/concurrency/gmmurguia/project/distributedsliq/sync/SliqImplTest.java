package ca.uvic.concurrency.gmmurguia.project.distributedsliq.sync;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.CompleteProcessorSync;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessorLocal;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyHistory;
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

@RunWith(Parameterized.class)
public class SliqImplTest {

//    @Parameterized.Parameters
    public static Collection<Integer> data() {
        return Arrays.asList(1, 2, 5, 7, 10);
    }

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

//    @Parameterized.Parameter
    public Integer repetitions = 5;

    @Parameterized.Parameter
    public String file;

    private SliqImpl sliq;

    @Test
    public void testIt() throws IOException {
        System.out.println(file);
        System.out.println(repetitions);
        String classAttributeFile = "Activity";
        AttributeFileProcessorHdd classAttributeProcessor = new AttributeFileProcessorHdd(classAttributeFile, true);
        ClassListLocal classList = new ClassListLocal(classAttributeProcessor);
        classList.init();
        MinEntropyHistory minEntropyHistory = new MinEntropyHistoryLocal();

        sliq = new SliqImpl();
        sliq.setClassAttributeProcessor(classAttributeProcessor);
        sliq.setMinEntropyHistory(minEntropyHistory);
        sliq.setClassList(classList);
//        sliq.setSourcePath("C:\\Users\\Giovanni\\Google Drive\\UVic\\MCS\\Concurrency\\Project\\sliq\\ConfLongDemo_JSI.txt");
        sliq.setSourcePath(file);
        EntropyProcessorLocal entropyProcessor = new EntropyProcessorLocal(classList, minEntropyHistory);
        AttributeFileProcessor[] attributeFileProcessors = {
                new AttributeFileProcessorHdd("Person-ID", entropyProcessor),
                new AttributeFileProcessorHdd("Tag-ID", entropyProcessor),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessorHdd("X-Coord", entropyProcessor),
                new AttributeFileProcessorHdd("Y-Coord", entropyProcessor),
                new AttributeFileProcessorHdd("Z-Coord", entropyProcessor),
                classAttributeProcessor,
        };
        entropyProcessor.setAttributeFileProcessors(attributeFileProcessors);
        CompleteProcessorSync completeProcessor = new CompleteProcessorSync(attributeFileProcessors);
        classAttributeProcessor.setEntropyProcessor(entropyProcessor);
        sliq.setAttributeFileProcessors(attributeFileProcessors);
        sliq.setCompleteProcessor(completeProcessor);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        sliq.start(repetitions);
        stopWatch.stop();

        try (BufferedWriter out = new BufferedWriter(new FileWriter("results_sync.txt", true))) {
            out.write(String.format("%d,%d", repetitions, stopWatch.getNanoTime()));
            out.newLine();
        }
    }
}