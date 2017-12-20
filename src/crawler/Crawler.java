package crawler;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.WebClient;  
import com.gargoylesoftware.htmlunit.html.HtmlForm;  
import com.gargoylesoftware.htmlunit.html.HtmlPage;  
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;  
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;  
 

public class Crawler{
	private String basicUrl = "http://222.195.242.224:8080";
	private String login = "http://222.195.242.224:8080/loginAction.do";
	private String vchartUrl = basicUrl + "/validateCodeAction.do?random=";//这里需要加上random使用
	//用户名和密码
	private String userName = "";
	private String passwd = "";
	private Set<Cookie> cookie;
	private WebClient wb = new WebClient();
	private String headShotUrl = "http://222.195.242.224:8080/xjInfoAction.do?oper=img";
	private String privateInfo = "http://222.195.242.224:8080/xjInfoAction.do?oper=xjxx";
	
	private int count = 0;
	public void setUser(String userName, String userPasswd) {
		this.userName = userName;
		this.passwd = userPasswd;
	}
	
	
	public int getPage() {
		//LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog"); 
		
		wb.getOptions().setThrowExceptionOnScriptError(false);
		wb.getOptions().setThrowExceptionOnFailingStatusCode(false);
		wb.getOptions().setCssEnabled(false);
		wb.getCookieManager().setCookiesEnabled(true);
		try{
			HtmlPage page = wb.getPage(basicUrl);
			cookie = wb.getCookieManager().getCookies();
			//调用完getPage()后再获取cookie就能得到了
			String vchartSrc = page.getElementById("vchart").getAttribute("src");
			System.out.println(getRandom(vchartSrc)); 
			//获取验证码
			String vchart = getVchart(vchartSrc);
			
			//填充表单
			fileForm(page, vchart);
			
			HtmlImageInput btn = (HtmlImageInput) page.getElementById("btnSure");
			HtmlPage newPage = (HtmlPage) btn.click();
			wb.waitForBackgroundJavaScript(10000);
			
			//看看新页面是否登入成功
			if("URP 综合教务系统 - 登录".equals(newPage.getTitleText())) {
				//查看一下有无错误信息  如果有的话 则if不成立
				if(!newPage.querySelector(".errorTop").asText().equals("")) {
					//300账号密码不正确  彻底失败
					System.out.println();
					return 300;
				}
				//210验证码不正确，重新识别
				return 210;
			}
			//其他情况认为登录成功（没有考虑 ’数据库忙‘ 的页面）
			return 200;
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("出现异常");
	//	getch();
		return 310;
		
	}
	
	
	private String getRandom(String src) {
		//由观察得验证码从第三十位（含）开始~
		return src.substring(30);
	}
	
	
	
	
	public void getHeadShot() {
		int tmp;
		//200登录成功 跳出循环
		while((tmp = getPage()) != 200) {
			if(tmp == 300 || tmp == 310) {
				System.out.println("账号密码错误");
				return;
			}
			
		}
		
		try {
			WebRequest get = new WebRequest(new URL(headShotUrl));
			get.setHttpMethod(HttpMethod.GET);
			Page page = wb.getPage(get);
			WebResponse res = page.getWebResponse();
			InputStream is = res.getContentAsStream();
			String name = null;
			while(name == null) {
				name = getName();
			}
			saveImg(is, name);
		//	Thread.sleep(500);
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok");
		
	}
	
	
	private String getName() {
		int i = 0;
		while(i ++ < 10) {
			try {
				WebRequest get = new WebRequest(new URL(privateInfo));
				get.setHttpMethod(HttpMethod.GET);
				HtmlPage page = wb.getPage(get);
				
				DomElement tvlView = page.getElementById("tblView");
				String name = null;
				if(tvlView != null) {
					name = tvlView.getElementsByTagName("td").get(3).asText();
				}else {
					continue;
				}
				System.out.println(name + ".jpg");
				return name + ".jpg";
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		return userName;
	}
	
	
	private String getVchart(String random) {
		WebClient wc = new WebClient();
		wc.getOptions().setThrowExceptionOnScriptError(false);
		wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
		wc.getOptions().setCssEnabled(false);
		wc.getCookieManager().setCookiesEnabled(true);
		Iterator<Cookie> i = cookie.iterator();
		while(i.hasNext()) {
			wc.getCookieManager().addCookie(i.next());
		}
		try {
			WebRequest get = new WebRequest(new URL(vchartUrl + random));
			get.setHttpMethod(HttpMethod.GET);
			//这个是带着cookie获取的单独的验证码页面
			Page page = wc.getPage(get);
			WebResponse res = page.getWebResponse();
			//通过res创建输入流
			InputStream is = res.getContentAsStream();
			//通过输入流写入文件并保存
			File img = saveImg(is, "vchart.jpg");
			//调用图形识别接口
			Recognition rec = new Recognition();
			String vchart = rec.execute(img);
			
			System.out.println("验证码识别结果：" + vchart);
			wc.close();
			return vchart;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			wc.close();
			return "";
		}
		
	}
	
	
	
	private File saveImg(InputStream is, String fileName) {
		File f = new File("C:\\Users\\fan\\Desktop\\temp\\head\\" + fileName);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int temp;
		try {
			while((temp = is.read(buffer)) != -1) {
				bos.write(buffer, 0, temp);
			}
			byte[] data = bos.toByteArray();
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(data);
			fos.close();
			return f;
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}
	
	private void fileForm(HtmlPage page, String vchart) {

		HtmlForm form = page.getForms().get(0);
		HtmlTextInput zjh = form.getInputByName("zjh");
		zjh.setValueAttribute(userName);
		HtmlPasswordInput mm = form.getInputByName("mm");
		mm.setValueAttribute(passwd);
		//这里要注意，传进来的验证码可能包含隐藏的空格！实际长度大于4
		if(vchart.length() > 4) {
			vchart = vchart.substring(0, 4);
		}else {
			return;
		}
		HtmlTextInput yzm = form.getInputByName("v_yzm");
		yzm.setValueAttribute(vchart);
		
	}
	
	
	private void getch() {
		Scanner scan = new Scanner(System.in);
		scan.nextLine();
	}
	
}
