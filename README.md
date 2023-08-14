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
{
   "batchId":"xxx.xxx.xx.xx",						<IPV4 Address>
   "cmIp":{
      "v4":"xx.xx.xx.xx"							<IPV4 Address>
   },
   "cmMacAddr":"MACADDR123",
   "cmtsHostName":"TESTRPCC01.ga.at.cox.net",
   "cmtsIp":{
      "v4":"xxx.xxx.xx.xx", 						<Same as batchId>
      "v6":"xxxx:xxx:xexx:x:xxx:xxx:xx:xx"			<IPV6 Address>
   },
   "dsChSet":[
      17,
      18,
      19,
      20,
      21,
      22,
      23,
      24,
      25,
      26,
      27,
      28,
      29,
      30,
      31,
      32,
      33,
      34,
      35,
      36,
      37,
      38,
      39,
      40,
      41,
      42,
      43,
      44,
      45,
      46,
      47,
      48,
      159
   ],
   "dsIdentifier":8000,
   "dsOctets":757040,
   "dsPackets":1570,
   "dsScn":"vidiptvdn",
   "dsTimeActive":58694,
   "fromTime":"2023-05-23T12:15:00Z",
   "mdIfIndex":1149,
   "mdIfName":"Cable3/0/4",
   "qosVersion":2,
   "region":"Atlanta",
   "timeZone":"UTC",
   "toTime":"2023-05-23T12:30:00Z",
   "usChSet":[
      1,
      2,
      3,
      4,
      5,
      6,
      7,
      8
   ],
   "usIdentifier":12907,
   "usOctets":157877,
   "usPackets":1374,
   "usScn":"vidiptvup",
   "usTimeActive":58694,
   "v":1
}
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