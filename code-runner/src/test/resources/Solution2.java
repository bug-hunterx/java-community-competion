import java.util.function.Function;

public class Solution2 implements Function<String, String> {

    @Override
    public String apply(String s) {
        String concat = s;
        for (int i = 0; i < 99999; i++) { //busy more than 1 second
            concat = concat + s;
        }
        return concat;
    }
}