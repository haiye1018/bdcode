/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.examples;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.lang.ObjectUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
// hadoop fsck /tmax => blocks == maps
/* Annotated code package org.apache.hadoop.examples;
   cp /projects/bdcode/WordCount/src/main/java/org/apache/hadoop/examples/TMax.java /workspace_logs/
   hadoopc1/hadoopc1==>New terminal==>sudo /bin/bash ==>su - hadoop
   export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
   cp /workspace_logs/TMax.java ./
   hadoop com.sun.tools.javac.Main TMax.java
   jar cf TMax.jar TMax*.class
   hadoop jar TMax.jar TMax /user/hive/warehouse/test.db/ncdc /out
   result: hdfs dfs -cat /out/part-r-00000
*/
public class TMax {

    public static class TMaxMapper
            extends Mapper<Object, Text, Text, IntWritable> {
        //private IntWritable zero = new IntWritable(0);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            String[] strs = value.toString().split(",");
            String token = strs[2];
            int temperature = Integer.parseInt(strs[3]);
            if (token != null && (token.equals("TMAX") || token.equals("TMIN"))) {
                word.set(token);
                context.write(word, new IntWritable(temperature));
            }
        }
    }

    public static class TMaxMinReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int max = 0;
            int min = 0;
            String keyString = key.toString();
            for (IntWritable val : values) {
                if (keyString.equals("TMAX")) {
                    if (val.get() > max){
                        max = val.get();
                    }
                }
                else if (keyString.equals("TMIN")) {
                    if (val.get() < min){
                        min = val.get();
                    }
                }
            }
            if (keyString.equals("TMAX")) {
                result.set(max);
                context.write(key, result);
            }
            else if (keyString.equals("TMIN")) {
                result.set(min);
                context.write(key, result);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length < 2) {
            System.err.println("Usage: TMax <in> [<in>...] <out>");
            System.exit(2);
        }
        Job job = Job.getInstance(conf, "TMax");
        job.setJarByClass(TMax.class);
        job.setMapperClass(TMaxMapper.class);
        job.setCombinerClass(TMaxMinReducer.class);
        job.setReducerClass(TMaxMinReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        for (int i = 0; i < otherArgs.length - 1; ++i) {
            FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
        }
        FileOutputFormat.setOutputPath(job,
                new Path(otherArgs[otherArgs.length - 1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
