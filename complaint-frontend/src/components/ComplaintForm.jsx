import React, { useState } from 'react';
import api from '../services/api';
import 'bootstrap/dist/css/bootstrap.min.css';

const CATEGORIES = ['Academic', 'Hostel', 'Facilities', 'Administration', 'Other'];
const PRIORITIES  = ['Low', 'Medium', 'High', 'Urgent'];

const ComplaintForm = ({ onComplaintCreated }) => {
  const [title, setTitle]             = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory]       = useState('');
  const [priority, setPriority]       = useState('');
  const [message, setMessage]         = useState('');
  const [messageType, setMessageType] = useState('');
  const [aiStatus, setAiStatus]       = useState(''); // 'analyzing' | 'done' | 'error' | ''

  // ── AI Analysis ────────────────────────────────────────────────────────────
  const handleAnalyze = async () => {
    const text = `${title} ${description}`.trim();
    if (!text) {
      setAiStatus('error');
      setMessage('Please enter a title and description before analyzing.');
      setMessageType('warning');
      return;
    }

    setAiStatus('analyzing');
    setMessage('');

    try {
      const res = await api.post('/api/ai/analyze', { text });
      const { category: cat, priority: pri, source } = res.data;

      if (cat) setCategory(cat);
      if (pri) setPriority(pri);

      setAiStatus('done');
      setMessage(
        source === 'fallback'
          ? '⚠️ AI unavailable – defaults applied. You can edit them below.'
          : '✅ AI analysis complete! Fields auto-filled below.'
      );
      setMessageType(source === 'fallback' ? 'warning' : 'success');
    } catch (err) {
      // AI failure must NOT block submission — just show a message
      setAiStatus('error');
      setMessage('⚠️ AI analysis unavailable. Please fill category and priority manually.');
      setMessageType('warning');
    }
  };

  // ── Complaint Submission ───────────────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await api.post('/api/complaints', { title, description, category });

      setMessage('Complaint filed successfully!');
      setMessageType('success');
      setTitle('');
      setDescription('');
      setCategory('');
      setPriority('');
      setAiStatus('');

      setTimeout(() => setMessage(''), 3000);

      if (onComplaintCreated) onComplaintCreated();
    } catch (error) {
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

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div className="card p-4 mb-4">
      <form onSubmit={handleSubmit}>

        {/* Title */}
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

        {/* Description */}
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

        {/* AI Analyze Button */}
        <div className="mb-3">
          <button
            type="button"
            className="btn btn-outline-primary btn-sm"
            onClick={handleAnalyze}
            disabled={aiStatus === 'analyzing'}
          >
            {aiStatus === 'analyzing' ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Analyzing…
              </>
            ) : (
              '✨ Analyze with AI'
            )}
          </button>
          <small className="text-muted ms-2">Auto-fills category &amp; priority</small>
        </div>

        {/* Category */}
        <div className="mb-3">
          <label htmlFor="category" className="form-label">Category</label>
          <select
            className="form-select"
            id="category"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            required
          >
            <option value="">-- Select category --</option>
            {CATEGORIES.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>

        {/* Priority (display only – not sent to DB) */}
        <div className="mb-3">
          <label htmlFor="priority" className="form-label">
            Priority <small className="text-muted">(AI suggestion)</small>
          </label>
          <select
            className="form-select"
            id="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value)}
          >
            <option value="">-- Select priority --</option>
            {PRIORITIES.map(p => (
              <option key={p} value={p}>{p}</option>
            ))}
          </select>
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
