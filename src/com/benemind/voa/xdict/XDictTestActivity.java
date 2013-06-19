package com.benemind.voa.xdict;

import java.io.FileOutputStream;
import java.io.InputStream;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import com.benemind.adict.R;
import com.benemind.voa.xdict.core.kingsoft.KingsoftDictHtmlBuilder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class XDictTestActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(SampleList.THEME); //Used for theme switching in samples

		super.onCreate(savedInstanceState);
		setContentView(R.layout.xdict_main);
		test();
		//parseExcel();
	}
	
	private void parseExcel(){  
        try {  
            Workbook workbook = null;  
            InputStream in = null;  
            try {  
//              in = this.getClassLoader().getResourceAsStream("CollMjr.xlsx");  
                in = this.getAssets().open("iciba.xls");  
                workbook = Workbook.getWorkbook(in); 
                workbook.getNumberOfSheets();
            } catch (Exception e) {  
                e.printStackTrace();  
                throw new Exception("file not found!");  
            }  
            Sheet sheet = workbook.getSheet(0);  
            Cell cell = null;  
            int columnCount = sheet.getColumns();  
            int rowCount = sheet.getRows();  
            System.out.println("当前工作表的名字:" + sheet.getName());    
            System.out.println("总行数:" + rowCount);    
            System.out.println("总列数:" + columnCount);
            for(int i=0; i<5; i++){
            	for(int j=0; j<3; j++ ){
            		cell = sheet.getCell(j, i); //注意，这里的两个参数，第一个是表示列的，第二才表示行
            		StringBuilder b = new StringBuilder();
            		b.append((char)('A'+i));
            		b.append(1+j);
            		b.append(":");
            		b.append(cell.getContents());
            		Log.v("excel", b.toString());
            	}
            }
/*            
            list = new ArrayList<String>();  
            for (int i = 0; i < rowCount; i++) {  
                for (int j = 0; j < columnCount; j++) {  
                    // 注意，这里的两个参数，第一个是表示列的，第二才表示行  
                    cell = sheet.getCell(j, i);   
                    String s = sheet.getCell(columnCount-1, i).getContents();   
                }  
            }
*/            
            workbook.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  

	private void test() {
		String xml = loadXml("iciba.xml");
		//KingsoftDictHtmlBuilder.loadConvertParam(this);
		//String html = KingsoftDictHtmlBuilder.buildDictSearchContent(xml);
		//writeFileSdcardFile(html);
	}
	
	private void writeFileSdcardFile(String write_str) {
		try {
	
			FileOutputStream fout = new FileOutputStream("/mnt/sdcard/new.html");
			byte[] bytes = write_str.getBytes();
	
			fout.write(bytes);
			fout.close();
		}
	
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	private String loadXml(String xmlName) {
		String res = "";
		try {

			// 得到资源中的asset数据流
			InputStream in = getResources().getAssets().open(xmlName);

			int length = in.available();
			byte[] buffer = new byte[length];

			in.read(buffer);
			in.close();
			// res = EncodingUtils.getString(buffer, "UTF-8");
			res = new String(buffer);

		} catch (Exception e) {

			e.printStackTrace();

		}
		return res;
	}
}
