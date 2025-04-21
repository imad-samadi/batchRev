package com.example.batchRev.commons;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.partition.support.PartitionNameProvider;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public abstract class AbstractPartitioner implements Partitioner, PartitionNameProvider, InitializingBean {

    public static final String PARTITION_NAME_PREFIX = "partition";
    public static final String PARTITION_DATA_VALUE_SEPARATOR = ",";

    protected final BatchProperties batchProperties;
    protected final String partitionDataKeyName;

    protected AbstractPartitioner(BatchProperties batchProperties, String partitionDataKeyName) {
        this.batchProperties = batchProperties;
        this.partitionDataKeyName = partitionDataKeyName;
    }

    protected abstract List<String> partitioningList();

    @Override
    public Map<String, ExecutionContext> partition(final int gridSize) {
        log.info("Grid size: {}", gridSize);
        List<String> partitioningList = this.partitioningList();

        log.info("Number of records in partitioning list: {}",
                CollectionUtils.isNotEmpty(partitioningList) ? partitioningList.size() : 0);

        if (CollectionUtils.isNotEmpty(partitioningList)) {
            Map<String, ExecutionContext> partitionContextMap = new HashMap<>(gridSize);

            int sourceLen = partitioningList.size();
            int partitionSize = calculatePartitionSize(sourceLen, gridSize);

            List<List<String>> partitions = partitionList(partitioningList, partitionSize);
            log.info("Number of partitions created: {}", partitions.size());

            List<String> partitionNames = this.getPartitionNames(partitions.size());

            for (int i = 0; i < partitions.size(); i++) {
                String partitionData = String.join(PARTITION_DATA_VALUE_SEPARATOR, partitions.get(i));
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.put(this.partitionDataKeyName, partitionData);
                partitionContextMap.put(partitionNames.get(i), executionContext);
            }
            return partitionContextMap;
        }
        return Collections.emptyMap();
    }

    private int calculatePartitionSize(int sourceLen, int gridSize) {
        if (sourceLen > batchProperties.getTriggerPartitioningThreshold()) {
            return (int) Math.ceil((double) sourceLen / gridSize);
        }
        return sourceLen; // Treat as single partition if below threshold
    }

    private List<List<String>> partitionList(List<String> list, int partitionSize) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += partitionSize) {
            partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
        }
        return partitions;
    }

    @Override
    public List<String> getPartitionNames(int gridSize) {
        return IntStream.range(0, gridSize)
                .mapToObj(i -> PARTITION_NAME_PREFIX + "-" + (i + 1))
                .toList();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.state(this.batchProperties != null, "'batchProperties' is required");
        Assert.hasText(this.partitionDataKeyName, "'partitionDataKeyName' is required");
    }
}
