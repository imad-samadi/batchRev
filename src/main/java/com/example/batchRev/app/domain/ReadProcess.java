package com.example.batchRev.app.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadProcess implements ItemProcessor<BankTransaction,BankTransaction> {

    private StepExecution stepExecution;

    @Override
    public BankTransaction process(BankTransaction item) throws Exception {
        System.out.println(item);
        return item;
    }

    public void actionSkip (List<BankTransaction> skippedItems)  {

        this.stepExecution.getExecutionContext().put("skippedItems", skippedItems);
    }
    public void actionKill ()  {

        this.stepExecution.setTerminateOnly();
    }



}
