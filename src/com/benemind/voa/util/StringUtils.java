package com.benemind.voa.util;

public class StringUtils {
	final private static char LF = 10;
	final private static char CR = 13;
	
	public static boolean isNull(String s) {
		if ((s != null) && (!s.trim().equals("")))
			return false;
		return true;
	}
	
	/**
     * 把文本编码为Html代码
     * @param target
     * @return 编码后的字符串
     */
    public static String htmEncode(String target)
    {
        StringBuffer stringbuffer = new StringBuffer();
        char prevCh = 0;
        int j = target.length();
        for (int i = 0; i < j; i++)
        {
            char c = target.charAt(i);
            switch (c)
            {
            case 60:
                stringbuffer.append("&lt;");
                break;
            case 62:
                stringbuffer.append("&gt;");
                break;
            case 38:
                stringbuffer.append("&amp;");
                break;
            case 34:
                stringbuffer.append("&quot;");
                break;
            case 169:
                stringbuffer.append("&copy;");
                break;
            case 174:
                stringbuffer.append("&reg;");
                break;
            case 165:
                stringbuffer.append("&yen;");
                break;
            case 8364:
                stringbuffer.append("&euro;");
                break;
            case 8482:
                stringbuffer.append("&#153;");
                break;
            case LF:
            case CR:
            	if( prevCh == LF || prevCh == CR ){
            		if( prevCh == c ){
            			stringbuffer.append("<br/>\n");
            		}
            	}
            	else{
            		stringbuffer.append("<br/>\n");
            	}
                break;
            case 32:
                if (i < j - 1 && target.charAt(i + 1) == ' ')
                {
                    stringbuffer.append(" &nbsp;");
                    i++;
                    break;
                }
            default:
/*            	
            	if( c > 256 ){
            		 stringbuffer.append("&#");
            		 stringbuffer.append(Integer.valueOf(c));
            		 stringbuffer.append(";");
            	}
            	else{
*/            		
            		stringbuffer.append(c);
//            	}
                break;
            }
            prevCh = c;
        }
        return new String(stringbuffer.toString());
    }
}
