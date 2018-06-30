import java.util.Arrays;
import java.util.function.Function;

public class Solution1 implements Function<String, String> {

    private int state = 0;

    @Override
    public String apply(String s) {
        int sum = Arrays.stream(s.split(",")).map(Integer::parseInt).reduce(0, Integer::sum);
        sum = sum + state;
        state++;
        return String.valueOf(sum);
    }
}