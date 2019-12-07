/**
 * @jest-environment node
 */
jest.mock('./dataService');

const checkSubmission = require('./checkSubmission');

describe('test submission checker', () => {
  const solution = 'module.exports = (input) => input';

  test('should return result with first failure input', async () => {
    const result = checkSubmission('ts2', 'userId', solution, 'js');
    expect(result.totalTestCount).toBe(3);
    expect(result.testsPassed).toBe(1);
    expect(result.tests.passed).toBe(['1']);
    expect(result.tests.passed).toBe(['1,2,3', '1,2,3,4,5,6']);
    expect(result.firstFailedInput).toBe('1,2,3');
  });
});
