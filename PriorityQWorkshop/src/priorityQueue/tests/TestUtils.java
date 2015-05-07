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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;
import org.omg.CosNaming.IstringHelper;


public class TestUtils {


	private static String timeStamp = null;
	private static Map<String,String> testBenchesNames = new TreeMap<String, String>();


	@Test
	public void generateGraphs(){

		//	String queues[] = {"JavaPriorityBlockingQueue", "NaiveLockNativePriorityQueue", "LazyLockSparyListPriorityQueue", "GlobalLockSprayListPriorityQueue", "LockFreeSprayListPriorityQueue","TMSprayListPriorityQueue", "CoolSprayListPriorityQueue","CoolSprayListPriorityQueueFairLock" ,"OptimisticCoolSprayListPriorityQueue", "OptimisticCoolSprayListPriorityQueueFairLock", "GlobalLockSprayListPriorityQueue_CPP", "LazyLockSparyListPriorityQueue_CPP", "CoolSprayListPriorityQueue_CPP", "OptimisticCoolSprayListPriorityQueue_CPP"};
		String queues[] = { "GlobalLockSprayListPriorityQueue" ,"TMSprayListPriorityQueue", "CoolSprayListPriorityQueue","CoolSprayListPriorityQueueFairLock" ,"OptimisticCoolSprayListPriorityQueue", "OptimisticCoolSprayListPriorityQueueFairLock","LockFreeSprayListPriorityQueue", "GlobalLockSprayListPriorityQueue_CPP", "CoolSprayListPriorityQueue_CPP", "CoolSprayListPriorityQueueFairLock_CPP"};
		//String queues[] = {"JavaPriorityBlockingQueue", "NaiveLockNativePriorityQueue","GlobalLockSprayListPriorityQueue", "TMSprayListPriorityQueue", "LazyLockSparyListPriorityQueue", "CoolSprayListPriorityQueue","CoolSprayListPriorityQueueFairLock" ,"OptimisticCoolSprayListPriorityQueue", "OptimisticCoolSprayListPriorityQueueFairLock",  "LockFreeSprayListPriorityQueue", "GlobalLockSprayListPriorityQueue_CPP", "CoolSprayListPriorityQueue_CPP", "CoolSprayListPriorityQueueFairLock_CPP"};
		//String queues[] = {"CoolSprayListPriorityQueue", "CoolSprayListPriorityQueueFairLock", "OptimisticCoolSprayListPriorityQueue", "OptimisticCoolSprayListPriorityQueueFairLock"};
		//String queues[] = {"CoolSprayListPriorityQueue", "CoolSprayListPriorityQueueFairLock", "OptimisticCoolSprayListPriorityQueue", "OptimisticCoolSprayListPriorityQueueFairLock"};
		//String queues[] = {"GlobalLockSprayListPriorityQueue_CPP", "CoolSprayListPriorityQueue_CPP", "CoolSprayListPriorityQueueFairLock_CPP"};
		String fileName = "results_all_with_java.txt";

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
		saveToHTML("<html>");
		saveToHTML("<head>");
		saveToHTML("  <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
		saveToHTML("  <script src=\"priorityQueueGraphs_"+timeStamp+".js\">/</script>");
		saveToHTML("  <script>");		
		saveToHTML("	 	google.load('visualization', '1.1', {packages: ['line']})");

		String[][][] testsScenatios1 = {{{"1","1"}, {"2","2"} , {"5","5"}, {"10","10"}, {"20","20"}, {"40","40"}}};
		String[] tests1 = {"0","1","3","4","5","6"};
		List<String> ids = graphSet(fileName, queues, "B", testsScenatios1, tests1, "0", false);
		String[][][] testsScenatios2 = {{ {"1","1"}, {"10","20"} , {"10","50"} }, { {"1","1"}, {"20","10"} , {"50","10"} }};
		String[] tests2 = {"0","1","3","4","5","6"};
		ids.addAll(graphSet(fileName, queues, "B", testsScenatios2, tests2, "0", true));
		String[][][] testsScenatios3 = {{{"1","1"}, {"2","2"} , {"5","5"}, {"10","10"}, {"15","15"}, {"20","20"}, {"30","30"} ,{"40","40"}, {"60","60"} ,{"80","80"}}};
		String[] tests3 = {"0","1","2","3","4","5"};
		ids.addAll(graphSet(fileName, queues, "C", testsScenatios3, tests3, "1", false));

		for(String id:ids){
			saveToHTML("	google.setOnLoadCallback(drawChart_"+id+");");
		}
		//endHTML();
		saveToHTML("  </script>");
		saveToHTML("  </head>");
		saveToHTML("  <body>");

		for(String id:ids){
			saveToHTML("<div id=\"test"+id+"\"></div>");
			saveToHTML(" <brbrbrbrbrbrbrbr>");
		}
		saveToHTML("</body>");
		saveToHTML("</html>");
	}


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

		//	System.out.println(buff.toString());

		} catch (IOException e) {
			System.out.println("Error in print result to file");
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
		String fileName = "priorityQueueGraphs_"+timeStamp+".js";
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(fileName, true));
			output.append(line + "\n");
			System.out.println(buff.toString());
			output.flush();
			output.close();

		} catch (IOException e) {
			System.out.println("Error in print data to file");
		}

	}

	public static void saveToHTML(String... result){


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
		String fileName = "index_"+timeStamp+".html";
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(fileName, true));
			output.append(line + "\n");
			System.out.println(buff.toString());
			output.flush();
			output.close();

		} catch (IOException e) {
			System.out.println("Error in print data to file");
		}

	}


	public List<String> graphSet(String fileName, String[] queues, String code, String[][][] testsScenatios, String[] tests, String set, boolean isNumTwo){

		List<RowEntity> rowData = makeDataForChart(fileName);

		int testScenarioCount=0;
		List<String> ids = new ArrayList<String>();
		for(String[][] testScenario: testsScenatios){
			testScenarioCount++;
			Map<String,ChartRow> chartData = new TreeMap<String,ChartRow>( new Comparator<String>() {
				@Override
				public int compare(String arg0, String arg1) {
					String[] str0 = arg0.replaceAll("'", "").replaceAll("\\(", "").replaceAll("\\)", "").split(",");
					String[] str1 = arg1.replaceAll("'", "").replaceAll("\\(", "").replaceAll("\\)", "").split(",");
					for (int i = 0; i< str0.length; i++) {
						int a = Integer.parseInt(str0[i]);
						int b = Integer.parseInt(str1[i]);
						if (a !=b){
							return a-b;
						}
					}
					return 0;
				}
			});
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
					String x_value;
					if(!isNumTwo){
						x_value = rowEntry.getXValue();
					}else{
						x_value = rowEntry.getXValue_2();
					}

					if(flag==1){
						ChartRow currentRow;
						if(chartData.containsKey(x_value)){
							currentRow = chartData.get(x_value);
						}else{
							currentRow = new ChartRow();
						}

						for(String queueType : queues){
							if(rowEntry.getQueueType().equals(queueType))
							{
								currentRow.row.put(queueType, rowEntry);
							}
						}

						chartData.put(x_value, currentRow);
					}
				}
				if(!isNumTwo){
					for(int i=0; i<5;i++){
						String id=code+"_"+test+"_"+i;
						ids.add(id);
						startGraph(queues,id,false);

						for(Entry<String, ChartRow> en: chartData.entrySet()){
							String data[] = new String[queues.length+1];
							data[0] = en.getKey();
							int index = 1;
							if(i==0){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getInsertThroughput();
									index++;
								}
							}
							if(i==1){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getDeleteThroughput();
									index++;
								}
							}
							if(i==2){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getThroughputRatio();
									index++;
								}
							}
							if(i==3){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getAvgGrade();
									index++;
								}
							}
							if(i==4){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getVarianceGrade();
									index++;
								}
							}
							saveData(data);
						}
						endGraph(id,i, false);
					}
				}else{
					for(int i=0; i<4;i++){
						String id=code+"_"+test+"_"+i+"_"+testScenarioCount;
						ids.add(id);
						startGraph(queues,id,true);

						for(Entry<String, ChartRow> en: chartData.entrySet()){
							String data[] = new String[queues.length+1];
							data[0] = en.getKey();
							int index = 1;
							if(i==0){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getInsertThroughput();
									index++;
								}
							}
							if(i==1){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getDeleteThroughput();
									index++;
								}
							}
							if(i==2){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getAvgGrade();
									index++;
								}
							}
							if(i==3){
								ChartRow ja = en.getValue();
								for(String queueType : queues){
									RowEntity c = ja.row.get(queueType);
									Assert.assertNotNull("No queue with name:" +queueType +" in file" , c);
									data[index] = c.getVarianceGrade();
									index++;
								}
							}
							saveData(data);
						}
						endGraph(id,i, true);
					}
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


	private void startGraph(String[] queues, String id,boolean flag) {
		//		saveData("google.setOnLoadCallback(drawChart_"+id+");");
		saveData("function drawChart_"+id+"() {");
		saveData("      var data = new google.visualization.DataTable();");
		if(!flag){
			saveData("      data.addColumn('string', 'Threads');");
		}
		else{
			saveData("      data.addColumn('string', 'Threads(Insert,Delete)');");
		}

		for(String queueName: queues){
			saveData("      data.addColumn('number', '"+queueName+"');");
		}


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

		TreeMap<String, RowEntity> row; 

		public ChartRow(){
			row = new TreeMap<String, TestUtils.RowEntity>();
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

			if(insertTime==0){
				return "1";
			}

			return Long.toString(insertCount/insertTime);	
		}

		public String getDeleteThroughput(){
			long deleteCount = Long.parseLong(this.deleteCount);
			long deleteTime = Long.parseLong(this.deleteTime);


			if(deleteTime==0){
				return "1";
			}

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
