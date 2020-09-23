package Config;

import java.util.HashSet;


public class Config {
	
	// You must modify these fields based on your configuration
	public static String host = "localhost";
	public static int port=5432;	//the (default)port number specified when installing postgresql 
	public static String dbName = "testa";	// your database name
	public static String dbPath="jdbc:postgresql://localhost:"+port+"/"+dbName;	//path of your database
	public static String usrName="sba";	//the username of your database
	public static String pssWord="rjxwfx";	//the password of your database
		
	
	public static long MIN_INS_NUM=10;	// a threshold below which a function will be considered as trivial
	
	public static boolean FLOYDALG=false;	//use FLOYDALG or Dijkstra algorithm(default)
	
	//basically you do not need to change it 
	public static final boolean NODE_TYPE_SIZE_PREFIX_USE=false;	//使用byte、word等修饰符

	
	public static String[] UNSUPPORTEDCAHRS={
			"\\?",
			"\\$",
			"@",
			"\\."
	};

	public static final HashSet<String> INS_ARITHMETIC = new HashSet<String>() {{  
        add("add");  
        add("adc");  
        add("inc");  
        add("aaa");  
        add("daa");  
        add("sub");  
        add("sbb");
        add("dec");  
        add("aas");  
        add("das");  
        add("mul");  
        add("imul");  
        add("aam");  
        add("div"); 
        add("idiv");  
        add("cbw");  
        add("cwd");  
        add("cmp");
    }};   
    
    public static final HashSet<String> INS_LOGIC = new HashSet<String>() {{  
        add("not");  
        add("sal");  
        add("shl");  
        add("sar");  
        add("shr");  
        add("rol");  
        add("ror");
        add("rcl");  
        add("rcr");  
        add("and");  
        add("or");  
        add("xor");  
        add("test");  
    }};
    
    public static final HashSet<String> INS_CONTROL_TRANSFER = new HashSet<String>() {{  
        add("loop");  
        add("loopne");
        add("int");  
        add("into");
        add("iret");  
    }};
    
    public static final HashSet<String> INS_CPU_CONTROL = new HashSet<String>() {{  
        add("clc");  
        add("cmc");  
        add("stc");  
        add("cld");  
        add("std");  
        add("cli");  
        add("sti");
        add("hlt");  
        add("wait");  
        add("esc");  
        add("lock");  
    }};
    
    public static final HashSet<String> INS_STROP = new HashSet<String>() {{  
        add("movs");  
        add("cmps");  
        add("scas");  
        add("lods");  
        add("stos");  
        add("rep");    
    }};
}
