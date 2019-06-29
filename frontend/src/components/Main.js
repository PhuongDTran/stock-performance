import React from 'react';
import { BrowserRouter as Router, Route, Redirect, Switch, withRouter } from 'react-router-dom';
import SigninForm from './SigninForm';
import SignupForm from './SignupForm';

import './css/Main.css';

import Footer from './Footer';
import Header from './Header';

function Main() {
  return (
    <React.Fragment>
      <Router>
      <Header/>
        <Switch>
        <Route exact path="/signin/" component={withRouter(SigninForm)} />
        <Route exact path="/signup/" component={withRouter(SignupForm)} />
        <Route path="*" render={() => (
          <Redirect to="/signin/" />
        )} />
        </Switch>
      </Router>
    </React.Fragment>
  );
}

export default Main;
