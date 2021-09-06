package com.kinginsai.wc

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.graph.scala._
import org.apache.flink.graph.{Edge, Vertex}
import org.apache.flink.graph.spargel.{GatherFunction, MessageIterator, ScatterFunction}

import scala.collection.JavaConversions._

object SocialGraph {
  private var srcVertexId = 1L
  private var edgesInputPath: String = null
  private var maxIterations = 50

  def main(args: Array[String]): Unit = {
    if (!parseParameters(args)) {
      return
    }

    val env = ExecutionEnvironment.getExecutionEnvironment
    if (edgesInputPath == null) {
      val resource = this.getClass.getClassLoader.getResource("")
      edgesInputPath = resource + "socialgraph5002.dat"
    }

    val inputDataSet: DataSet[(String, String)] = env.readCsvFile[(String, String)](edgesInputPath,
      lineDelimiter = "\n",
      fieldDelimiter = "|",
      ignoreFirstLine = true,
      lenient = true)
      .map { pair => (pair._1.trim ,pair._2.trim) }
    val srcVertexId1: DataSet[String] = inputDataSet.distinct().map{ pair => pair._1}
    val srcVertexId2: DataSet[String] = inputDataSet.distinct().map{ pair => pair._2}
    var srcVertex:  DataSet[String] = srcVertexId1.union(srcVertexId2).distinct()

    val vertices = srcVertex.map { x => new Vertex(x.toLong, 1.0)}
    val edges   = inputDataSet.map { pair => new Edge(pair._1.toLong, pair._2.toLong, 1.0)}

    val graph = Graph.fromDataSet(edges, new InitVertices(srcVertexId), env)
    // Execute the scatter-gather iteration
    val result = graph.runScatterGatherIteration(new MinDistanceMessenger, new VertexDistanceUpdater, maxIterations)
    // Extract the vertices as the result
    val singleSourceShortestPaths = result.getVertices
      .filter { _.getValue != Double.PositiveInfinity }
      .map { pair => ( pair.getId, pair.getValue) }
      .maxBy(1)
      .map { pair => pair._2 }

    singleSourceShortestPaths.print()
  }



  private final class InitVertices(srcId: Long) extends MapFunction[Long, Double] {

    override def map(id: Long) = {
      if (id.equals(srcId)) {
        0.0
      } else {
        Double.PositiveInfinity
      }
    }
  }

  /**
   * Distributes the minimum distance associated with a given vertex among all
   * the target vertices summed up with the edge's value.
   */
  private final class MinDistanceMessenger extends
    ScatterFunction[Long, Double, Double, Double] {

    override def sendMessages(vertex: Vertex[Long, Double]) {
      if (vertex.getValue < Double.PositiveInfinity) {
        for (edge: Edge[Long, Double] <- getEdges ){
          sendMessageTo(edge.getTarget, vertex.getValue + edge.getValue)
        }
      }
    }
  }

  /**
   * Function that updates the value of a vertex by picking the minimum
   * distance from all incoming messages.
   */
  private final class VertexDistanceUpdater extends GatherFunction[Long, Double, Double] {

    override def updateVertex(vertex: Vertex[Long, Double], inMessages: MessageIterator[Double]) {
      var minDistance = Double.MaxValue
      while (inMessages.hasNext) {
        val msg = inMessages.next
        if (msg < minDistance) {
          minDistance = msg
        }
      }
      if (vertex.getValue > minDistance) {
        setNewVertexValue(minDistance)
      }
    }
  }

  private def parseParameters(args: Array[String]): Boolean = {
    if(args.length > 0 && args.length != 3) {
      System.out.println("Usage: SingleSourceShortestPaths <source vertex id>" +
        " <input edges path> <num iterations>")
      return false
    }
    if(args.length == 3) {
      srcVertexId = args(0).toLong
      edgesInputPath = args(1)
      maxIterations = args(2).toInt
    }
    true
  }
}
