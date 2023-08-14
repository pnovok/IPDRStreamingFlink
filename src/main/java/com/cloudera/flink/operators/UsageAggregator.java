package com.cloudera.flink.operators;

import com.cloudera.flink.model.OutputUsageMessages;

public class UsageAggregator implements org.apache.flink.api.common.functions.ReduceFunction<OutputUsageMessages> {
    @Override
    public OutputUsageMessages reduce(OutputUsageMessages value1, OutputUsageMessages value2) {

        int hour1 = value1.getHourUsage();
        int hour2 = value2.getHourUsage();

   //     if (hour1 == hour2)
            return new OutputUsageMessages(
                    value1.getHourUsage(),
                    value1.getCmtshost(),
                    value1.getScn(),
                    value1.getMac(),
                    value1.getUsage() + value2.getUsage());

//        else
//            return new OutputUsageMessages(
//                    value1.getTime(),
//                    value1.getCmtshost(),
//                    value1.getScn(),
//                    value1.getMac(),
//                    value1.getUsage());

    }
}
