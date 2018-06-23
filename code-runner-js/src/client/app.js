import React, { Component } from 'react';
import ReactDOM from 'react-dom';

import './style/bootstrap.min.css';
import './style/style.css';

import { BrowserRouter, Switch, Route } from 'react-router-dom';

import TasksList from './components/TasksList';
import TaskPage from './components/TaskPage';
import ResultDashboard from './components/ResultsDashboard';
import LoginPage from './components/LoginPage';

const App = () => (
  <div className="container">
    <Main />
  </div>
);

const Main = () => (
  <main />
);


ReactDOM.render(<BrowserRouter>
  <div>
    <Route exact path="/" component={TasksList} />
    <Route exact path="/task/:taskId" component={TaskPage} />
    <Route exact path="/dashboard" component={ResultDashboard} />
    <Route exact path="/login" component={LoginPage} />
  </div>
                </BrowserRouter>, document.getElementById('root'));
