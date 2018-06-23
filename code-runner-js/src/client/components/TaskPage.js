import React, {Component} from "react"
import { fetchTaskData } from '../services/dataService';
import UploadForm from './FileUpload';

class TaskDetails extends React.Component {

    state = {
        task: null
    }

    componentDidMount() {
        fetchTaskData(this.props.match.params.taskId)
            .then(task => this.setState({task}) )
            .catch(e => console.log(e))

    }
    //
    // componentWillReceiveProps(nextProps){
    //     if (!nextProps.taskId) return
    //     fetchTaskData(nextProps.taskId)
    //         .then(task => this.setState({task}))
    //         .catch(e => console.log(e))
    // }

    render() {
        let task = this.state.task
        return (
            <div className='row'>
                {task && <div className='col-lg-12'>
                    <div className="card">
                        <div className="card-header">
                            {task.title}
                        </div>
                        <p dangerouslySetInnerHTML={{__html:task.htmlDesc}}></p>
                    </div>
                    <div>
                        <UploadForm taskId={this.props.match.params.taskId}/>
                    </div>
                </div>}
            </div>)
    }
}

export default TaskDetails;