package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.*;

public class JavaCodeRunnerTest {

    private final TasksStorage tasksStorage = mock(TasksStorage.class);
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

    private final JavaCodeRunner testee = new JavaCodeRunner(tasksStorage, taskExecutor);

    @Before
    public void setup(){
        doAnswer(invocationOnMock -> {
            final Runnable task = invocationOnMock.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).submit(any());

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
    public void shouldCompileAndRun() throws InterruptedException {
        testee.run(1, code);

        Thread.sleep(1000);

        final ArgumentCaptor<TestingStatus> captor = ArgumentCaptor.forClass(TestingStatus.class);

        verify(tasksStorage).updateTestStatus(anyLong(), captor.capture());
        verify(taskExecutor).submit(any());

        final TestingStatus result = captor.getValue();
        assertThat(captor.getAllValues().size()).isEqualTo(1);

        assertThat(result.isAllTestsDone()).isTrue();
        assertThat(result.isAllTestsPassed()).isFalse();

        assertThat(result.getTestsStatuses()).containsExactly(PASS, PASS, FAIL);
    }
}