package priorityQueue.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.FileReader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;


public class TestUtils {


	private static String timeStamp = null;
	private static Map<String,String> testBenchesNames = new TreeMap<String, String>();


	public static void saveResult(String[] result){


		if(timeStamp==null){
			timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		}

		StringBuffer buff = new StringBuffer();
		for(String s:result)
		{
			buff.append(s);
			buff.append("\t");
		}

		String fileName = "results_"+ timeStamp +".txt";
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(fileName, true));
			output.append(buff.toString() + "\n");
			System.out.println(buff.toString());
			output.flush();
			output.close();
			
			System.out.println(buff.toString());

		} catch (IOException e) {
			System.out.println("Error in print result to file");
			e.printStackTrace();
		}

	}


	public static void saveData(String... result){


		if(timeStamp==null){
			timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		}

		StringBuffer buff = new StringBuffer();
		if(result.length>1){
			buff.append("[");
			for(int i=0;i<result.length;i++)
			{
				buff.append(result[i]);
				//if(i!=result.length-1)
				buff.append(",");
			}
			buff.append("]");
		}else
		{
			buff.append(result[0]);
		}
		String line = buff.toString();
		if(result.length>1)line = line.replace(",]", "],");
		String fileName = "data_"+timeStamp+".html";
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(fileName, true));
			output.append(line + "\n");
			System.out.println(buff.toString());
			output.flush();
			output.close();

		} catch (IOException e) {
			System.out.println("Error in print data to file");
			e.printStackTrace();
		}

	}


	@Test
	public void generateGraphs(){

		String fileName = "results_20150322_202349.txt";
		testBenchesNames.put("B_0", "testBench2");
		testBenchesNames.put("B_1", "testBench5");
		testBenchesNames.put("B_2", "testBench8");
		testBenchesNames.put("B_3", "testBench11");
		testBenchesNames.put("B_4", "testBench14");
		testBenchesNames.put("B_5", "testBench7");
		testBenchesNames.put("B_6", "testBench16");

		testBenchesNames.put("C_0", "testBench3");
		testBenchesNames.put("C_1", "testBench6");
		testBenchesNames.put("C_2", "testBench10");
		testBenchesNames.put("C_3", "testBench13");
		testBenchesNames.put("C_4", "testBench15");
		testBenchesNames.put("C_5", "testBench17");


		//		startHTML();
		saveData("<html>");
		saveData("<head>");
		saveData("  <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
		saveData("   <script type=\"text/javascript\">");
		saveData("	 	google.load('visualization', '1.1', {packages: ['line']})");

		List<String> ids = graphSet1(fileName);
		ids.addAll(graphSet2(fileName));
		ids.addAll(graphSet3(fileName));

		//endHTML();
		saveData("  </script>");
		saveData("  </head>");
		saveData("  <body>");

		for(String id:ids){
			saveData("<div id=\"test"+id+"\"></div>");
			saveData(" <brbrbrbrbrbrbrbr>");
		}
		saveData("</body>");
		saveData("</html>");
	}


	//	@Test
	public List<String> graphSet1(String fileName){

		List<RowEntity> rowData = makeDataForChart(fileName);

		/*------------------ make the 4 graphs sim-balanced for 0-6 tests----------------*/
		Map<String,ChartRow> chartData = new TreeMap<String,ChartRow>();
		//		saveData("QueueType", "Set", "Test", "NumOfInsertWorkers", "NumOfDeleteWorkers", "InsertThroughput", "DeleteThroughput" );
		String[][][] testsScenatios = {{{"1","1"}, {"2","2"} , {"3","3"}, {"4","4"}}};
		String set = "0";
		List<String> ids = new ArrayList<String>();
		String[] tests = {"0","1","2","3","4","5","6"};
		for(String[][] testScenario: testsScenatios){
			for(String test: tests){
				int flag;
				for(RowEntity rowEntry : rowData){
					flag=0;
					String deleteWorkers = rowEntry.getNumOfDeleteWorkers();
					String insertWorkers = rowEntry.getNumOfInsertWorkers();
					for(String[] pair: testScenario){
						if(insertWorkers.equals(pair[0]) && deleteWorkers.equals(pair[1])){
							flag=1;
							break;
						}
					}
					if(!rowEntry.getSet().equals(set)){
						continue;
					}

					if(!rowEntry.getTest().equals(test)){
						continue;
					}
					String x_value = rowEntry.getXValue();
					if(flag==1){
						ChartRow currentRow;
						if(chartData.containsKey(x_value)){
							currentRow = chartData.get(x_value);
						}else{
							currentRow = new ChartRow(5);
						}
						
						if(rowEntry.getQueueType().equals("JavaPriorityBlockingQueue")){
							currentRow.JavaPriorityBlockingQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.JavaPriorityBlockingQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.JavaPriorityBlockingQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.JavaPriorityBlockingQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.JavaPriorityBlockingQueue_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("NaiveLockNativePriorityQueue")){
							currentRow.NaiveLockNativePriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.NaiveLockNativePriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.NaiveLockNativePriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.NaiveLockNativePriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.NaiveLockNativePriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("GlobalLockSprayListPriorityQueue")){
							currentRow.GlobalLockSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.GlobalLockSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.GlobalLockSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueue")){
							currentRow.OptimisticCoolSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("TMSprayListPriorityQueue")){
							currentRow.TMSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.TMSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.TMSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.TMSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.TMSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("LockFreeSprayListPriorityQueue")){
							currentRow.LockFreeSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.LockFreeSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LockFreeSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.LockFreeSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.LockFreeSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueue")){
							currentRow.CoolSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("LazyLockSparyListPriorityQueue")){
							currentRow.LazyLockSparyListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.LazyLockSparyListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LazyLockSparyListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.LazyLockSparyListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.LazyLockSparyListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}

						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueueFairLock")){
							currentRow.CoolSprayListPriorityQueueFairLock_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueueFairLock_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueueFairLock_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueueFairLock_val[3]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueueFairLock_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueueFairLock")){
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[3]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("GlobalLockSprayListPriorityQueue_CPP")){
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("LazyLockSparyListPriorityQueue_CPP")){
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueue_CPP")){
							currentRow.CoolSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueue_CPP")){
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						chartData.put(x_value, currentRow);
					}
				}

				for(int i=0; i<5;i++){

					String id="B_"+test+"_"+i;
					ids.add(id);
					startGraph(id,false);

					for(Entry<String, ChartRow> en: chartData.entrySet()){
						saveData(en.getKey(),en.getValue().JavaPriorityBlockingQueue_val[i], en.getValue().NaiveLockNativePriorityQueue_val[i], en.getValue().LazyLockSparyListPriorityQueue_val[i], en.getValue().GlobalLockSprayListPriorityQueue_val[i], en.getValue().LockFreeSprayListPriorityQueue_val[i], en.getValue().TMSprayListPriorityQueue_val[i], en.getValue().OptimisticCoolSprayListPriorityQueue_val[i], en.getValue().CoolSprayListPriorityQueue_val[i], en.getValue().CoolSprayListPriorityQueueFairLock_val[i], en.getValue().OptimisticCoolSprayListPriorityQueueFairLock_val[i], en.getValue().GlobalLockSprayListPriorityQueue_CPP_val[i], en.getValue().LazyLockSparyListPriorityQueue_CPP_val[i], en.getValue().CoolSprayListPriorityQueue_CPP_val[i], en.getValue().OptimisticCoolSprayListPriorityQueue_CPP_val[i]);
					}

					endGraph(id,i, false);
				}
			}
		}
		return ids;
	}


	//	@Test
	public List<String> graphSet2(String fileName){

		List<RowEntity> rowData = makeDataForChart(fileName);

		/*------------------ make the 4 graphs sim-unbalanced for 0-6 tests----------------*/
		//		Map<String,ChartRow> chartData = new TreeMap<String,ChartRow>();
		//		saveData("QueueType", "Set", "Test", "NumOfInsertWorkers", "NumOfDeleteWorkers", "InsertThroughput", "DeleteThroughput" );
		String[][][] testsScenatios = {{ {"1","1"}, {"1","3"} , {"1","7"}},{ {"1","1"}, {"3","1"} , {"7","1"}}};//, {5,5}, {6,6}, {8,8}}
		String set = "0";
		int testScenarioCount=0;
		List<String> ids = new ArrayList<String>();
		String[] tests = {"0","1","2","3","4","5","6"};
		for(String[][] testScenario: testsScenatios){
			testScenarioCount++;
			Map<String,ChartRow> chartData = new TreeMap<String,ChartRow>();
			for(String test: tests){
				int flag;
				for(RowEntity rowEntry : rowData){
					flag=0;
					String deleteWorkers = rowEntry.getNumOfDeleteWorkers();
					String insertWorkers = rowEntry.getNumOfInsertWorkers();
					for(String[] pair: testScenario){
						if(insertWorkers.equals(pair[0]) && deleteWorkers.equals(pair[1])){
							flag=1;
							break;
						}
					}

					if(!rowEntry.getSet().equals(set)){
						continue;
					}

					if(!rowEntry.getTest().equals(test)){
						continue;
					}

					String x_value = rowEntry.getXValue_2();
					if(flag==1){
						ChartRow currentRow;
						if(chartData.containsKey(x_value)){
							currentRow = chartData.get(x_value);
						}else{
							currentRow = new ChartRow(4);
						}
						
						if(rowEntry.getQueueType().equals("JavaPriorityBlockingQueue")){
							currentRow.JavaPriorityBlockingQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.JavaPriorityBlockingQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.JavaPriorityBlockingQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.JavaPriorityBlockingQueue_val[3]=rowEntry.getAvgGrade();
						}

						if(rowEntry.getQueueType().equals("NaiveLockNativePriorityQueue")){
							currentRow.NaiveLockNativePriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.NaiveLockNativePriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.NaiveLockNativePriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.NaiveLockNativePriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("GlobalLockSprayListPriorityQueue")){
							currentRow.GlobalLockSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.GlobalLockSprayListPriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueue")){
							currentRow.OptimisticCoolSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("TMSprayListPriorityQueue")){
							currentRow.TMSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.TMSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.TMSprayListPriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.TMSprayListPriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("LockFreeSprayListPriorityQueue")){
							currentRow.LockFreeSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.LockFreeSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LockFreeSprayListPriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.LockFreeSprayListPriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueue")){
							currentRow.CoolSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("LazyLockSparyListPriorityQueue")){
							currentRow.LazyLockSparyListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.LazyLockSparyListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LazyLockSparyListPriorityQueue_val[2]=rowEntry.getAvgGrade();
							currentRow.LazyLockSparyListPriorityQueue_val[3]=rowEntry.getVarianceGrade();
						}

						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueueFairLock")){
							currentRow.CoolSprayListPriorityQueueFairLock_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueueFairLock_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueueFairLock_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueueFairLock_val[3]=rowEntry.getAvgGrade();
						}
						
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueueFairLock")){
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[3]=rowEntry.getAvgGrade();
						}
						
						if(rowEntry.getQueueType().equals("GlobalLockSprayListPriorityQueue_CPP")){
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
						}
						
						if(rowEntry.getQueueType().equals("LazyLockSparyListPriorityQueue_CPP")){
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
						}
						
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueue_CPP")){
							currentRow.CoolSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
						}
						
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueue_CPP")){
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
						}
						
						chartData.put(x_value, currentRow);
					}
				}

				for(int i=0; i<4;i++){
					String id="B_"+test+"_"+i+"_"+testScenarioCount;
					ids.add(id);
					startGraph(id,true);

					for(Entry<String, ChartRow> en: chartData.entrySet()){
						saveData(en.getKey(),en.getValue().JavaPriorityBlockingQueue_val[i], en.getValue().NaiveLockNativePriorityQueue_val[i], en.getValue().LazyLockSparyListPriorityQueue_val[i], en.getValue().GlobalLockSprayListPriorityQueue_val[i], en.getValue().LockFreeSprayListPriorityQueue_val[i], en.getValue().TMSprayListPriorityQueue_val[i], en.getValue().OptimisticCoolSprayListPriorityQueue_val[i], en.getValue().CoolSprayListPriorityQueue_val[i], en.getValue().CoolSprayListPriorityQueueFairLock_val[i], en.getValue().OptimisticCoolSprayListPriorityQueueFairLock_val[i], en.getValue().GlobalLockSprayListPriorityQueue_CPP_val[i], en.getValue().LazyLockSparyListPriorityQueue_CPP_val[i], en.getValue().CoolSprayListPriorityQueue_CPP_val[i], en.getValue().OptimisticCoolSprayListPriorityQueue_CPP_val[i]);
					}
					endGraph(id,i, true);
				}
			}
		}
		return ids;
	}

	//	@Test
	public List<String> graphSet3(String fileName){

		List<RowEntity> rowData = makeDataForChart(fileName);

		/*------------------ make the 4 graphs sim-unbalanced for 0-6 tests----------------*/
		//		Map<String,ChartRow> chartData = new TreeMap<String,ChartRow>();
		//		saveData("QueueType", "Set", "Test", "NumOfInsertWorkers", "NumOfDeleteWorkers", "InsertThroughput", "DeleteThroughput" );
		String[][][] testsScenatios = {{{"1","1"}, {"2","2"} , {"3","3"}, {"4","4"}, {"5","5"}, {"6","6"}, {"7","7"} ,{"8","8"}}};
		String set = "1";
		List<String> ids = new ArrayList<String>();
		String[] tests = {"0","1","2","3","4","5"};
		for(String[][] testScenario: testsScenatios){
			Map<String,ChartRow> chartData = new TreeMap<String,ChartRow>();
			for(String test: tests){
				int flag;
				for(RowEntity rowEntry : rowData){
					flag=0;
					String deleteWorkers = rowEntry.getNumOfDeleteWorkers();
					String insertWorkers = rowEntry.getNumOfInsertWorkers();
					for(String[] pair: testScenario){
						if(insertWorkers.equals(pair[0]) && deleteWorkers.equals(pair[1])){
							flag=1;
							break;
						}
					}
					if(!rowEntry.getSet().equals(set)){
						continue;
					}
					if(!rowEntry.getTest().equals(test)){
						continue;
					}
					String x_value = rowEntry.getXValue();
					if(flag==1){
						ChartRow currentRow;
						if(chartData.containsKey(x_value)){
							currentRow = chartData.get(x_value);
						}else{
							currentRow = new ChartRow(5);
						}
						
						if(rowEntry.getQueueType().equals("JavaPriorityBlockingQueue")){
							currentRow.JavaPriorityBlockingQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.JavaPriorityBlockingQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.JavaPriorityBlockingQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.JavaPriorityBlockingQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.JavaPriorityBlockingQueue_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("NaiveLockNativePriorityQueue")){
							currentRow.NaiveLockNativePriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.NaiveLockNativePriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.NaiveLockNativePriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.NaiveLockNativePriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.NaiveLockNativePriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("GlobalLockSprayListPriorityQueue")){
							currentRow.GlobalLockSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.GlobalLockSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.GlobalLockSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueue")){
							currentRow.OptimisticCoolSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("TMSprayListPriorityQueue")){
							currentRow.TMSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.TMSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.TMSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.TMSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.TMSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("LockFreeSprayListPriorityQueue")){
							currentRow.LockFreeSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.LockFreeSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LockFreeSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.LockFreeSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.LockFreeSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueue")){
							currentRow.CoolSprayListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("LazyLockSparyListPriorityQueue")){
							currentRow.LazyLockSparyListPriorityQueue_val[0]=rowEntry.getInsertThroughput();
							currentRow.LazyLockSparyListPriorityQueue_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LazyLockSparyListPriorityQueue_val[2]=rowEntry.getThroughputRatio();
							currentRow.LazyLockSparyListPriorityQueue_val[3]=rowEntry.getAvgGrade();
							currentRow.LazyLockSparyListPriorityQueue_val[4]=rowEntry.getVarianceGrade();
						}
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueueFairLock")){
							currentRow.CoolSprayListPriorityQueueFairLock_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueueFairLock_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueueFairLock_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueueFairLock_val[3]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueueFairLock_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueueFairLock")){
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[3]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueueFairLock_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("GlobalLockSprayListPriorityQueue_CPP")){
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.GlobalLockSprayListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("LazyLockSparyListPriorityQueue_CPP")){
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.LazyLockSparyListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("CoolSprayListPriorityQueue_CPP")){
							currentRow.CoolSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.CoolSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.CoolSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.CoolSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.CoolSprayListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						if(rowEntry.getQueueType().equals("OptimisticCoolSprayListPriorityQueue_CPP")){
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[0]=rowEntry.getInsertThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[1]=rowEntry.getDeleteThroughput();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[2]=rowEntry.getThroughputRatio();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[3]=rowEntry.getAvgGrade();
							currentRow.OptimisticCoolSprayListPriorityQueue_CPP_val[4]=rowEntry.getVarianceGrade();
						}
						
						chartData.put(x_value, currentRow);
					}
				}

				for(int i=0; i<5;i++){
					String id="C_"+test+"_"+i;
					ids.add(id);
					startGraph(id,false);

					for(Entry<String, ChartRow> en: chartData.entrySet()){
						saveData(en.getKey(), en.getValue().JavaPriorityBlockingQueue_val[i], en.getValue().NaiveLockNativePriorityQueue_val[i], en.getValue().LazyLockSparyListPriorityQueue_val[i], en.getValue().GlobalLockSprayListPriorityQueue_val[i], en.getValue().LockFreeSprayListPriorityQueue_val[i], en.getValue().TMSprayListPriorityQueue_val[i], en.getValue().OptimisticCoolSprayListPriorityQueue_val[i], en.getValue().CoolSprayListPriorityQueue_val[i], en.getValue().CoolSprayListPriorityQueueFairLock_val[i], en.getValue().OptimisticCoolSprayListPriorityQueueFairLock_val[i], en.getValue().GlobalLockSprayListPriorityQueue_CPP_val[i], en.getValue().LazyLockSparyListPriorityQueue_CPP_val[i], en.getValue().CoolSprayListPriorityQueue_CPP_val[i], en.getValue().OptimisticCoolSprayListPriorityQueue_CPP_val[i]);
					}
					endGraph(id,i, false);
				}
			}
		}
		return ids;
	}


	private void endGraph(String id, int i, boolean flag) {
		saveData("");
		saveData("		]);");
		saveData("");
		saveData("var options = {");
		saveData("chart: {");
		saveData("title: '"+getTestRule(i,flag)+"',");
		saveData("subtitle: '"+getTestBenchName(id)+"'");
		saveData("},");
		saveData("width: 1100,");
		saveData("height: 600");
		saveData(" };");
		saveData("");
		saveData("var chart_"+id+" = new google.charts.Line(document.getElementById('test"+id+"'));");
		saveData("chart_"+id+".draw(data, options);");
		saveData("}");
	}


	private String getTestRule(int i, boolean flag) {

		if(i==0){
			return "Insert Thouroput" ;
		}
		if(i==1){
			return "Delete Thouroput" ;
		}
		if(flag){
			if(i==2){
				return "Average Grade";
			}
			if(i==3){
				return "Variance Grade";
			}

		}
		if(i==2){
			return "Insert to Delete Thouroput logarithmic Ratio" ;
		}
		if(i==3){
			return "Average Grade" ;
		}

		return "Variance Grade";
	}


	private String getTestBenchName(String id) {
		String value = null;
		for(Entry<String, String> entry : testBenchesNames.entrySet()){
			String key = entry.getKey();
			if(id.toLowerCase().contains(key.toLowerCase())){
				value=entry.getValue();
				break;
			}
		}
		return value;
	}


	private void startGraph(String id,boolean flag) {
		saveData("google.setOnLoadCallback(drawChart_"+id+");");
		saveData("function drawChart_"+id+"() {");
		saveData("      var data = new google.visualization.DataTable();");
		if(!flag){
			saveData("      data.addColumn('string', 'Threads');");
		}
		else{
			saveData("      data.addColumn('string', 'Threads(Insert,Delete)');");
		}
		saveData("      data.addColumn('number', 'JavaPriorityBlockingQueue');");
		saveData("      data.addColumn('number', 'NaiveLockNativePriorityQueue');");
		saveData("      data.addColumn('number', 'LazyLockSparyListPriorityQueue');");
		saveData("      data.addColumn('number', 'GlobalLockSprayListPriorityQueue');");
		saveData("      data.addColumn('number', 'LockFreeSprayListPriorityQueue');");
		saveData("      data.addColumn('number', 'TMSprayListPriorityQueue');");
		saveData("      data.addColumn('number', 'OptimisticCoolSprayListPriorityQueue');");
		saveData("      data.addColumn('number', 'CoolSprayListPriorityQueue');");
		saveData("      data.addColumn('number', 'CoolSprayListPriorityQueueFairLock');");
		saveData("      data.addColumn('number', 'OptimisticCoolSprayListPriorityQueueFairLock');");
		//cpp		
		saveData("      data.addColumn('number', 'GlobalLockSprayListPriorityQueue_CPP');");
		saveData("      data.addColumn('number', 'LazyLockSparyListPriorityQueue_CPP');");
		saveData("      data.addColumn('number', 'CoolSprayListPriorityQueue_CPP');");
		saveData("      data.addColumn('number', 'OptimisticCoolSprayListPriorityQueue_CPP');");

		saveData("");
		saveData("      data.addRows([");


	}


	public List<RowEntity> makeDataForChart(String fileName){
		List<RowEntity> rowData =new ArrayList<RowEntity>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName)); 
			String line;
			while ((line = br.readLine()) != null) {
				String[] row = line.split("\t");
				if(row.length>1){
					if(row[0].contains("type")){
						continue;
					}
				}
				String[] cleanedRow = cleanRow(row);
				RowEntity rowEntity = new RowEntity(cleanedRow);
				rowData.add(rowEntity);
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Error in reading result to file");
			e.printStackTrace();
		}
		return rowData;
	}

	private String[] cleanRow(String[] row) {
		//check whether the end is " " or empty
		if(row[row.length-1].isEmpty() || row[row.length-1].equals(" ")){
			String[] cleanedRow = new String[row.length-1];
			for(int i=0;i<cleanedRow.length;i++){
				cleanedRow[i]=row[i];
			}
			return cleanedRow;
		}
		return row;
	}

	public class ChartRow{

		//		insert,delete, avg, var;
		String[] JavaPriorityBlockingQueue_val;
		String[] NaiveLockNativePriorityQueue_val;
		String[] LazyLockSparyListPriorityQueue_val;
		String[] GlobalLockSprayListPriorityQueue_val;
		String[] OptimisticCoolSprayListPriorityQueue_val;
		String[] TMSprayListPriorityQueue_val;
		String[] LockFreeSprayListPriorityQueue_val;
		String[] CoolSprayListPriorityQueue_val;
		String[] CoolSprayListPriorityQueueFairLock_val;
		String[] OptimisticCoolSprayListPriorityQueueFairLock_val;
		String[] GlobalLockSprayListPriorityQueue_CPP_val;
		String[] LazyLockSparyListPriorityQueue_CPP_val;
		String[] CoolSprayListPriorityQueue_CPP_val;
		String[] OptimisticCoolSprayListPriorityQueue_CPP_val;
		

		public ChartRow(int size){
			JavaPriorityBlockingQueue_val = new String[size];
			NaiveLockNativePriorityQueue_val = new String[size];
			LazyLockSparyListPriorityQueue_val = new String[size];
			GlobalLockSprayListPriorityQueue_val = new String[size];
			OptimisticCoolSprayListPriorityQueue_val = new String[size];
			TMSprayListPriorityQueue_val = new String[size];
			LockFreeSprayListPriorityQueue_val = new String[size];
			CoolSprayListPriorityQueue_val = new String[size];
			CoolSprayListPriorityQueueFairLock_val = new String[size];
			OptimisticCoolSprayListPriorityQueueFairLock_val = new String[size];
			GlobalLockSprayListPriorityQueue_CPP_val = new String[size];
			LazyLockSparyListPriorityQueue_CPP_val = new String[size];
			CoolSprayListPriorityQueue_CPP_val = new String[size];
			OptimisticCoolSprayListPriorityQueue_CPP_val = new String[size];

		}
	}

	public class RowEntity{

		private String queueType;
		private String set;
		private String threads; 
		private String test;
		private String numOfInsertWorkers;
		private String numOfDeleteWorkers;
		private String insertCount;
		private String insertTime;
		private String deleteCount;
		private String deleteTime;
		private String timeOut;
		private String highest;
		private String height;
		private int[] grades;

		//		public RowEntity(String queueType,	String set, String threads, String test, String	numOfInsertWorkers, String numOfDeleteWorkers, String	insertCount, String insertTime, String deleteCount, String deleteTime, String timeOut, String highest, String height, String grades){
		public RowEntity(String[] row){
			this.setQueueType(row[0]);
			this.setSet(row[1]);
			this.setThreads(row[2]); 
			this.setTest(row[3]);
			this.setNumOfInsertWorkers(row[4]);
			this.setNumOfDeleteWorkers(row[5]);
			this.setInsertCount(row[6]);
			this.setInsertTime(row[7]);
			this.setDeleteCount(row[8]);
			this.setDeleteTime(row[9]);
			this.setTimeOut(row[10]);
			this.setHighest(row[11]);
			this.setHeight(row[12]);
			if(row.length>13){
				grades = new int[row.length-13];
				for(int i=0;i<grades.length;i++){
					grades[i]=Integer.parseInt(row[i+13]);
				}
			}

		}

		public String getQueueType() {
			return queueType;
		}

		public void setQueueType(String queueType) {
			this.queueType = queueType;
		}

		public String getSet() {
			return set;
		}

		public void setSet(String set) {
			this.set = set;
		}

		public String getThreads() {
			return threads;
		}

		public void setThreads(String threads) {
			this.threads = threads;
		}

		public String getTest() {
			return test;
		}

		public void setTest(String test) {
			this.test = test;
		}

		public String getNumOfInsertWorkers() {
			return numOfInsertWorkers;
		}

		public void setNumOfInsertWorkers(String numOfInsertWorkers) {
			this.numOfInsertWorkers = numOfInsertWorkers;
		}

		public String getNumOfDeleteWorkers() {
			return numOfDeleteWorkers;
		}

		public void setNumOfDeleteWorkers(String numOfDeleteWorkers) {
			this.numOfDeleteWorkers = numOfDeleteWorkers;
		}

		public String getInsertCount() {
			return insertCount;
		}

		public void setInsertCount(String insertCount) {
			this.insertCount = insertCount;
		}

		public String getInsertTime() {
			return insertTime;
		}

		public void setInsertTime(String insertTime) {
			this.insertTime = insertTime;
		}

		public String getDeleteCount() {
			return deleteCount;
		}

		public void setDeleteCount(String deleteCount) {
			this.deleteCount = deleteCount;
		}

		public String getDeleteTime() {
			return deleteTime;
		}

		public void setDeleteTime(String deleteTime) {
			this.deleteTime = deleteTime;
		}

		public String getTimeOut() {
			return timeOut;
		}

		public void setTimeOut(String timeOut) {
			this.timeOut = timeOut;
		}

		public String getHighest() {
			return highest;
		}

		public void setHighest(String highest) {
			this.highest = highest;
		}

		public String getHeight() {
			return height;
		}

		public void setHeight(String height) {
			this.height = height;
		}

		public String getThroughputRatio(){
			long insertThroughput = Long.parseLong(this.getInsertThroughput());
			long deleteThroughput = Long.parseLong(this.getDeleteThroughput());

			if(insertThroughput!=0&&deleteThroughput!=0){
				double ratio = (double) insertThroughput/deleteThroughput;
				//				return Long.toString((insertThroughput/deleteThroughput));
				return Double.toString(Math.log(ratio));
			}
			return "0";
		}

		public String getInsertThroughput(){
			long insertCount = Long.parseLong(this.insertCount);
			long insertTime = Long.parseLong(this.insertTime);

			return Long.toString(insertCount/insertTime);	
		}

		public String getDeleteThroughput(){
			long deleteCount = Long.parseLong(this.deleteCount);
			long deleteTime = Long.parseLong(this.deleteTime);

			return Long.toString(deleteCount/deleteTime);	
		}

		public String getAvgGrade(){
			if(grades==null){
				return "0";
			}
			if(grades.length==0){
				return "0";
			}
			int sum=0;
			for(int grade: grades){
				sum+=grade;
			}

			int avg = (int) (sum/grades.length);

			return Integer.toString(avg);
		}

		public String getVarianceGrade(){
			if(grades==null){
				return "0";
			}
			if(grades.length==0){
				return "0";
			}
			int mean = Integer.parseInt(getAvgGrade());
			double temp = 0;
			for(int a :grades){
				temp += (mean-a)*(mean-a);
			}

			int variance = (int) (temp/grades.length);

			return Integer.toString(variance);
		}

		public String getXValue(){
			return "'" + numOfDeleteWorkers+ "'";

		}

		public String getXValue_2(){
			return "'(" + numOfInsertWorkers+ ","+ numOfDeleteWorkers +")'";
		}



	}

}
