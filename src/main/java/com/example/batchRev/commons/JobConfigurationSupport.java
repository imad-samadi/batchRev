package com.example.batchRev.commons;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;


@Slf4j
public abstract   class JobConfigurationSupport <R, W>{

    @Autowired
    protected BatchProperties batchProperties;

    @Autowired
    private JobRepository jobRepository;

    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JobExecutionListener jobExecutionListener;
    @Autowired private StepExecutionListener stepExecutionListener;
    @Autowired private BackOffPolicy backOffPolicy;
    @Autowired private RetryPolicy retryPolicy;
    @Autowired private SkipPolicy skipPolicy;

    //not implemented yet !!!
    @Autowired private ObjectProvider<SkipListener<R, W>> skipListenerProvider;
   // @Autowired private List<Class<? extends Throwable>> skippedExceptions;
    //@Autowired private JobParametersIncrementer jobParametersIncrementer;


    protected Job newSimpleJob(
            final String jobName,
            final ItemReader<R> reader,
            final ItemProcessor<R, W> processor,
            final ItemWriter<W> writer) {
        return new JobBuilder(jobName, this.jobRepository)

                .listener(this.jobExecutionListener())
                .start(newStep(jobName, reader, processor, writer))
                .build();
    }


    protected Step newStep(
            final String name,
            final ItemReader<R> reader,
            final ItemProcessor processor,
            final ItemWriter<W> writer) {
        FaultTolerantStepBuilder stepBuilder =
                new StepBuilder(name + "-Step", this.jobRepository)
                        .<R, W>chunk(this.batchProperties.getChunkSize(), this.transactionManager)
                        //.allowStartIfComplete(true)
                        .listener(this.stepExecutionListener())
                        //.taskExecutor(this.taskExecutor) // Multithreading in Step
                        // need verification if it works
                        .reader(reader)
                        .processor(processor)
                        .writer(writer)
                        .faultTolerant()
                        .skipPolicy(this.skipPolicy())
                        .listener(this.skipListener())
                        .retryPolicy(this.retryPolicy())
                        .backOffPolicy(this.backOffPolicy());


        return stepBuilder.build();
    }

    protected BackOffPolicy backOffPolicy() {
        return this.backOffPolicy;
    }

    protected RetryPolicy retryPolicy() {
        return this.retryPolicy;
    }

    protected SkipPolicy skipPolicy() {
        return this.skipPolicy;
    }

    protected JobExecutionListener jobExecutionListener() {
        return this.jobExecutionListener;
    }

    protected StepExecutionListener stepExecutionListener() {
        return this.stepExecutionListener;
    }




   protected SkipListener<R, W> skipListener() {
        return this.skipListenerProvider.getIfAvailable(
                () ->
                        new SkipListener<>() {
                            @Override
                            public void onSkipInRead(Throwable t) {
                                // just logs the message, without printing the full stack trace
                                log.error("⛔ Skipped while reading due to: {}", t.getMessage());
                            }
                        });
    }
}
