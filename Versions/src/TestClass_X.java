import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestClass_Origin {

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int[][] mass = new int[10][10];

        for (int i = 0; i < 10; i++) {
            String l = reader.readLine();
            String[] s = l.split(" ");
            for (int j = 0; j < s.length; j++)
                mass[i][j] = Integer.parseInt(s[j]);
        }
        String line = reader.readLine();
        System.out.print( (int)(Math.random()*9) );
        System.out.print(" ");
        System.out.println( (int)(Math.random()*9) );
        System.out.print( (int)(Math.random()*9) );
        System.out.print(" ");
        System.out.println( (int)(Math.random()*9) );
        System.out.print( (int)(Math.random()*9) );
        System.out.print(" ");
        System.out.println( (int)(Math.random()*9) );
    }


}
