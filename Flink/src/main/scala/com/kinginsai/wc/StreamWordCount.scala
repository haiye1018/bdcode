package com.kinginsai.wc

import org.apache.flink.streaming.api.scala._


// 流处理word count
object StreamWordCount {
  def main(args: Array[String]): Unit = {
    // 创建流处理的执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // 接收一个socket文本流
    val inputDataStream: DataStream[String] = env.socketTextStream("app-11", 7777)
    // 进行转化处理统计
    val resultDataStream: DataStream[(String, Int)] = inputDataStream
      .flatMap(_.split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    resultDataStream.print().setParallelism(1)

    // 启动任务执行
    env.execute("stream word count")
  }
}
