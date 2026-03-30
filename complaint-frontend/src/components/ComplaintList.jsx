import React, { useState } from 'react';
import api from '../services/api';
import 'bootstrap/dist/css/bootstrap.min.css';

const VALID_TRANSITIONS = {
  OPEN: ['IN_PROGRESS'],
  IN_PROGRESS: ['RESOLVED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
};

const ComplaintList = ({ isAdmin, complaints, onComplaintUpdated }) => {
  const [error, setError] = useState('');

  const handleStatusChange = async (complaintId, newStatus) => {
    try {
      await api.patch(`/api/complaints/${complaintId}/status`, { status: newStatus });

      if (onComplaintUpdated) {
        onComplaintUpdated();
      }
    } catch (err) {
      const message = err.response?.data?.message || err.message;
      setError('Error updating status: ' + message);
    }
  };
    const handleDelete = async (complaintId) => {
      const confirmed = window.confirm("Are you sure you want to delete this complaint?");
      if (!confirmed) return;

      try {
        await api.delete(`/api/complaints/${complaintId}`);

        if (onComplaintUpdated) {
          onComplaintUpdated();
        }
      } catch (err) {
        setError('Error deleting complaint: ' + (err.response?.data?.error || err.message));
      }
    };

  if (!complaints || complaints.length === 0) {
    return <div className="text-center text-muted mt-4">No complaints found</div>;
  }

  return (
    <div className="table-responsive">
      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>Title</th>
            <th>Category</th>
            <th>Description</th>
            <th>Status</th>
            {isAdmin && <th>Submitted By</th>}
            {isAdmin && <th>Email</th>}
            <th>Created Date</th>
            {isAdmin && <th>Action</th>}
          </tr>
        </thead>
        <tbody>
          {complaints.map(complaint => (
            <tr key={complaint.id}>
              <td>{complaint.title}</td>
              <td>{complaint.category}</td>
              <td style={{ maxWidth: '250px', whiteSpace: 'normal' }}>{complaint.description}</td>
              <td>
                {isAdmin ? (
                  <select
                    className="form-select form-select-sm"
                    value={complaint.status}
                    onChange={(e) => handleStatusChange(complaint.id, e.target.value)}
                  >
                    {/* Always show the current status as the first (selected) option */}
                    <option value={complaint.status}>{complaint.status}</option>
                    {/* Then show only valid next states */}
                    {(VALID_TRANSITIONS[complaint.status] || []).map(next => (
                      <option key={next} value={next}>{next}</option>
                    ))}
                  </select>
                ) : (
                  <span className="badge bg-info">{complaint.status}</span>
                )}
              </td>
              {isAdmin && <td>{complaint.submittedByName || 'Unknown'}</td>}
              {isAdmin && <td>{complaint.submittedByEmail || 'Unknown'}</td>}
              <td>{new Date(complaint.createdAt).toLocaleDateString()}</td>
              {isAdmin && (
                <td>
                  <button
                    className="btn btn-sm btn-danger"
                    onClick={() => handleDelete(complaint.id)}
                  >
                    Delete
                  </button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
      {error && <div className="alert alert-danger mt-3">{error}</div>}
    </div>
  );
};

export default ComplaintList;