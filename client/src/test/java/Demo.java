import java.text.MessageFormat;

public class Demo {
    public static void main(String[] args) {
        System.out.println(MessageFormat.format("/demo/{0}{1,/#}", "1111"));
    }
}
