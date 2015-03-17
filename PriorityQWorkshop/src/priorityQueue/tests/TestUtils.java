package priorityQueue.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;



public class TestUtils {


	private static String timeStamp = null;


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

			output.flush();
			output.close();

		} catch (IOException e) {
			System.out.println("Error in print result to file");
			e.printStackTrace();
		}

	}

}
