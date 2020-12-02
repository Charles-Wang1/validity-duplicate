import React, { Component } from 'react';
import { Link, withRouter } from 'react-router-dom';

class HomePage extends Component {
  render() {
    return (
        <div className="home-page">
          <Link to="/hello">Click to see the duplicate entries</Link>
        </div>
    );
  }
}

export default withRouter(HomePage);
