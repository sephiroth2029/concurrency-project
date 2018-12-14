package ca.uvic.concurrency.gmmurguia.project.distributedsliq.sync;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.ClassList;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessorLocal;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.MinEntropyHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class SyncImpl {

    public static void main(String[] args) {
        SpringApplication.run(SyncImpl.class, args);
    }

    @Bean
    public AttributeFileProcessor[] attributeFileProcessors(
            @Value("${sliq.classAttributeFile}") String classAttributeFile, @Autowired EntropyProcessor entropyProcessor) {
        return new AttributeFileProcessor[] {
                new AttributeFileProcessorHdd("Person-ID", entropyProcessor),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessorHdd("X-Coord", entropyProcessor),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessor.NullAttributeFileProcessor(),
                new AttributeFileProcessorHdd(classAttributeFile, true),
        };
    }

    @Bean
    public EntropyProcessor entropyProcessor(@Autowired MinEntropyHistory minEntropyHistory,
                                             @Autowired ClassList classList) {
        return new EntropyProcessorLocal(classList, minEntropyHistory);
    }

    @Bean
    public MinEntropyHistoryLocal minEntropyHistoryLocal() {
        return new MinEntropyHistoryLocal();
    }

    @Bean
    public ClassList classList() {
        return new ClassListLocal(null);
    }
}
