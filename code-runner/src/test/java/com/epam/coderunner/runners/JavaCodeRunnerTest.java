package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static com.epam.coderunner.Status.FAIL;
import static com.epam.coderunner.Status.PASS;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JavaCodeRunnerTest {

    private final TasksStorage tasksStorage = mock(TasksStorage.class);
    private final TaskExecutor taskExecutor = mock(TaskExecutor.class);

    private String code = "" +
            "import java.util.function.Function;\n" +
            "\n" +
            "public class Test implements Function<String, String> {\n" +
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
    }

    @Test
    public void shouldCompileAndRun() throws InterruptedException {

        Map<String, String> inOut = ImmutableMap.of(
                "1", "1",
                "2", "2",
                "asdasd, asdads", "asdasd, asdasd"
        );



        testee.runCode("Test", code, inOut);

        Thread.sleep(1000);

        ArgumentCaptor<TestingStatus> captor = ArgumentCaptor.forClass(TestingStatus.class);

        verify(tasksStorage).updateTestStatus(anyString(), captor.capture());
        verify(taskExecutor).submit(any());

        TestingStatus result = captor.getValue();

        assertTrue(result.isAllTestsDone());
        assertFalse(result.isAllTestsPassed());

        assertThat(result.getTestsStatuses(), Matchers.contains(PASS, PASS, FAIL));
    }
}