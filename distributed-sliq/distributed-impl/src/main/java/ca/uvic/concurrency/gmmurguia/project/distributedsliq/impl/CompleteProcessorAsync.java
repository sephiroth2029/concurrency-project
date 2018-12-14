package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.CompleteProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@AllArgsConstructor
public class CompleteProcessorAsync implements CompleteProcessor {

    private AttributeFileProcessor[] attributeFileProcessors;

    @Override
    public void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                        HashMap<String, List<String>> processedValues) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (AttributeFileProcessor processor : attributeFileProcessors) {
            if (processor.isClassAttribute()) continue;

//            futures.add(CompletableFuture.supplyAsync(() -> {
                processor.process(classCounters, processedValues);
//                return null;
//            }));
        }

//        for (CompletableFuture<Void> future : futures) {
//            future.join();
//        }
    }
}
