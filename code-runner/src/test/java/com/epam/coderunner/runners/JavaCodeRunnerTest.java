package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Map;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.*;

public class JavaCodeRunnerTest {

    private final TasksStorage tasksStorage = mock(TasksStorage.class);

    private static final String code = "" +
            "import java.util.function.Function;\n" +
            "\n" +
            "public class Solution1 implements Function<String, String> {\n" +
            "\n" +
            "    @Override\n" +
            "    public String apply(String s) {\n" +
            "        return s;\n" +
            "    }\n" +
            "}";

    private final JavaCodeRunner testee = new JavaCodeRunner(tasksStorage, 1);

    @Before
    public void setup(){
        final Map<String, String> inOut = ImmutableMap.of(
                "1", "1",
                "2", "2",
                "asdasd, asdads", "asdasd, asdasd"
        );
        final Task task = new Task();
        task.setAcceptanceTests(inOut);
        doReturn(task).when(tasksStorage).getTask(1);
    }

    @Test
    public void shouldCompileAndRun() {

        final TestingStatus result = testee.run(1, code).block(Duration.ofSeconds(1));
        assertThat(result).isNotNull();
        assertThat(result.isAllTestsDone()).isTrue();
        assertThat(result.isAllTestsPassed()).isFalse();

        assertThat(result.getTestsStatuses()).containsExactly(PASS, PASS, FAIL);
    }
}