package com.example.batchRev.commons;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.BackOffPolicyBuilder;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;


import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(BatchProperties.class)
@RequiredArgsConstructor

public class BatchConfiguration extends DefaultBatchConfiguration {

    private final DataSource dataSource;

    private final BatchProperties batchProperties;



    @Override
    protected PlatformTransactionManager getTransactionManager() {
        return new DataSourceTransactionManager(this.dataSource);
    }


    @ConditionalOnMissingBean
   @Bean
   BackOffPolicy backOffPolicy(BatchProperties batchProperties) {  //How Long Should We Wait Before Retrying ?
       return BackOffPolicyBuilder.newBuilder()
               .delay(batchProperties.getBackoffInitialDelay().toMillis())
               .multiplier(batchProperties.getBackoffMultiplier())
               .build();
   }

    @ConditionalOnMissingBean
    @Bean
    List<Class<? extends Throwable>> skippedExceptions() {
        return List.of(FlatFileParseException.class);
    }

    @ConditionalOnMissingBean
    @Bean
    public RetryPolicy retryPolicy(BatchProperties batchProperties) {
        CompositeRetryPolicy composite = new CompositeRetryPolicy();
        composite.setPolicies(new RetryPolicy[]{
                noRetryPolicy(batchProperties),
                daoRetryPolicy(batchProperties),
        });
        return composite;
    }

    //what not
    private RetryPolicy noRetryPolicy(BatchProperties batchProperties) {
        Map<Class<? extends Throwable>, Boolean> exceptionClassifiers =
                this.skippedExceptions().stream().collect(Collectors.toMap(ex -> ex, ex -> Boolean.FALSE));
        return new SimpleRetryPolicy(batchProperties.getMaxRetries(), exceptionClassifiers, false);
    }

    //policy specifically for database errors
    private RetryPolicy daoRetryPolicy(BatchProperties batchProperties) {
        return new SimpleRetryPolicy(
                batchProperties.getMaxRetries(),
                Map.of(
                        TransientDataAccessException.class,
                        true,
                        RecoverableDataAccessException.class,
                        true,
                        NonTransientDataAccessException.class,
                        false,
                        EmptyResultDataAccessException.class,
                        false),
                false);
    }





    @ConditionalOnMissingBean
    @Bean
    SkipPolicy skipPolicy(BatchProperties batchProperties) {
        Map<Class<? extends Throwable>, Boolean> exceptionClassifiers =
                this.skippedExceptions().stream().collect(Collectors.toMap(ex -> ex, ex -> Boolean.TRUE));
        return new LimitCheckingItemSkipPolicy(
                batchProperties.getSkipLimit(), exceptionClassifiers);
    }

    @ConditionalOnMissingBean
    @Bean
    JobExecutionListener jobExecutionListener() {
        return new LoggingJobListener();
    }

    @ConditionalOnMissingBean
    @Bean
    StepExecutionListener stepExecutionListener() {
        return new LoggingStepListener();
    }



}
