package com.example.batchRev.app;

import com.example.batchRev.app.domain.AppConstants;
import com.example.batchRev.app.domain.BankTransaction;
import com.example.batchRev.app.domain.ReadProcess;
import com.example.batchRev.commons.BatchConfiguration;
import com.example.batchRev.commons.JobConfigurationSupport;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@AutoConfigureAfter(value = {BatchConfiguration.class})
public class jobConfig extends JobConfigurationSupport<BankTransaction,BankTransaction> {


        @Autowired
        private  DataSource dataSource;


    @Bean
    Job OurJob1(@Qualifier("csvReader") FlatFileItemReader<BankTransaction> csvReader,
               @Qualifier("JdbcBatchItemWriter2") JdbcBatchItemWriter<BankTransaction> updateBalanceWriter2){
        System.out.println("OurJob1********************");
    return this.newSimpleJob( AppConstants.STATEMENT_JOB_NAME,csvReader,
            csvProcessor(),
            updateBalanceWriter2) ;

}





















    @Bean
    @StepScope
    public FlatFileItemReader<BankTransaction> csvReader() {
        return new FlatFileItemReaderBuilder<BankTransaction>()
                .name("csvReader")

                .linesToSkip(1)
                .resource(new ClassPathResource("customer_transactions.csv")) // Read from CSV
                .delimited()
                .delimiter(",")


                .names("id", "month", "day", "hour", "minute", "amount", "merchant") // Column mapping
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(BankTransaction.class);
                }})
                .build();
    }
    @Bean
    public ItemProcessor<BankTransaction, BankTransaction> csvProcessor() {
        return new ReadProcess();
    }

    @Bean(name = "JdbcBatchItemWriter2")
    public JdbcBatchItemWriter<BankTransaction> updateBalanceWriter2() {
        JdbcBatchItemWriter<BankTransaction> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);

        writer.setSql("INSERT INTO customer_transactions (id, month, day, hour, minute, amount, merchant) " +
                "VALUES (:id, :month, :day, :hour, :minute, :amount, :merchant)");

        // Mapping fields to SQL parameters
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

        return writer;
    }
}
