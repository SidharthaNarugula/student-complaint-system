import React, { useState } from 'react';
import api from '../services/api';
import 'bootstrap/dist/css/bootstrap.min.css';

const ComplaintForm = ({ onComplaintCreated }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const newComplaint = {
        title,
        description,
        category
      };

      await api.post('/api/complaints', newComplaint);

      setMessage('Complaint filed successfully!');
      setMessageType('success');
      setTitle('');
      setDescription('');
      setCategory('');

      setTimeout(() => setMessage(''), 3000);

      // Call the callback to refresh the complaints list
      if (onComplaintCreated) {
        onComplaintCreated();
      }
    } catch (error) {
      // Provide specific error messages
      if (error.response?.status === 401) {
        setMessage('Your session has expired. Please login again.');
      } else if (error.response?.status === 403) {
        setMessage('You do not have permission to file complaints.');
      } else if (error.response?.data?.error) {
        setMessage('Error filing complaint: ' + error.response.data.error);
      } else {
        setMessage('Error filing complaint: ' + (error.message || 'Unknown error'));
      }
      setMessageType('danger');
    }
  };

  return (
    <div className="card p-4 mb-4">
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label htmlFor="title" className="form-label">Title</label>
          <input
            type="text"
            className="form-control"
            id="title"
            placeholder="Enter complaint title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div className="mb-3">
          <label htmlFor="description" className="form-label">Description</label>
          <textarea
            className="form-control"
            id="description"
            rows="4"
            placeholder="Describe your complaint"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
          />
        </div>

        <div className="mb-3">
          <label htmlFor="category" className="form-label">Category</label>
          <input
            type="text"
            className="form-control"
            id="category"
            placeholder="e.g., Infrastructure, Faculty, etc."
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="btn btn-primary">
          Submit Complaint
        </button>
      </form>

      {message && (
        <div className={`alert alert-${messageType} mt-3`} role="alert">
          {message}
        </div>
      )}
    </div>
  );
};

export default ComplaintForm;
