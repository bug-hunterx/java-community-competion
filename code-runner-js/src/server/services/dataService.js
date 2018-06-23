const taskList = require('./mock');

const getUserTasks = userId => taskList;

// const getTaskById = (userId, taskId) => taskList.find(task => task.id == taskId);

module.exports = {
  getUserTasks,
  // getTaskById,
};
