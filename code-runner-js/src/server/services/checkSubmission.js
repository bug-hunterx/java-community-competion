const requireFromString = require('require-from-string');
const axios = require('axios');
const { getUserTasks, markTaskAsDoneAndUnlockNextTaskForUser, saveSubmissionInfo } = require('./dataService');


const REQUIRED_PASS_PERCENT = 100;

module.exports = (taskId, userId, source, language) => {
  return getUserTasks(userId)
      .then(userTasks => {
          const task = userTasks.find(task => task.id === taskId);
          const acceptanceTests = Object.keys(task.acceptanceTests)
          if (task.status === 'LOCKED') {
              return {
                  status: 'You did not unlock this task yet',
              };
          }

          const result = {
            totalTestCount: acceptanceTests.length,
            testsPassed: 0,
            tests: {
              passed: [],
              failed: []
            }

        }

          if (language === 'java') {
              // console.log(`Provided java code for taskId ${taskId}`);
              // console.log(`Source: ${source}`);

              return axios.post(`http://localhost:8090/task/submit`, { source,
              userId, taskId})
                  .then(response => {
                    // console.log('Result from java:', response.data);


                    if(response.data.errorType){
                        return {
                            error: response.data.errorType + "; " + response.data.errorMsg
                        }
                    }

                    const testStatuses = response.data.testsStatuses;

                      result.totalTestCount = testStatuses.length;
                      result.testsPassed = 0;
                      testStatuses.forEach((status, index) => {
                        if(status === 'PASS'){
                            result.tests.passed.push(acceptanceTests[index])
                            result.testsPassed += 1;
                        } else {
                            result.tests.failed.push(acceptanceTests[index])
                        }
                      });

                      if(response.data.currentFailedInput){
                        result.firstFailedInput = response.data.currentFailedInput;
                      }

                      if (result.testsPassed / result.totalTestCount >= REQUIRED_PASS_PERCENT / 100) {
                          markTaskAsDoneAndUnlockNextTaskForUser(userId, taskId);
                      }
                      saveSubmissionInfo(userId, taskId, result);
                      // console.log('Result js:', result);
                      return result;
                  })
                  .catch (e => {
                    console.log(e);
                    return {
                      error: e.message
                    }
                  })
          } else {
            try {
                const functionToTest = requireFromString(source);

                console.log(task)

                Object.keys(task.acceptanceTests).forEach((input) => {
                    const expectedOutput = task.acceptanceTests[input].trim();
                    const actualOutput = functionToTest(input).trim();
                    if (expectedOutput === actualOutput) {
                        result.testsPassed += 1;
                        result.tests.passed.push(actualOutput)
                    } else {
                        console.log(`Test failed for user ${userId}: expected: ${expectedOutput}, actual: ${actualOutput}`);
                        result.tests.failed.push(actualOutput)
                    }
                });
                if (result.testsPassed / result.totalTestCount >= REQUIRED_PASS_PERCENT / 100) {
                    markTaskAsDoneAndUnlockNextTaskForUser(userId, taskId);
                }
                saveSubmissionInfo(userId, taskId, result);

                return result;
            } catch (e) {
              console.log(e);
              return {
                error: e.message
              }
            }
          }
      })
      .catch(e => console.log(e))
};