# IPDR Hourly/Daily RollUp Example with Flink DataStream API 

## Main logic:
1) Read IPDR messages as an input stream from the Kafka queue, deserialize JSON to POJO
2) Filter the input stream based on dsScn service type as "data"
3) Apply the Map function and build a new output usage message. Convert fromDate field from String to Date and extract Hour
4) Apply the keyBy operator and group by MacAddr and Hour
5) Apply a Tumbling Window function with a certain Window size. Window size is defined in minutes. It could be changed to milliseconds, seconds, hours or days. [More info on Flink Window functions is available here](
   https://nightlies.apache.org/flink/flink-docs-master/docs/dev/datastream/operators/windows/)
6) Apply Aggregator and sum dsOctets to calculate usage for each given MacAddr and Hour.
7) Route IPDR output usage messages to the local FileSystem (or HDFS) and a Kafka queue sink.

Job configuration parameters are specified in the job.properties file.

## Usage on Secured SASL_SSL (Kerberos) Cluster:

To run the command on secured cluster:

```
flink run -yD security.kerberos.login.keytab=<keytab file> -yD security.kerberos.login.principal=<principal_name> -d -p 1 -ys 2 -ynm StreamingIPDRJob target/IPDRStreamingFlink.jar config/job.properties
```

Note: This was tested on CDP Public Cloud Data Hub cluster. 

## Usage on Unsecured Cluster:

```
flink run -d -p 1 -ys 1 -ytm 1500 -ynm StreamingJob target/IPDRStreamingFlink.jar config/job.properties
```

## Running a Load Test on a 6-node Flink/Kafka cluster

I ran a load test of this code processing data from 50,000 distinct Mac addresses coming over the past 2 hours. With 5 concurrent processes generating
IPDR data, Flink Map task utilization reached only 4%. 

![img.png](img.png)

![img_1.png](img_1.png)

## Sample IPDR input Message Format:

```
{"batchId":"103.65.107.58",
"cmIp":{"v4":"198.75.91.98"},
"cmMacAddr":"MACADDR39",
"cmtsHostName":"schaefer.info",
"cmtsIp":{"v4":"103.65.107.58","v6":"cb20:8db0:c3bf:247b:d593:a9f2:1c2f:50af"},
"dsChSet":[31,70,79,26],
"dsIdentifier":5957,
"dsOctets":635159,
"dsPackets":6099,
"dsScn":"default",
"dsTimeActive":35962,
"fromTime":"2023-08-01 19:15:00",
"mdIfIndex":2869,
"mdIfName":"Cable2/1/6",
"qosVersion":3,
"region":"Charlotte",
"timeZone":"EST",
"toTime":"2023-08-01 19:30:00",
"usChSet":[0,6,9,9,8,0,3],
"usIdentifier":89100,
"usOctets":584150,
"usPackets":23106,
"usScn":"default",
"usTimeActive":21239,
"v":2}
```
## Sample IPDR Usage Output Format:

```
{
"hourUsage":14,
"cmtshost":"turner.biz",
"scn":"data",
"mac":"MACADDR37076",
"usage":808697
}
```