const requireFromString = require('require-from-string');
const { getUserTasks } = require('./dataService');

module.exports = (taskId, userId, source, language) => {
  const userTasks = getUserTasks(userId);

  const task = userTasks.find(task => task.id === taskId);

  if (!task) {
    return {
      status: 'Task not found',
    };
  }

  if (language === 'java') {
    console.log(`Provided java code for taskId ${taskId}`);
    console.log(JSON.stringify({ source: sourceFile.data.toString('utf8') }));

    try {
      const result = axios.post(`http://localhost:8090/task/${taskId}`, { source: sourceFile.data.toString('utf8') });
      return result;
    } catch (e) {
      console.log(e);
      return {
        error: e.message,
      };
    }
  } else {
    const functionToTest = requireFromString(source);
    const result = {
      totalTestCount: Object.keys(task.acceptanceTests).length,
      testsPassed: 0,

    };

    Object.keys(task.acceptanceTests).forEach((input) => {
      const expectedOutput = task.acceptanceTests[input];
      const actualOutput = functionToTest(input);
      if (expectedOutput == actualOutput) {
        result.testsPassed += 1;
      } else if (!result.firstFailedInput) {
        result.firstFailedInput = input;
      }
    });

    return result;
  }
};
