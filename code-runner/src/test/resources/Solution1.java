import java.util.Arrays;
import java.util.function.Function;

public class Solution1 implements Function<String, String> {

    @Override
    public String apply(String s) {
        return Arrays.stream(s.split(",")).map(str-> Integer.parseInt(str)).reduce((l, r) -> l+r).get() + "";
    }
}