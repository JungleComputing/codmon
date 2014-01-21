import java.io.File;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.Properties;
import java.util.ArrayList;

// Jrobin stuff
import org.jrobin.core.*;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import java.awt.*;

// Mail stuff
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;


class Config {
	static {
    		loadProperties();
	}
	
	static  String CODMON_HOME;
	static  String CODMON_CHECKOUT;
	static  String BASE;
    	static  String RRDBASE;
    	static  final int RRDSTEP = 86400;
    	static  final String PROJECT = "Ibis";
    	static  final int MAXRUNS = 15;


	private static void loadProperties(){
		Properties prop = new Properties();
		try {
               //load a properties file
               prop.load(new FileInputStream("environment.properties"));

//		prop.load(new FileInputStream("/home/bvl300/codmon/codmon/local/environment.properties"));
		CODMON_HOME = prop.getProperty("codmon.home"); 
                CODMON_CHECKOUT = prop.getProperty("codmon.checkOut");
		BASE = CODMON_HOME + "/codmon/";
		RRDBASE = BASE+"rrd/";
    		} catch (IOException ex) {
    			ex.printStackTrace();
        	}
	}
}

class FatalSensorException extends Exception {}


class BufferedResults {
    class Result {
	public String id, file, name, scope, graph, type, fatal;
	public Result next;
	public Process process;
	public Result(String id, String name, String scope, String file, Process process, String graph, String type, String fatal) {
	    this.id = id;
	    this.name = name;
	    this.scope = scope;
	    this.file = file;
	    this.next = null;
	    this.process = process;
	    this.graph = graph;
	    this.type = type;
	    this.fatal = fatal;
	}
    }

    Result head;
    Result current;
    public BufferedResults() {
	current = null;
	head = null;
    }

    public void append(String id, String name, String scope, String file, Process process, String graph, String type, String fatal) {
	Result newRes = new Result(id, name, scope, file, process, graph, type, fatal);
	if (head == null) { 
	    head = newRes; 
	    current = newRes;
	} else {
	    current.next = newRes;
	    current = newRes;
	}
    }

    public boolean isEmpty() {
	return (current == null);
    }

    public Result get() {
	return current;
    }

    public void next() {
	current = current.next;
    }

    public void init() {
	current = head;
    }
}



class Shot {
    
    public StringBuffer str;
    public Runtime runtime;
    public File config;

    private static int running = 0;
    private static int total;

    public static String parseISToString(java.io.InputStream is){
	BufferedReader din
          = new BufferedReader(new InputStreamReader(is));
	//java.io.DataInputStream din = new java.io.DataInputStream(is);
	StringBuffer sb = new StringBuffer();
	try{
	    String line = null;
	    while((line=din.readLine()) != null){
		sb.append(line+"\n");
	    }
	}catch(Exception ex){
	    ex.getMessage();
	}finally{
	    try{
		is.close();
	    }catch(Exception ex){}
	}
	return sb.toString();
    }

    private static String xmlSafe(String str) {
	//System.err.println("str = "+str.trim());
	if (str.trim().startsWith("<html>") && str.trim().endsWith("</html>")) {
	    //System.err.println("Match html");
	    str = str.trim().substring(6, str.length()-8);
	}
	else
	    for (int i = 0; i < str.length(); i++) {
		if (str.charAt(i) == '<' && !str.substring(i, i+5).equals("<br/>"))
		    str = str.substring(0,i)+"&lt;"+str.substring(i+1);
		else if (str.charAt(i) == '>' && !str.substring(i-4, i+1).equals("<br/>"))
		    str = str.substring(0,i)+"&gt;"+str.substring(i+1);
		else if (str.charAt(i) == '&')
		    str = str.substring(0,i)+"&#38;"+str.substring(i+1);
	    }

	return str;
    }
    

    private static String lookup_wrapper(String name) {
	if (name.equals("time"))
	    return ("perl "+Config.BASE+"wrappers/time_wrapper.pl ");
	else if (name.equals("outcmp"))
	    return ("perl "+Config.BASE+"wrappers/outcmp_wrapper.pl ");
	else if (name.equals("outcmp_ibis"))
	    return ("perl "+Config.BASE+"wrappers/outcmp_ibis_wrapper.pl ");
	else if (name.equals("outcmp_gmi"))
	    return ("perl "+Config.BASE+"wrappers/outcmp_gmi_wrapper.pl ");
	else if (name.equals("outcmp_rmi"))
	    return ("perl "+Config.BASE+"wrappers/outcmp_rmi_wrapper.pl ");
	else if (name.equals("outcmp_mpj"))
	    return ("perl "+Config.BASE+"wrappers/outcmp_mpj_wrapper.pl ");
	else if (name.equals("outcmp_satin"))
	    return ("perl "+Config.BASE+"wrappers/outcmp_satin_wrapper.pl ");
	else
	    return "";
    }

    private String getAttribute(Element element, String id) {
        String s = element.getAttribute(id);
        return s.replaceAll("CODMON_HOME", Config.CODMON_HOME);
    }

    public Shot(String configg) {
	str = new StringBuffer();
	runtime = Runtime.getRuntime();
	config = new File(configg);

	// Add XML Header to stringBuffer
	add_header();

	// Stat work
	try {
	    DocumentBuilder builder =
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc = builder.parse(config);
	 

	    // Running onoff sensors
	    NodeList nodes = ((Element) doc.getElementsByTagName("onoff").item(0)).getElementsByTagName("sensor");
	    total = nodes.getLength();
	    str.append("<onoff>\n");
	    BufferedResults results = new BufferedResults();
	    
	    for (int i = 0; i < nodes.getLength(); i++) {
		Element element = (Element) nodes.item(i);


		String id = element.getAttribute("id");
		String name = element.getAttribute("name");
	
		if (id.equals("separator")) {
		    results.append(id, name, null, null, null, null, null, null);
		    continue;
		}
		else if (id.equals("part")) {
		    results.append(id, name, null, null, null, null, null, null);
		    continue;
		}


		String wrapper = getAttribute(element, "wrapper");
		String cmd = getAttribute(element, "cmd");
		String scope = element.getAttribute("scope");
		String graph = element.getAttribute("graph");

		String enabled = element.getAttribute("enabled");
		if (enabled.equals("false")) {
		    //str.append("<sensor id=\""+id+"\" name=\""+name+"\" complete=\"true\" returnValue=\"0\" scope=\""+scope+"\" graph=\""+graph+"\" enabled=\"false\" />\n");
		    results.append("disabled", name, null, null, null, null, null, null);
		    continue;
		}

		String fatal = element.getAttribute("fatal");
		if ((++running)%Config.MAXRUNS == 0) {
		    finalize_onoff(results);
		    results = new BufferedResults();
		}
	
	
		if (element.getAttribute("scheduled").equals("true"))
		    init_onoff(id, name, wrapper, cmd, scope, graph, fatal, results);
		else {  /* Unscheduled : finish running jobs, run *this* job, reinit results */
		    finalize_onoff(results);

		    results = new BufferedResults();
		    init_onoff(id, name, wrapper, cmd, scope, graph, fatal, results);
		    finalize_onoff(results);

		    results = new BufferedResults();
		}
		    //run_onoff(id, name, cmd, scope);

		
	    }
	    finalize_onoff(results);
	    str.append("</onoff>\n");

	    // Running graph sensors
	    nodes = ((Element) doc.getElementsByTagName("graph").item(0)).getElementsByTagName("sensor");
	    total = nodes.getLength();
	    str.append("<graph>\n");

	    running=0;
	    results = new BufferedResults();
	    for (int i = 0; i < nodes.getLength(); i++) {
		Element element = (Element) nodes.item(i);


		String id = element.getAttribute("id");
		String name = element.getAttribute("name");
		if (id.equals("separator")) {
		    results.append(id, name, null, null, null, null, null, null);
		    continue;
		}
		else if (id.equals("part")) {
		    results.append(id, name, null, null, null, null, null, null);
		    continue;
		}


		String wrapper = getAttribute(element, "wrapper");
		String cmd = getAttribute(element, "cmd");
		String scope = element.getAttribute("scope");
		String type = element.getAttribute("type");

		String enabled = element.getAttribute("enabled");
		if (enabled.equals("false")) {
		    results.append("disabled", name, null, null, null, null, null, null);
		    //str.append("<sensor id=\""+id+"\" name=\""+name+"\" complete=\"true\" returnValue=\"0\" scope=\""+scope+"\" enabled=\"false\" />\n");
		    continue;
		}

		String fatal = element.getAttribute("fatal");
		if ((++running)%Config.MAXRUNS == 0) {
		    finalize_graphs(results);
		    results = new BufferedResults();
		}

		if (element.getAttribute("scheduled").equals("true"))
		    init_graph(id, name, wrapper, cmd, scope, type, fatal, results);
		else {  /* Unscheduled : finish running jobs, run *this* job, reinit results */
		    finalize_graphs(results);

		    results = new BufferedResults();
		    init_graph(id, name, wrapper, cmd, scope, type, fatal, results);
		    finalize_graphs(results);

		    results = new BufferedResults();
		}
		    
	    }
	    finalize_graphs(results);
	    str.append("</graph>\n");


	}
	catch (Exception e) {
	    e.printStackTrace();
	}      
 
	// Add XML Footer
	add_footer();
	//used for debugging when calling a programm
	System.out.println(str);
    }

	
    private void add_header() {
	java.util.Date date = new java.util.Date();
	str.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<?xml-stylesheet type=\"text/xsl\" href=\"../libs/style.xml\"?>\n");
	str.append("<sensors date=\""+date+"\" project=\""+Config.PROJECT+"\" config=\""+config.getName()+"\">\n");
    }

    private void add_footer() {
	str.append("</sensors>\n");
    }


    private String getDirectory(String path){
	return null;	
    }



    /**
     *@author bvl300
     *Starts a new process in a given directory
     *TODO commando uitbreidem met te testen software
     **/
    private Process startProcess(String cmd) throws IOException {
	//Need to specify which Arraylist util vs awt 	
	java.util.List<String> argList= new ArrayList<String>();
        String[] arguments = cmd.split("\\s+");
        String call = arguments[0];
        int i = arguments[1].lastIndexOf('/')+1;
        String wrapper = arguments[1].substring(i);
        String dir = arguments[1].substring(0,i);
	argList.add(call);
        argList.add(wrapper);
        argList.add(arguments[2]);
        argList.add(arguments[3]);
	//The 4th argument is used for a non-default ant target
        if(arguments.length==5){argList.add(arguments[4]);}
	final Process pr = new ProcessBuilder(argList)
 		 .directory(new File(dir)) 
    		 .start();
	return pr;
    }

    private void init_onoff(String id, String name, String wrapper, String cmd, String scope, String graph, String fatal, BufferedResults results) {
	try {
	    System.err.println(running+"/"+total+"-Checking: "+name+"..."+((id.equals("separator")||id.equals("part"))?"(Separator)":""));
	    // long start = Util.getTime();
	    Process compil;	    
	    //compil = runtime.exec(lookup_wrapper(wrapper)+cmd);
            compil = startProcess(lookup_wrapper(wrapper)+cmd);
	    results.append(id, name, scope, null, compil, graph, null, fatal);

	} catch (Exception e) {
	    System.err.println("Failed in RunOnOff");
	}

    }

    private void finalize_onoff(BufferedResults results) {
	results.init();
	while (results.isEmpty() == false) {
	    try {
		System.err.println(running+"/"+total+"-Waiting: "+results.get().name+"..."+(results.get().id.equals("separator")?"(Separator)":""));
		
		BufferedResults.Result res = results.get();
		
		if (results.get().id.equals("separator")) {
		    str.append("<sensor id=\"separator\" name=\""+results.get().name+"\"/>\n");
		    results.next();
		    continue;
		}
		else if (results.get().id.equals("part")) {
		    str.append("<sensor id=\"part\" name=\""+results.get().name+"\"/>\n");
		    results.next();
		    continue;
		}
		else if (results.get().id.equals("disabled")) {
		    str.append("<sensor id=\"disabled\" name=\""+results.get().name+"\" enabled=\"false\" graph=\"false\" returnValue=\"0\" />\n");
		    results.next();
		    continue;
		}
		
		
		Process compil = res.process;
		String id = res.id;
		String name = res.name;
		String scope = res.scope;
		String graph = res.graph;
		String fatal = res.fatal;

		InputStream errors = compil.getErrorStream();
		String errorstr = parseISToString(errors);
		
		InputStream out = compil.getInputStream();
		String outstr = parseISToString(out);

		int value = compil.waitFor();
		// long end = Util.getTime();
		
		if (value==0) {
		    str.append("<sensor id=\""+id+"\" name=\""+name+"\" complete=\"true\" returnValue=\"0\" scope=\""+scope+"\" graph=\""+graph+"\" fatal=\""+fatal+"\">\n");
		    str.append(outstr);
		} else {
		    str.append("<sensor id=\""+id+"\" name=\""+name+"\" complete=\"false\" returnValue=\""+value+"\" scope=\""+scope+"\" graph=\""+graph+"\" fatal=\""+fatal+"\">\n");
		    str.append(outstr);
		    // Checking diffs
		    // String[] env = {"CVS_RSH=ssh"};
		    // Process cvsParser = runtime.exec(Config.BASE+"libs/cvsParser.pl "+scope, env);
		    // InputStream cvs = cvsParser.getInputStream();
		    // String cvsstr = parseISToString(cvs);
		    // str.append(cvsstr);
		    str.append(xmlSafe(errorstr));
		    
		}
                str.append("</sensor>\n");
		
	    } 
	    catch (Exception e) {
		System.err.println("Failed in RunOnOff");
	    }

	    results.next();
	}
    }
    


    private void init_graph(String id, String name, String wrapper, String cmd, String scope, String type, String fatal, BufferedResults results) {
	try {

	    System.err.println(running+"/"+total+"-Running: "+name+"..."+((id.equals("separator")||id.equals("part"))?"(Separator)":""));

	    java.io.File tmpDir = new File(Config.BASE+"tmp"); 

	    java.io.File file = File.createTempFile("codmon",".tmp", tmpDir); //)new java.io.File(Config.BASE+"apps/tmp");
	    String fileName = file.getAbsolutePath();
	    file.deleteOnExit();
	    // System.err.println("Cmd is "+cmd+" -out "+fileName);


	    // System.err.println(lookup_wrapper(wrapper)+cmd+" -out "+fileName);
	    Process compil = runtime.exec(lookup_wrapper(wrapper)+cmd+" -out "+fileName);

	    results.append(id, name, scope, fileName, compil, null, type, fatal);
	} catch (Exception e) {
	    System.err.println("Failed in Graph_init");
	}
    }

    private void finalize_graphs(BufferedResults results) {
	results.init();
	while (results.isEmpty() == false) {
	    try {
		System.err.println(running+"/"+total+"-Waiting: "+results.get().name + "..."+(results.get().id.equals("separator")?"(Separator)":""));

		BufferedResults.Result res = results.get();

		if (results.get().id.equals("separator")) {
		    str.append("<sensor id=\"separator\" name=\""+results.get().name+"\"/>\n");
		    results.next();
		    continue;
		}
		else if (results.get().id.equals("part")) {
		    str.append("<sensor id=\"part\" name=\""+results.get().name+"\"/>\n");
		    results.next();
		    continue;
		}
		else if (results.get().id.equals("disabled")) {
		    str.append("<sensor id=\"disabled\" name=\""+results.get().name+"\" enabled=\"false\" graph=\"false\" returnValue=\"0\" />\n");
		    results.next();
		    continue;
		}
		
	        InputStream errors = res.process.getErrorStream();
	        String errorstr = parseISToString(errors);

	        InputStream outputs = res.process.getInputStream();
	        String outputstr = parseISToString(outputs);
		
		int value = res.process.waitFor();
		
		/* InputStream errors = compil.getErrorStream();
		   System.out.println(parseISToString(errors)); */
		
		str.append("<sensor id=\""+res.id+"\" name=\""+res.name+"\" scope=\""+res.scope+"\" returnValue=\""+value+"\" type=\""+res.type+"\" fatal=\""+res.fatal+"\">\n");
		
		char[] cbuf = new char[500];
		int i;
		
		java.io.FileReader in = new java.io.FileReader(res.file);
		while ((i = in.read(cbuf)) != -1) {
		    str.append(new String(cbuf,0,i));
		}
		in.close();

		if (value != 0) {
		    // Checking diffs
		    // String[] env = {"CVS_RSH=ssh"};
		    // Process cvsParser = runtime.exec(Config.BASE+"libs/cvsParser.pl "+res.scope, env);
		    // InputStream cvs = cvsParser.getInputStream();
		    // String cvsstr = parseISToString(cvs);
		    // str.append(cvsstr);
		    str.append("STDOUT:\n");
		    str.append(xmlSafe(outputstr));
		    str.append("STDERR:\n");
		    str.append(xmlSafe(errorstr));
		}
	      
		str.append("</sensor>\n");
		
		//java.io.File file = File.createTempFile(fileName);
		//file.close();
		
		/*  if (value==0) {
		    return true;
		    } else {
		    return false;
		    }
		*/
	    } 
	    catch (Exception e) {
		str.append("</sensor>");
		System.err.println("Failed in Graph");
	    }
	    
	    results.next();
	    // return false;
	}
	
    }
}


class ColorChooser {
    private float r,g,b,step;
    //private Color [] colors = {Color.green, Color.magenta, Color.yellow};

    public ColorChooser(int nbcolors) {
	r = (float) 0.0;
	g = (float) 0.5;
	b = (float) 1.0;
	step = (float) 1.0 / nbcolors;
	//colors = {Color.green, Color.magenta, Color.yellow};
    }

    public Color getColor() {
	Color color = new Color(r,g,b);

	r += step;
	g = (g+step)%1;
	b -= step;

	return color;
	/*	Color color = colors[index];
	index = (index+1)%colors.length;
	return color; */
    }
}


class Alarm  {
    //    private RrdDb rrdDb;
    // private String[] dsNames;
    //private long step;
    private boolean active;
    private String id;
    private String name;
    private Element element;
    private String type;
    private String scope;
    private String shotXml;

    public Alarm(Element element, String type, String shotXml) {
	active = false;
	this.element = element;
	id = element.getAttribute("id");
	name = element.getAttribute("name");
	scope = element.getAttribute("scope");
	this.shotXml = shotXml;
	try {
	    if (type.equals("graph")) {
		this.type = "graph";

		if (!element.getAttribute("returnValue").equals("0"))
		    active = true;
		else {
		    RrdDb rrdDb = new RrdDb(Config.RRDBASE+id+".rrd");
		    String[] dsNames = rrdDb.getDsNames();
		    long step = rrdDb.getRrdDef().getStep();
		    this.id = id;
		    
		    FetchRequest request = rrdDb.createFetchRequest("AVERAGE", Util.normalize(Util.getTime()-step,step), Util.normalize(Util.getTime(),step), step);
		    FetchData fetchData = request.fetchData();
		    for (int i = 0; i < dsNames.length; i++) {
			double[] values = fetchData.getValues(dsNames[i]);
			double ratio = 1.33;
			if (element.getAttribute("type").equals("bandwidth")) ratio = 1.33;
			else if (element.getAttribute("type").equals("latency")) ratio = 0.75;
			// Activate the alarm if oldvalue/newvalue > 1.33
			if (values.length == 2 && (values[0] / values[1] > 1.33) )
			    active = true;
		    }
		}

	    } else if (type.equals("onoff")) {
		this.type = "onoff";
		if (!element.getAttribute("returnValue").equals("0"))
		    active = true;
	    }
	    } catch (Exception e) {	    System.err.println("Failed in Alarm");
	    }
	}

    /*
    public void trigger() {
	try {
	    if (active) {
		java.io.FileWriter out = new java.io.FileWriter("alarms", true);
		out.write("Alarm at "+Util.getTime()+" in "+id+"\n");
		out.close();
	    }
	} catch (Exception e) {
	    System.err.println("Failed in Alarm trigger");
	}

    }
    */

    private String flattenNode(Element element) {
	StringBuffer cont = new StringBuffer();
	NodeList nodes = element.getChildNodes();
	for (int i=0; i<nodes.getLength(); i++) {
	    if (nodes.item(i).getNodeType() == 3 && !(nodes.item(i).getNodeValue().equals("\n"))) {
		cont.append(nodes.item(i).getNodeValue());
	    }
	    else if (nodes.item(i).getNodeType() == 1 && !(((Element) nodes.item(i)).getTagName().equals("modif"))) {
		Element current = (Element) nodes.item(i);
		NamedNodeMap attrs = current.getAttributes();
		cont.append("<"+current.getTagName()+" ");
		for (int j = 0; j < attrs.getLength(); j++) {
		    Attr attr = (Attr) attrs.item(j);
		    cont.append(attr.getNodeName()+"=\""+attr.getNodeValue()+"\" ");
		}
		if (current.hasChildNodes()) {
		    cont.append(">\n");
		    cont.append(flattenNode(current));
		    cont.append("</"+current.getTagName()+">\n");
		}
		else
		    cont.append("/>\n");
	    }
	    
	}
	return cont.toString();
    }	

    public void trigger() throws FatalSensorException {
	try {
	    if (active) {
		// Retrieve the last mods
		String[] env = {"CVS_RSH=ssh"};
		Process cvsParser = Runtime.getRuntime().exec(Config.BASE+"libs/cvsParser.pl "+scope, env);
		cvsParser.waitFor();
		InputStream cvs = cvsParser.getInputStream();
		String cvsstr = Shot.parseISToString(cvs);

		// Extract the incriminated users
		String cvsxml = "<cvs>"+cvsstr+"</cvs>";
		//System.err.println("CVS output:\n"+cvsxml);
		StringReader stringReader = new StringReader(cvsxml);
		HashSet tos = new HashSet();
		DocumentBuilder builder =
		    DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new org.xml.sax.InputSource(stringReader));
		NodeList nodes = doc.getElementsByTagName("modif");
		for (int i = 0; i < nodes.getLength(); i++)
		    tos.add(((Element)nodes.item(i)).getAttribute("user").toString()+"@cs.vu.nl");
		

		// Retrieve the text content of the node
		String cont = flattenNode(element);


		// Create a well formed xml
		cvsstr = "<sensor id=\""+id+"\" name=\""+name+"\" returnValue=\""+element.getAttribute("returnValue")+"\" link=\""+shotXml+"\" >"+cont+cvsstr.trim()+"</sensor>";

		//Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.vu.nl");
		
		// create some properties and get the default Session
		Session session = Session.getDefaultInstance(props, null);
		

		// create the message
		StringWriter stringWriter;
		StringWriter finalString;

		// intermediate xml
		File xsltFile = new File(Config.BASE+"libs/style_mail.xml");
		File xsltPlain = new File(Config.BASE+"libs/style_plain.xml");
		
		javax.xml.transform.Source xmlSource =
		    new javax.xml.transform.stream.StreamSource(new StringReader(cvsstr));
		    //new DOMSource(element);
		javax.xml.transform.Source xsltSource =
		    new javax.xml.transform.stream.StreamSource(xsltFile);
		javax.xml.transform.Result tmpRes =
		    new javax.xml.transform.stream.StreamResult(stringWriter = new StringWriter());
		
		javax.xml.transform.TransformerFactory transFact =
		    javax.xml.transform.TransformerFactory.newInstance(  );
		
		javax.xml.transform.Transformer trans =
		    transFact.newTransformer();

		trans.transform(xmlSource, tmpRes);

		// final html
		javax.xml.transform.Source tmpSrc =
		    new javax.xml.transform.stream.StreamSource(new StringReader(stringWriter.toString()));
		javax.xml.transform.Result htmlRes =
		    new javax.xml.transform.stream.StreamResult(finalString = new StringWriter());

		trans =
		    transFact.newTransformer(xsltSource);

		trans.transform(tmpSrc, htmlRes);
		String html_text  = finalString.toString();

		// final plain
		xsltSource =
		    new javax.xml.transform.stream.StreamSource(xsltPlain);
		tmpSrc =
		    new javax.xml.transform.stream.StreamSource(new StringReader(stringWriter.toString()));
		htmlRes =
		    new javax.xml.transform.stream.StreamResult(finalString = new StringWriter());

		trans =
		    transFact.newTransformer(xsltSource);

		trans.transform(tmpSrc, htmlRes);
		String plain_text  = finalString.toString();


		// assemble the message
		MimeMultipart content = new MimeMultipart("alternative");
		MimeBodyPart text = new MimeBodyPart();
		MimeBodyPart html = new MimeBodyPart();
		text.setText(plain_text);
		html.setContent(html_text, "text/html");
		content.addBodyPart(text);
		content.addBodyPart(html);
		Message msg = new MimeMessage(session);
		
		// set the from and to address
		InternetAddress addressFrom = new InternetAddress("ibis@cs.vu.nl");
		msg.setFrom(addressFrom);
		
// 		InternetAddress[] addressTo = new InternetAddress[tos.size()];

// 		int index = 0;
// 		for (Iterator itor = tos.iterator(); itor.hasNext(); index++) {
// 		    String to = (String) itor.next();
// 		    System.err.println("Adding To:"+to);
// 		    addressTo[index] = new InternetAddress(to);
// 		}
		
// 		msg.setRecipients(Message.RecipientType.TO, addressTo);

		InternetAddress[] addressTo = new InternetAddress[1]; 
		addressTo[0] = new InternetAddress("ceriel@cs.vu.nl");
		
		msg.setRecipients(Message.RecipientType.TO, addressTo);
		
		
		// Optional : You can also set your custom headers in the Email if you Want
		// msg.addHeader("MyHeaderName", "myHeaderValue");
		
		// Setting the Subject and Content Type
		msg.setSubject("Ibis Alarm triggered");
		/*	String[] env = {"CVS_RSH=ssh"};

		Process cvsParser = Runtime.getRuntime().exec("./cvsParser.pl "+scope, env);
		InputStream cvs = cvsParser.getInputStream();
		String cvsstr = Shot.parseISToString(cvs);
		*/
		msg.setContent(content);//"trigger on "+name+"\n"+cvsstr, "text/plain");
		// Transport.send(msg); --Ceriel: commented out temporarily

		if (element.getAttribute("fatal").equals("true"))
		    throw (new FatalSensorException());
	    }
	} 
	catch (FatalSensorException e) {throw(e);}
	catch (Exception e) {System.err.println("Failed in Mailing:");e.printStackTrace();}
    }
}

class RrdInput {

    private static String normalizeStr(String str, int len) {
	if (str.length() > len) {
	    return str.substring(0,len);
	} 
	else return str;
    }

    public RrdInput (Shot shot, String shotXml) throws FatalSensorException {

	try {

	    java.io.File file = new java.io.File(shotXml);
	    file.delete();


	    java.io.FileWriter out = new java.io.FileWriter(shotXml, true);
	    out.write(shot.str.toString());
	    out.close();

	    DocumentBuilder builder =
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc = builder.parse(shotXml);



	    // Running onoff sensors
	    NodeList nodes = ((Element) doc.getElementsByTagName("onoff").item(0)).getElementsByTagName("sensor");

	    
	    for (int i = 0; i < nodes.getLength(); i++) {
		Element element = (Element) nodes.item(i);

		if (element.getAttribute("id").equals("separator") || element.getAttribute("id").equals("part") || element.getAttribute("enabled").equals("false")) {
		    continue;
		}

		System.err.println("Updating "+element.getAttribute("name")+"...");

		if (element.getAttribute("graph").equals("true")) {
		    String filename = Config.RRDBASE+element.getAttribute("id")+".rrd";
		    java.io.File rrdfile = new java.io.File(filename);
		    if (!rrdfile.exists()) {
			System.err.println("File "+filename+" doesn't exist");
			create_rrddb(element);
		    }
		    
		    update_rrddb(element);
		    update_graphs(element);
		}
		Alarm alarm = new Alarm(element, "onoff", shotXml);
		alarm.trigger();
	    }


	    // Running graph sensors
	    nodes = ((Element) doc.getElementsByTagName("graph").item(0)).getElementsByTagName("sensor");

	    
	    for (int i = 0; i < nodes.getLength(); i++) {
		Element element = (Element) nodes.item(i);

		if (element.getAttribute("id").equals("separator")) continue;
		else if (element.getAttribute("id").equals("part")) continue;
		else if (element.getAttribute("enabled").equals("false")) continue;


		System.err.println("Updating "+element.getAttribute("name")+"...");


		String filename = Config.RRDBASE+element.getAttribute("id")+".rrd";
		java.io.File rrdfile = new java.io.File(filename);
		if (!rrdfile.exists()) {
		    System.err.println("File "+filename+" doesn't exist");
		    create_rrddb(element);
		}

		update_rrddb(element);
		update_graphs(element);
		Alarm alarm = new Alarm(element, "graph", shotXml);
		alarm.trigger();
	    }

	}
	catch (FatalSensorException e) {throw(e);}
	catch (Exception e) {
	    e.printStackTrace();
	}      
 


	/*
	RrdDef rrdDef = new RrdDef(rrdPath, start - 1, 300);
	rrdDef.addDatasource("sun", "GAUGE", 600, 0, Double.NaN);
	rrdDef.addDatasource("shade", "GAUGE", 600, 0, Double.NaN);
	*/}

   private void create_rrddb(Element element) {

	try {
	    System.err.println("Creating RrdDb...");
	    String id = element.getAttribute("id");
	    String name = element.getAttribute("name");
	    
	    FileWriter xml = new FileWriter(Config.RRDBASE+id+".xml", true);
	    
	    xml.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<?xml-stylesheet type=\"text/xsl\" href=\"../libs/style_id.xml\"?>\n");
	    xml.write("<sensor id=\""+id+"\" name=\""+name+"\" project=\""+Config.PROJECT+"\"/>\n");
	    xml.close();

	    RrdDef rrdDef = new RrdDef(Config.RRDBASE+id+".rrd", Config.RRDSTEP);
	    rrdDef.setStartTime(Util.normalize(Util.getTime(), Config.RRDSTEP) - 1);

	    NodeList tests = element.getElementsByTagName("test");
	    
	    for (int i = 0; i < tests.getLength(); i++) {
		Element test = (Element) tests.item(i);
		
		String testId = normalizeStr(test.getAttribute("id"), 20);
		String testValue = test.getAttribute("value");
		
		rrdDef.addDatasource(testId, "GAUGE", Config.RRDSTEP, Double.NaN, Double.NaN);
	    }
	    rrdDef.addArchive("LAST", 0.5, 1, 600);
	    rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
	    rrdDef.addArchive("AVERAGE", 0.5, 3, 600);



	    RrdDb rrdDb = new RrdDb(rrdDef);
	    rrdDb.close();
	    
	}
	catch (Exception e) {
	    e.printStackTrace();
	}      
	
	
    }


    private void update_rrddb(Element element) {

	try {
	    
	    String id = element.getAttribute("id");
	    String name = element.getAttribute("name");
	    System.err.println("Updating records of "+name+"("+id+")"+"...");
	    
	    
	    NodeList tests = element.getElementsByTagName("test");
	    
	    RrdDb rrdDb = new RrdDb(Config.RRDBASE+id+".rrd");
	    

	    Sample sample = rrdDb.createSample();
	    for (int i = 0; i < tests.getLength(); i++) {
		Element test = (Element) tests.item(i);
		
		String testId = normalizeStr(test.getAttribute("id"),20);
		String testName = test.getAttribute("name");
		String testValue = test.getAttribute("value");
		//To avoid the following decimal notation. ?,??
		testValue = testValue.replace(',','.');
		System.err.println("Updating "+testName+"("+testId+")"+" with "+testValue);
		
		//Double doub = new Double(0.0);
		//sample.setTime(Util.getTime());
		long time = Util.normalize(Util.getTime(), rrdDb.getRrdDef().getStep());
		if (Util.getTime() > time+3*rrdDb.getRrdDef().getStep()/4) time += rrdDb.getRrdDef().getStep();
		sample.setTime(time);
		sample.setValue(testId, Double.parseDouble(testValue));
	    }
	    sample.update();
	    
	    rrdDb.close();
	}
	catch (org.jrobin.core.RrdException e) {
	    System.err.println("Timestamp already present. Dropping this one");
	}
	catch (Exception e) {
	    e.printStackTrace();
	}      
	

    }

    private void update_graphs(Element element) {

	try {
	    System.err.println("Updating graphs...");
    
	    String id = element.getAttribute("id");
	    String name = element.getAttribute("name");

	    //	    RrdDb rrdDb = new RrdDb("rrd/"+id+".rrd");
	    //String [] dsName = rrdDb.getDsNames();
    
	    NodeList tests = element.getElementsByTagName("test");
	    ColorChooser colorChooser = new ColorChooser(tests.getLength());	    
	    String rrdFile = Config.RRDBASE+id+".rrd";

	    RrdGraphDef gDefS = new RrdGraphDef(Util.normalize(Util.getTime(), Config.RRDSTEP)-604800, Util.normalize(Util.getTime(), Config.RRDSTEP)); // 7 days
	    RrdGraphDef gDefL = new RrdGraphDef(Util.normalize(Util.getTime(), Config.RRDSTEP)-15552000, Util.normalize(Util.getTime(), Config.RRDSTEP)); // 6 months

	    gDefS.setTitle(name);
	    //gDef.setVerticalLabel("Ordonnees");
	    gDefS.setLowerLimit(0.0);
	    gDefS.setBaseValue(1024);
	    gDefL.setTitle(name);
	    gDefL.setLowerLimit(0.0);
	    gDefL.setBaseValue(1024);


	    for (int i = 0; i < tests.getLength(); i++) {
		Element test = (Element) tests.item(i);

		String testName = test.getAttribute("name");
		String testId = normalizeStr(test.getAttribute("id"),20);
		String testUnit = test.getAttribute("unit");
		Color color = colorChooser.getColor();

		gDefS.datasource(testId, Config.RRDBASE+id+".rrd", testId, "AVERAGE");
		gDefS.line(testId, color, testName);
		gDefS.gprint(testId, "LAST", "    Last = @2.1@s"+testUnit+"@l");
		gDefL.datasource(testId, Config.RRDBASE+id+".rrd", testId, "AVERAGE");
		gDefL.line(testId, color, testName);
		gDefL.gprint(testId, "LAST", "    Last = @2.1@s"+testUnit+"@l");

		/*gDef.gprint("sun", "AVERAGE", "avgSun = @3@S@r");
		  gDef.gprint("shade", "MAX", "maxShade = @3@S");
		  gDef.gprint("shade", "AVERAGE", "avgShade = @3@S@r"); */
		// create graph finally
	    }
	    RrdGraph graphS = new RrdGraph(gDefS);
	    RrdGraph graphL = new RrdGraph(gDefL);
	    
	    if (tests.getLength() > 0)
		graphS.saveAsPNG(Config.RRDBASE+id+"-S.png", 800, 200);
		graphL.saveAsPNG(Config.RRDBASE+id+"-L.png", 800, 200);

	}
	
	catch (Exception e) {
	    e.printStackTrace();
	}      
	

    }
}


public class Stats{

    public static void main(String[] args) throws FatalSensorException{
	if (args.length != 2) {
	    System.err.println("Wrong number of arguments. Usage:\njava codmon.jar <config-file> <output-file>");
	    System.exit(1);
	}

	String config = args[0];
	String out = args[1];

	Shot shot = new Shot(config);

	RrdInput rrdInput = new RrdInput(shot, out);

	//	System.out.println(shot.str);
    }
}
