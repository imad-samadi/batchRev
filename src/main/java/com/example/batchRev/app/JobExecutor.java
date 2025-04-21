package com.example.batchRev.app;

import com.example.batchRev.app.domain.AppConstants;
import com.example.batchRev.commons.AbstractJobExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobExecutor extends AbstractJobExecutor {

    private final Job statementJob;

    JobExecutor(@Qualifier(AppConstants.STATEMENT_JOB_NAME)  final Job statementJob) {
        this.statementJob = statementJob;
    }

    public void executeSimpleJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        executeWithNoParameters(statementJob); // Inherits @Autowired fields from parent
    }
}
