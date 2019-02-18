package ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl;

import ca.uvic.concurrency.gmmurguia.project.sliqimpl.AttributeFileProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessor;
import ca.uvic.concurrency.gmmurguia.project.sliqimpl.EntropyProcessorLocal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

/**
 * The entry point of the program and main configurer of the Spring Boot/Cloud application.
 */
@SpringBootApplication
@EnableEurekaClient
public class DistributedImpl {

    public static void main(String[] args) {
        SpringApplication.run(DistributedImpl.class, args);
    }

    @Bean
    public AttributeFileProcessor[] attributeFileProcessors(
            @Value("${sliq.classAttributeFile}") String classAttributeFile) {

        AttributeFileProcessor[] arr = new AttributeFileProcessor[9];
        arr[0] = new AttributeFileProcessorAtomix("Person-ID", entropyProcessor());
        arr[1] = new AttributeFileProcessor.NullAttributeFileProcessor();
        arr[2] = new AttributeFileProcessor.NullAttributeFileProcessor();
        arr[3] = new AttributeFileProcessor.NullAttributeFileProcessor();
        arr[4] = new AttributeFileProcessorAtomix("X-Coord", entropyProcessor());
        arr[5] = new AttributeFileProcessor.NullAttributeFileProcessor();
        arr[6] = new AttributeFileProcessor.NullAttributeFileProcessor();
        arr[7] = new AttributeFileProcessor.NullAttributeFileProcessor();
        arr[8] = new AttributeFileProcessorAtomix(classAttributeFile, true);


        return arr;
    }

    @Bean
    public EntropyProcessor entropyProcessor() {
        return new EntropyProcessorLocal(null, null);
    }

}
