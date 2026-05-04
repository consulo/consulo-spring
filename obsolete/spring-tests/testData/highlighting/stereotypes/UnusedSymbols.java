 import org.springframework.beans.factory.annotation.Autowired;
 import jakarta.annotation.Resource;

 public class UnusedSymbols {

     @Resource
     private String a;

     @Autowired
     private String b;
 
     private final String s;

     private UnusedSymbols(String s) {
         this.s = s;
     }

     void dosomething() {
         System.out.println(a);
         System.out.println(b);
         System.out.println(s);
     }
 }
