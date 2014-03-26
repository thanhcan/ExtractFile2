import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vn.hus.nlp.tagger.TaggerOptions;
import vn.hus.nlp.tagger.VietnameseMaxentTagger;
import vn.hus.nlp.tokenizer.TokenizerOptions;
import vn.hus.nlp.tokenizer.VietTokenizer;
import vn.hus.nlp.utils.UTF8FileUtility;

public class ExtractFile {
	
	private static String content_file;
	
	private ExtractFile() {
		
		this.content_file = null;
	}
	
	/**
	 * The starting point of the programme.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		/**
		 * Lay du lieu trong the content
		 */
		ReadFile readFile = new ReadFile();
		content_file = readFile.readWrite("baomoi.tsv");			//Đoc nội dung thẻ content trong file đầu vào
		//content_file = "1.txt";
		
		/**
		 * Đoc từng dòng file dữ liệu, với mỗi dòng đồng thời đánh chỉ mục phân loại từ và số lần xuất hiện từ
		 *    , kiểm tra xem các danh từ có trong 30 từ xuất hiện nhiều nhất không
		 */
		VietTokenizer vietTokenizer = new VietTokenizer();				//Biến tách từu
		VietnameseMaxentTagger tagger = new VietnameseMaxentTagger();   //Biến đánh chỉ mục phân lọai từ
		StatisticWords statistic = new StatisticWords();				//Biến dùng để thống kê từ và ghi ouput
		
		TokenizerOptions.USE_SENTENCE_DETECTOR = true;					//Đặt chế độ tách từ '-sd'
		TaggerOptions.UNDERSCORE = true;								//Đặt chế độ đánh chỉ mục từ '-u'
		
		Map<String, Integer> map = new HashMap<String, Integer>();		//Map với  key: từ và value: lần xuất hiện
		
		long startTime = System.currentTimeMillis();
		String[] paragraphs = UTF8FileUtility.getLines(content_file);
		String noun = null;
		int lines = 0;
		
		System.out.println("Starting ...");
		for (String p : paragraphs) 
		{
			String[] sentences = vietTokenizer.tokenize(p);			//Tách từ ở mỗi dòng
			for (String s : sentences) 
			{
				Pattern pattern;
				Matcher matcher;
				
				//Bỏ các ký tự, chuỗi không cần thiết
				s = s.replaceAll("\\\\ ", "\\\\"); 		s = s.replaceAll("\\\\u003E", " "); 
				s = s.replaceAll("\\\\[^ ]* ", "");		s = s.replace('_', ' ');
				s = s.replaceAll(" [ \\t]*", " ");		s = s.replaceAll("\\\\n", "");
				
				//Kiểm tra có phải xâu rỗng không
				if (s.length() == 0)
					continue;
				//Kiểm tra xâu có từ bất kỳ nào không
				pattern = Pattern.compile("[a-zA-Z]");
				matcher = pattern.matcher(s);				
				if (matcher.find() == false)
					continue;
				
				//System.out.println(lines + "_ " + s);
				s = tagger.tagText(s);			//Đánh chỉ mục các từ ở mỗi dòng
				if (s == null) 
				{
					System.out.println("Gặp lỗi ở dòng: " + lines);
					System.exit(1);
				}
				
				//Tìm các danh từ trong mỗi dòng
				pattern = Pattern.compile("[^ ]*/N ");
				matcher = pattern.matcher(s);
				while (matcher.find())
				{
					noun = matcher.group().toLowerCase();
					noun = noun.replace('_', ' '); noun = noun.replaceAll("/n", "");
					
					//Đưa vào map lưu trữ
					if (map.containsKey(noun))
						map.put(noun, map.get(noun) + 1);
					else map.put(noun, 1);
					
					//Kiểm tra có phải trong top 30 từ xuất hiện nhiều nhất không
					statistic.put(noun, map.get(noun));
				}
				
				lines++;
			}
		}
		long endTime = System.currentTimeMillis();
		float duration = (float) (endTime - startTime) / 1000;
		System.out.println("Tokenized " + " in " + duration + " (s).\n" + "Done!");
		
		statistic.Collocation();		//Sắp xếp mảng 30 từ xuất hiện nhiều nhất giảm dần
		statistic.WriteOutput();		//Ghi ra file output
	}
	
}
