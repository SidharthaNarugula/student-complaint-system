﻿import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

const Navbar = ({ role, onLogout, userName }) => {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <span className="navbar-brand">Student Complaint System</span>
        <div className="ms-auto">
          {userName && <span className="text-light me-3">User: {userName}</span>}
          <span className="text-light me-3">Role: {role}</span>
          <button className="btn btn-sm btn-outline-light" onClick={onLogout}>
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
