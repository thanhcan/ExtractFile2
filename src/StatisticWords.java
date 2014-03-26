import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.Normalizer;
import java.text.SimpleDateFormat;


public class StatisticWords {
	
	private static String[] words = new String[31];
	private static long[] statistic = new long[31];
	
	//Tất cả các ký tự có dấu trong tiếng Việt
	static String SPECIAL_CHARACTERS = "àÀảẢãÃáÁạẠăĂằẰẳẲẵẴắẮặẶâÂầẦẩẨẫẪấẤậẬđĐèÈẻẺẽẼéÉẹẸêÊềỀểỂễỄếẾệỆìÌỉỈĩĨíÍịỊòÒỏỎõÕóÓọỌôÔồỒổỔỗỖốỐộỘơƠờỜởỞỡỠớỚợỢùÙủỦũŨúÚụỤưƯừỪửỬữỮứỨựỰỳỲỷỶỹỸýÝỵỴ";
	//Các ký tự bỏ dấu tương ứng
	static String REPLACEMENTS = "aAaAaAaAaAaAaAaAaAaAaAaAaAaAaAaAaAdDeEeEeEeEeEeEeEeEeEeEeEiIiIiIiIiIoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOoOuUuUuUuUuUuUuUuUuUuUuUyYyYyYyYyY";

	public StatisticWords() {
		
		int i = 0;
		for (i = 0; i < 30; i++)
		{
			words[i] = null;
			statistic[i] = 0;
		}	
	}
	
	public void put(String word, int appear) {
		int index = 0, i = 0;
		
		for (i = 0; i < 30 ; i++)
			if (words[i] != null && words[i].compareTo(word) == 0)
			{
				statistic[i] = appear;
				return;
			}
		
		index = MinAppear();
		if (appear > statistic[index])
		{
			statistic[index] = appear;
			words[index] = word;
		}
	}
	
	/**
	 * Tìm vị trí từ xuất hiện nhiều thứ 30 trong dữ liệu
	 */
	private static int MinAppear()
	{
		int i, index;
		long min;
		
		min = statistic[0]; index = 0;
		for (i = 1; i < 30; i++)
			if (statistic[i] < min)
			{
				min = statistic[i];
				index = i;
			}
		
		return index;
	}
	
	/**
	 * Kiểm tra xem có phải ký tự có dấu không và trả về ký tự không dấu
	 * @param x: ký tự cần kiểm tra
	 * @return: nếu là ký tự có dấu trả về ký tự đã bỏ dấu, nếu không trả về '$'
	 */
	private static char Check(char x)
	{
		int i = 0;
		char p, q;
		for (i = 0; i < REPLACEMENTS.length(); i++)
		{	
			p = SPECIAL_CHARACTERS.charAt(i);
			q = REPLACEMENTS.charAt(i);
			if (p == x)
				return q;
		}
		return '$';	
	}
	
	/**
	 * Sắp xếp 30 từ xuất hiện nhiều nhất từ nhỏ đến lớn theo số lần xuất hiện
	 */
	public void Collocation() {
		int i, j;
		long swap;
		String swapStr;
		
		for (i = 0; i < 29; i++)
			for (j = i+1; j < 30; j++)
				if (statistic[i] < statistic[j])
				{
					swap = statistic[i]; statistic[i] = statistic[j]; statistic[j] = swap;
					swapStr = words[i]; words[i] = words[j]; words[j] = swapStr;
				}
	}
	
	public void WriteOutput() throws IOException {
		
		String noun, outFile;
		int i, j;
		char replace;
		
		//Ten file output
		Date today = new Date(System.currentTimeMillis());
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
		String todayS = dayFormat.format(today.getTime());
		outFile = "ThanhBTT_" + todayS + "_NOUN.tsv"; 
		
		File file = new File(outFile);
		FileOutputStream fileIn = new FileOutputStream(file);
		OutputStreamWriter inputStream = new OutputStreamWriter(fileIn);
		BufferedWriter bufferWrite = new BufferedWriter(inputStream);
		
		for (i = 0; i < 30; i++)
		{
			//noun = Normalizer.normalize(words[i],Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
			
			noun = words[i];
			for (j = 0; j < words[i].length(); j++)
			{
				replace = Check(noun.charAt(j));
				if (replace != '$')
					noun = noun.replace(noun.charAt(j), replace);
			}
			bufferWrite.write(words[i] + "\t" + noun + "\t" + statistic[i] + "\n");	
		}
		
		bufferWrite.close();
	}
}
