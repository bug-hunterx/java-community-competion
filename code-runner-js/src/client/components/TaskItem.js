import React from 'react';
import classNames from 'classnames';

const Task = ({ id, title, submitted }) => (
  <div className="col-lg-3">
    <div className={classNames('task-item', { submitted })}>
      {title}
    </div>
  </div>
);

export default Task;
