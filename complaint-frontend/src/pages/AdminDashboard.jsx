import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Navbar from '../components/Navbar';
import ComplaintList from '../components/ComplaintList';
import 'bootstrap/dist/css/bootstrap.min.css';

const AdminDashboard = () => {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user data exists in localStorage and is admin
    const storedUser = localStorage.getItem('user');

    if (!storedUser) {
      // User not authenticated, redirect to login
      navigate('/');
      return;
    }

    try {
      const userData = JSON.parse(storedUser);

      // Check if user is admin
      if (userData.role !== 'ADMIN') {
        // User is not admin, redirect to login
        navigate('/');
        return;
      }

      setUser(userData);
      fetchComplaints();
    } catch (error) {
      console.error('Error parsing user data:', error);
      navigate('/');
    }
  }, [navigate]);

  const fetchComplaints = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get('/api/complaints');
      setComplaints(response.data);
    } catch (error) {
      console.error('Error fetching complaints:', error);

      // Handle specific error cases
      if (error.response?.status === 401 || error.response?.status === 403) {
        // Session expired/invalidated (e.g. backend restart) or truly forbidden
        // Redirect to login so the user can re-authenticate
        localStorage.removeItem('user');
        navigate('/');
      } else {
        // Generic error
        setError('Failed to load complaints: ' + (error.response?.data?.error || error.message));
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear user data from localStorage
      localStorage.removeItem('user');
      navigate('/');
    }
  };

  if (!user) {
    return <div className="spinner-border" role="status"><span className="visually-hidden">Loading...</span></div>;
  }

  return (
    <div>
      <Navbar role="ADMIN" onLogout={handleLogout} userName={user.name} />
      <div className="container mt-4">
        <h2>Admin Dashboard</h2>
        <p className="text-muted">Welcome, {user.name}!</p>

        {error && (
          <div className="alert alert-danger alert-dismissible fade show" role="alert">
            {error}
            <button type="button" className="btn-close" onClick={() => setError(null)}></button>
          </div>
        )}

        <h3>All Complaints</h3>
        {loading ? (
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        ) : (
          <ComplaintList isAdmin={true} complaints={complaints} onComplaintUpdated={fetchComplaints} />
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
