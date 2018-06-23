import React, { Component } from 'react';


export default class extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      userId: 'test-user',
      tasks: [],
    };
  }

  render() {
    return (
      <form onSubmit={this.onFormSubmit}>
        <h1>
File Upload
        </h1>
        <div>
Current location:
          {JSON.stringify(this.props.location)}
        </div>
        <input type="file" onChange={this.onChange} />
        <button type="submit">
Upload
        </button>
      </form>
    );
  }
}
