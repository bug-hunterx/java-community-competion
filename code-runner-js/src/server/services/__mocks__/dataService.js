
const getUserTasks = userId => [
  {
    id: 'ts2',
    title: 'ts2',
    shortDesc: 'Find sum',
    longDesc: 'Return sum of all digits',
    submitted: false,
    testExamples: {
      1: '1',
      '1,2,3': '6',
    },
    acceptanceTests: {
      1: '1',
      '1,2,3': '6',
      '1,2,3,4,5,6': '21',
    },
  },
];

// const getTaskById = (userId, taskId) => taskList.find(task => task.id == taskId);

module.exports = {
  getUserTasks,
  // getTaskById,
};
