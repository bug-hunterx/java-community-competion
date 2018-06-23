package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TaskStorage;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public final class JavaCodeRunnerTest {

    private final TaskStorage taskStorage = mock(TaskStorage.class);
    private final TaskExecutor taskExecutor = mock(TaskExecutor.class);

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

    private final RuntimeCodeCompiler codeCompiler = new RuntimeCodeCompiler(new SourceCodeGuard());
    private final JavaCodeRunner testee = new JavaCodeRunner(taskExecutor, taskStorage, codeCompiler);

    @Before
    public void setup(){
        final Map<String, String> inOut = ImmutableMap.of(
                "1", "1",
                "2", "2",
                "asdasd, asdads", "asdasd, asdasd"
        );
        final Task task = new Task();
        task.setAcceptanceTests(inOut);
        doReturn(task).when(taskStorage).getTask(1);

        when(taskExecutor.submit(any())).thenAnswer(invocationOnMock -> {
            final Callable<TestingStatus> callable = invocationOnMock.getArgument(0);
            return Mono.fromCallable(callable);
        });
    }

    @Test
    public void shouldCompileAndRun() {
        final TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(1);
        taskRequest.setUserId("user@epam.com");
        taskRequest.setSource(code);

        final TestingStatus result = testee.run(taskRequest).block(Duration.ofSeconds(1));

        verify(taskStorage).getTask(1);

        assertThat(result).isNotNull();
        assertThat(result.isAllTestsDone()).isTrue();
        assertThat(result.isAllTestsPassed()).isFalse();
        assertThat(result.getTestsStatuses()).containsExactly(PASS, PASS, FAIL);
        assertThat(result.getCurrentFailedInput()).isEqualTo("asdasd, asdads");
    }
}
